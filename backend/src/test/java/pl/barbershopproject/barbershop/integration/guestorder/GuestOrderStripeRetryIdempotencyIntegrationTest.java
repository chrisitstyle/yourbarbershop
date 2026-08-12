package pl.barbershopproject.barbershop.integration.guestorder;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.http.Fault;
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
import org.springframework.test.context.event.ApplicationEvents;
import org.springframework.test.context.event.RecordApplicationEvents;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.payment.event.OnlinePaymentPendingEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RecordApplicationEvents
class GuestOrderStripeRetryIdempotencyIntegrationTest extends BaseIntegrationTest {

    private static final String GUEST_EMAIL = "guest-payment-retry@example.com";

    private static final String IDEMPOTENCY_KEY = "guest-order-stripe-retry-idempotency-test-key";

    private static final String CHECKOUT_ENDPOINT = "/v1/checkout/sessions";

    private static final String STRIPE_SCENARIO = "guest-stripe-checkout-retry";

    private static final String CONNECTION_DROPPED = "connection-dropped";

    private static final String STRIPE_SECRET_KEY = "sk_test_integration";

    private static final String SESSION_ID = "cs_test_guest_retry_123";

    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_guest_retry_123";

    private static final LocalDateTime VISIT_DATE = LocalDateTime
            .of(2033, Month.JANUARY, 17, 12, 0);

    private static final WireMockServer stripeMock = new WireMockServer(options().dynamicPort());

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
    private ApplicationEvents applicationEvents;

    @DynamicPropertySource
    static void configureStripeProperties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
                "stripe.api-base-url",
                stripeMock::baseUrl);

        registry.add(
                "stripe.secret-key",
                () -> STRIPE_SECRET_KEY);

        registry.add(
                "stripe.success-url",
                () -> "http://localhost:3000/payment/success");

        registry.add(
                "stripe.cancel-url",
                () -> "http://localhost:3000/payment/cancel");
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        stripeMock.resetAll();
        stubStripeConnectionFailureThenSuccess();
    }

    @AfterAll
    static void tearDown() {
        stripeMock.stop();
    }

    @DisplayName(
            "Should reuse existing guest order and payment when Stripe checkout is retried after connection failure")
    @Test
    void shouldReuseExistingGuestOrderAndPayment_WhenStripeCheckoutIsRetriedAfterConnectionFailure()
            throws Exception {
        // given
        Long offerId = firstOfferId();

        ObjectNode request = createGuestOrderRequest(
                offerId,
                VISIT_DATE);

        // when then
        assertThatThrownBy(() ->
                mockMvc.perform(post("/guestorders")
                        .header(
                                "Idempotency-Key",
                                IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(
                                objectMapper.writeValueAsString(
                                        request
                                )
                        ))
        )
                .isInstanceOf(ServletException.class)
                .hasCauseInstanceOf(
                        ResourceAccessException.class
                );

        assertThat(countGuestOrders()).isEqualTo(1);
        assertThat(countPayments()).isEqualTo(1);
        assertThat(countAppointmentSlots()).isEqualTo(1);

        assertThat(
                countIdempotencyRequests(
                        "RESOURCE_CREATED")
        ).isEqualTo(1);

        Long paymentId = paymentId();
        Long existingGuestOrderId = existingGuestOrderId();

        String stripeCheckoutIdempotencyKey = stripeCheckoutIdempotencyKey(paymentId);

        assertThat(stripeCheckoutIdempotencyKey).isNotBlank();

        assertThat(paymentCheckoutSessionId(paymentId)).isNull();

        assertThat(idempotencyCheckoutUrl()).isNull();

        // when
        MvcResult retryResult = mockMvc.perform(
                        post("/guestorders")
                                .header(
                                        "Idempotency-Key",
                                        IDEMPOTENCY_KEY
                                )
                                .contentType(
                                        MediaType.APPLICATION_JSON
                                )
                                .content(
                                        objectMapper.writeValueAsString(
                                                request
                                        )
                                )
                )
                .andExpect(status().isCreated())
                .andExpect(
                        jsonPath("$.paymentMethod")
                                .value("KARTA_ONLINE")
                )
                .andExpect(
                        jsonPath("$.paymentStatus")
                                .value("OCZEKUJE_NA_PLATNOSC")
                )
                .andExpect(
                        jsonPath("$.checkoutUrl")
                                .value(CHECKOUT_URL)
                )
                .andReturn();

        // then
        assertThat(
                guestOrderId(retryResult)
        ).isEqualTo(existingGuestOrderId);

        assertThat(countGuestOrders()).isEqualTo(1);
        assertThat(countPayments()).isEqualTo(1);
        assertThat(countAppointmentSlots()).isEqualTo(1);

        assertThat(
                countIdempotencyRequests(
                        "RESOURCE_CREATED")
        ).isZero();

        assertThat(
                countIdempotencyRequests(
                        "COMPLETED")
        ).isEqualTo(1);

        assertThat(
                paymentCheckoutSessionId(paymentId)
        ).isEqualTo(SESSION_ID);

        assertThat(
                idempotencyCheckoutUrl()
        ).isEqualTo(CHECKOUT_URL);

        stripeMock.verify(
                2,
                postRequestedFor(
                        urlEqualTo(CHECKOUT_ENDPOINT))
                        .withHeader(
                                "Authorization",
                                equalTo(
                                        "Bearer "
                                                + STRIPE_SECRET_KEY))
                        .withHeader(
                                "Idempotency-Key",
                                equalTo(
                                        "checkout-session-payment-"
                                                + stripeCheckoutIdempotencyKey)));

        long paymentPendingEvents = applicationEvents.stream(
                        OnlinePaymentPendingEvent.class
                )
                .count();

        assertThat(paymentPendingEvents).isEqualTo(1);
    }

    private void stubStripeConnectionFailureThenSuccess() {
        stripeMock.stubFor(
                WireMock.post(
                                urlEqualTo(CHECKOUT_ENDPOINT)
                        )
                        .inScenario(STRIPE_SCENARIO)
                        .whenScenarioStateIs(
                                Scenario.STARTED
                        )
                        .willSetStateTo(
                                CONNECTION_DROPPED
                        )
                        .willReturn(
                                aResponse()
                                        .withFault(
                                                Fault.CONNECTION_RESET_BY_PEER
                                        )
                        )
        );

        stripeMock.stubFor(
                WireMock.post(
                                urlEqualTo(CHECKOUT_ENDPOINT)
                        )
                        .inScenario(STRIPE_SCENARIO)
                        .whenScenarioStateIs(
                                CONNECTION_DROPPED
                        )
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(
                                                """
                                                        {
                                                          "id": "cs_test_guest_retry_123",
                                                          "url": "https://checkout.stripe.com/c/pay/cs_test_guest_retry_123"
                                                        }
                                                        """
                                        )
                        )
        );
    }

    private ObjectNode createGuestOrderRequest(
            Long offerId,
            LocalDateTime visitDate
    ) {
        return objectMapper.createObjectNode()
                .put("firstname", "John")
                .put("lastname", "Doe")
                .put("phonenumber", "123456789")
                .put("email", GUEST_EMAIL)
                .put("idOffer", offerId)
                .put(
                        "visitDate",
                        visitDate.toString()
                )
                .put(
                        "paymentMethod",
                        "KARTA_ONLINE"
                );
    }

    private Long firstOfferId() {
        return jdbcTemplate.queryForObject(
                "SELECT MIN(id_offer) FROM offer",
                Long.class
        );
    }

    private long guestOrderId(
            MvcResult result
    ) throws Exception {
        return objectMapper.readTree(
                        result.getResponse()
                                .getContentAsString()
                )
                .get("guestOrderId")
                .asLong();
    }

    private Long existingGuestOrderId() {
        return jdbcTemplate.queryForObject(
                """
                        SELECT id_guest_order
                        FROM guest_order
                        WHERE email = ?
                          AND visit_date = ?
                        """,
                Long.class,
                GUEST_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private Long paymentId() {
        return jdbcTemplate.queryForObject(
                """
                        SELECT p.id_payment
                        FROM payment p
                        INNER JOIN guest_order g
                            ON g.id_guest_order = p.id_guest_order
                        WHERE g.email = ?
                          AND g.visit_date = ?
                        """,
                Long.class,
                GUEST_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private String paymentCheckoutSessionId(
            Long paymentId
    ) {
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

    private int countGuestOrders() {
        return count(
                """
                        SELECT COUNT(*)
                        FROM guest_order
                        WHERE email = ?
                          AND visit_date = ?
                        """,
                GUEST_EMAIL,
                Timestamp.valueOf(VISIT_DATE)
        );
    }

    private int countPayments() {
        return count(
                """
                        SELECT COUNT(*)
                        FROM payment p
                        INNER JOIN guest_order g
                            ON g.id_guest_order = p.id_guest_order
                        WHERE g.email = ?
                          AND g.visit_date = ?
                        """,
                GUEST_EMAIL,
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

    private int countIdempotencyRequests(
            String requestStatus
    ) {
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

    private int count(
            String sql,
            Object... arguments
    ) {
        Integer result = jdbcTemplate.queryForObject(
                sql,
                Integer.class,
                arguments
        );

        return result != null ? result : 0;
    }

    private String stripeCheckoutIdempotencyKey(
            Long paymentId
    ) {
        return jdbcTemplate.queryForObject(
                """
                        SELECT stripe_checkout_idempotency_key
                        FROM payment
                        WHERE id_payment = ?
                        """,
                String.class,
                paymentId
        );
    }
}
