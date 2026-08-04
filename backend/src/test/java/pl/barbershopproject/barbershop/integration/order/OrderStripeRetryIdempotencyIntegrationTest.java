package pl.barbershopproject.barbershop.integration.order;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.stubbing.Scenario;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Sql(
        scripts = "/sql/order-stripe-retry-idempotency-cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class OrderStripeRetryIdempotencyIntegrationTest extends BaseIntegrationTest {

    private static final String USER_EMAIL = "johndoe@example.com";
    private static final String IDEMPOTENCY_KEY =
            "order-stripe-retry-idempotency-test-key";

    private static final String CHECKOUT_ENDPOINT = "/v1/checkout/sessions";
    private static final String STRIPE_SCENARIO = "stripe-checkout-retry";
    private static final String STRIPE_FAILED_ONCE = "stripe-failed-once";
    private static final String STRIPE_SECRET_KEY = "sk_test_integration";

    private static final String SESSION_ID = "cs_test_retry_123";
    private static final String CHECKOUT_URL =
            "https://checkout.stripe.com/c/pay/cs_test_retry_123";

    private static final LocalDateTime VISIT_DATE =
            LocalDateTime.of(2033, 1, 16, 12, 0);

    private static final WireMockServer stripeMock =
            new WireMockServer(options().dynamicPort());

    static {
        stripeMock.start();
    }

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @DynamicPropertySource
    static void configureStripeProperties(DynamicPropertyRegistry registry) {
        registry.add("stripe.api-base-url", stripeMock::baseUrl);
        registry.add("stripe.secret-key", () -> STRIPE_SECRET_KEY);
        registry.add(
                "stripe.success-url",
                () -> "http://localhost:3000/payment/success"
        );
        registry.add(
                "stripe.cancel-url",
                () -> "http://localhost:3000/payment/cancel"
        );
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        stripeMock.resetAll();
        stubStripeFailureThenSuccess();
    }

    @AfterAll
    static void tearDown() {
        stripeMock.stop();
    }

    @DisplayName("Should reuse existing order and payment when Stripe checkout is retried")
    @Test
    void shouldReuseExistingOrderAndPayment_WhenStripeCheckoutIsRetried()
            throws Exception {
        // given
        String token = tokenFor(USER_EMAIL);
        Long offerId = firstOfferId();
        ObjectNode request = createOrderRequest(offerId, VISIT_DATE);

        // when then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
        )
                .isInstanceOf(ServletException.class)
                .hasRootCauseInstanceOf(
                        HttpServerErrorException.InternalServerError.class
                );

        assertThat(countOrders()).isEqualTo(1);
        assertThat(countPayments()).isEqualTo(1);
        assertThat(countAppointmentSlots()).isEqualTo(1);
        assertThat(countIdempotencyRequests("RESOURCE_CREATED")).isEqualTo(1);

        Long paymentId = paymentId();
        Long existingOrderId = existingOrderId();

        assertThat(paymentCheckoutSessionId(paymentId)).isNull();
        assertThat(idempotencyCheckoutUrl()).isNull();

        // when
        MvcResult retryResult = mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("KARTA_ONLINE"))
                .andExpect(jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"))
                .andExpect(jsonPath("$.checkoutUrl").value(CHECKOUT_URL))
                .andReturn();

        // then
        assertThat(orderId(retryResult)).isEqualTo(existingOrderId);

        assertThat(countOrders()).isEqualTo(1);
        assertThat(countPayments()).isEqualTo(1);
        assertThat(countAppointmentSlots()).isEqualTo(1);

        assertThat(countIdempotencyRequests("RESOURCE_CREATED")).isZero();
        assertThat(countIdempotencyRequests("COMPLETED")).isEqualTo(1);

        assertThat(paymentCheckoutSessionId(paymentId)).isEqualTo(SESSION_ID);
        assertThat(idempotencyCheckoutUrl()).isEqualTo(CHECKOUT_URL);

        stripeMock.verify(
                2,
                postRequestedFor(urlEqualTo(CHECKOUT_ENDPOINT))
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer " + STRIPE_SECRET_KEY)
                        )
                        .withHeader(
                                "Idempotency-Key",
                                equalTo("checkout-session-payment-" + paymentId)
                        )
        );
    }

    private void stubStripeFailureThenSuccess() {
        stripeMock.stubFor(
                WireMock.post(urlEqualTo(CHECKOUT_ENDPOINT))
                        .inScenario(STRIPE_SCENARIO)
                        .whenScenarioStateIs(Scenario.STARTED)
                        .willSetStateTo(STRIPE_FAILED_ONCE)
                        .willReturn(
                                aResponse()
                                        .withStatus(500)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                  "error": {
                                                    "message": "Stripe temporary error"
                                                  }
                                                }
                                                """
                                        )
                        )
        );

        stripeMock.stubFor(
                WireMock.post(urlEqualTo(CHECKOUT_ENDPOINT))
                        .inScenario(STRIPE_SCENARIO)
                        .whenScenarioStateIs(STRIPE_FAILED_ONCE)
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(
                                                """
                                                {
                                                  "id": "cs_test_retry_123",
                                                  "url": "https://checkout.stripe.com/c/pay/cs_test_retry_123"
                                                }
                                                """
                                        )
                        )
        );
    }

    private ObjectNode createOrderRequest(
            Long offerId,
            LocalDateTime visitDate
    ) {
        return objectMapper.createObjectNode()
                .put("idOffer", offerId)
                .put("visitDate", visitDate.toString())
                .put("paymentMethod", "KARTA_ONLINE");
    }

    private String tokenFor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return jwtService.generateAccessToken(user);
    }

    private Long firstOfferId() {
        return jdbcTemplate.queryForObject(
                "SELECT MIN(id_offer) FROM offer",
                Long.class
        );
    }

    private long orderId(MvcResult result) throws Exception {
        return objectMapper.readTree(
                        result.getResponse().getContentAsString()
                )
                .get("orderId")
                .asLong();
    }

    private Long existingOrderId() {
        return jdbcTemplate.queryForObject(
                """
                SELECT o.id_order
                FROM user_order o
                INNER JOIN user u
                    ON u.id_user = o.id_user
                WHERE u.email = ?
                  AND o.visit_date = ?
                """,
                Long.class,
                USER_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private Long paymentId() {
        return jdbcTemplate.queryForObject(
                """
                SELECT p.id_payment
                FROM payment p
                INNER JOIN user_order o
                    ON o.id_order = p.id_order
                INNER JOIN user u
                    ON u.id_user = o.id_user
                WHERE u.email = ?
                  AND o.visit_date = ?
                """,
                Long.class,
                USER_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private String paymentCheckoutSessionId(Long paymentId) {
        return jdbcTemplate.queryForObject(
                """
                SELECT stripe_checkout_session_id
                FROM payment
                WHERE id_payment = ?
                """,
                String.class,
                paymentId
        );
    }

    private String idempotencyCheckoutUrl() {
        return jdbcTemplate.queryForObject(
                """
                SELECT checkout_url
                FROM idempotency_request
                WHERE idempotency_key = ?
                """,
                String.class,
                IDEMPOTENCY_KEY
        );
    }

    private int countOrders() {
        return count(
                """
                SELECT COUNT(*)
                FROM user_order o
                INNER JOIN user u
                    ON u.id_user = o.id_user
                WHERE u.email = ?
                  AND o.visit_date = ?
                """,
                USER_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private int countPayments() {
        return count(
                """
                SELECT COUNT(*)
                FROM payment p
                INNER JOIN user_order o
                    ON o.id_order = p.id_order
                INNER JOIN user u
                    ON u.id_user = o.id_user
                WHERE u.email = ?
                  AND o.visit_date = ?
                """,
                USER_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private int countAppointmentSlots() {
        return count(
                """
                SELECT COUNT(*)
                FROM appointment_slot
                WHERE visit_date = ?
                """,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private int countIdempotencyRequests(String requestStatus) {
        return count(
                """
                SELECT COUNT(*)
                FROM idempotency_request
                WHERE idempotency_key = ?
                  AND status = ?
                """,
                IDEMPOTENCY_KEY,
                requestStatus
        );
    }

    private int count(String sql, Object... arguments) {
        Integer result = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                arguments
        );

        return result != null ? result : 0;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}