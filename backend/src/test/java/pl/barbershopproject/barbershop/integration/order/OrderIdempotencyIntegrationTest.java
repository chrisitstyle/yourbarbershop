package pl.barbershopproject.barbershop.integration.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class OrderIdempotencyIntegrationTest extends BaseIntegrationTest {

    private static final String USER_EMAIL = "johndoe@example.com";
    private static final String SECOND_USER_EMAIL =
            "second.user.idempotency@example.com";

    private static final String REPLAY_IDEMPOTENCY_KEY =
            "order-idempotency-replay-test-key";

    private static final String REQUEST_CONFLICT_IDEMPOTENCY_KEY =
            "order-idempotency-request-conflict-test-key";

    private static final String OWNER_CONFLICT_IDEMPOTENCY_KEY =
            "order-idempotency-owner-conflict-test-key";

    private static final LocalDateTime REPLAY_VISIT_DATE =
            LocalDateTime.of(2032, 1, 16, 12, 0);

    private static final LocalDateTime REQUEST_CONFLICT_VISIT_DATE =
            LocalDateTime.of(2032, 1, 17, 12, 0);

    private static final LocalDateTime CHANGED_VISIT_DATE =
            LocalDateTime.of(2032, 1, 17, 13, 0);

    private static final LocalDateTime OWNER_CONFLICT_VISIT_DATE =
            LocalDateTime.of(2032, 1, 18, 12, 0);

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

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        createSecondUserIfMissing();
    }

    @DisplayName("Should return the same order when the request is retried with the same idempotency key")
    @Test
    void shouldReturnSameOrder_WhenRequestIsRetriedWithSameKey()
            throws Exception {
        // given
        String token = tokenFor(USER_EMAIL);
        Long offerId = firstOfferId();

        ObjectNode request = createOrderRequest(
                offerId,
                REPLAY_VISIT_DATE
        );

        // when
        MvcResult firstResult = mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .header(
                                "Idempotency-Key",
                                REPLAY_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("NIE_WYMAGANA"))
                .andReturn();

        MvcResult secondResult = mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .header(
                                "Idempotency-Key",
                                REPLAY_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("NIE_WYMAGANA"))
                .andReturn();

        // then
        long firstOrderId = orderId(firstResult);
        long secondOrderId = orderId(secondResult);

        assertThat(secondOrderId).isEqualTo(firstOrderId);

        assertThat(countOrders(USER_EMAIL, REPLAY_VISIT_DATE))
                .isEqualTo(1);

        assertThat(countOrderPayments(USER_EMAIL, REPLAY_VISIT_DATE))
                .isEqualTo(1);

        assertThat(countAppointmentSlots(REPLAY_VISIT_DATE))
                .isEqualTo(1);

        assertThat(countIdempotencyRequests(REPLAY_IDEMPOTENCY_KEY))
                .isEqualTo(1);

        assertThat(countCompletedIdempotencyRequests(
                REPLAY_IDEMPOTENCY_KEY
        )).isEqualTo(1);
    }

    @DisplayName("Should reject a different order request using the same idempotency key")
    @Test
    void shouldReturnConflict_WhenSameKeyIsUsedWithDifferentRequest()
            throws Exception {
        // given
        String token = tokenFor(USER_EMAIL);
        Long offerId = firstOfferId();

        ObjectNode originalRequest = createOrderRequest(
                offerId,
                REQUEST_CONFLICT_VISIT_DATE
        );

        ObjectNode changedRequest = createOrderRequest(
                offerId,
                CHANGED_VISIT_DATE
        );

        mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .header(
                                "Idempotency-Key",
                                REQUEST_CONFLICT_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalRequest)))
                .andExpect(status().isCreated());

        // when then
        mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(token))
                        .header(
                                "Idempotency-Key",
                                REQUEST_CONFLICT_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changedRequest)))
                .andExpect(status().isConflict());

        assertThat(countOrders(
                USER_EMAIL,
                REQUEST_CONFLICT_VISIT_DATE
        )).isEqualTo(1);

        assertThat(countOrders(
                USER_EMAIL,
                CHANGED_VISIT_DATE
        )).isZero();

        assertThat(countOrderPayments(
                USER_EMAIL,
                REQUEST_CONFLICT_VISIT_DATE
        )).isEqualTo(1);

        assertThat(countAppointmentSlots(
                REQUEST_CONFLICT_VISIT_DATE
        )).isEqualTo(1);

        assertThat(countAppointmentSlots(CHANGED_VISIT_DATE))
                .isZero();

        assertThat(countIdempotencyRequests(
                REQUEST_CONFLICT_IDEMPOTENCY_KEY
        )).isEqualTo(1);
    }

    @DisplayName("Should reject the same idempotency key used by another authenticated user")
    @Test
    void shouldReturnConflict_WhenSameKeyIsUsedByDifferentUser()
            throws Exception {
        // given
        String firstUserToken = tokenFor(USER_EMAIL);
        String secondUserToken = tokenFor(SECOND_USER_EMAIL);
        Long offerId = firstOfferId();

        ObjectNode request = createOrderRequest(
                offerId,
                OWNER_CONFLICT_VISIT_DATE
        );

        mockMvc.perform(post("/orders")
                        .header(
                                "Authorization",
                                bearer(firstUserToken)
                        )
                        .header(
                                "Idempotency-Key",
                                OWNER_CONFLICT_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // when then
        mockMvc.perform(post("/orders")
                        .header(
                                "Authorization",
                                bearer(secondUserToken)
                        )
                        .header(
                                "Idempotency-Key",
                                OWNER_CONFLICT_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict());

        assertThat(countOrders(
                USER_EMAIL,
                OWNER_CONFLICT_VISIT_DATE
        )).isEqualTo(1);

        assertThat(countOrders(
                SECOND_USER_EMAIL,
                OWNER_CONFLICT_VISIT_DATE
        )).isZero();

        assertThat(countOrderPayments(
                USER_EMAIL,
                OWNER_CONFLICT_VISIT_DATE
        )).isEqualTo(1);

        assertThat(countAppointmentSlots(
                OWNER_CONFLICT_VISIT_DATE
        )).isEqualTo(1);

        assertThat(countIdempotencyRequests(
                OWNER_CONFLICT_IDEMPOTENCY_KEY
        )).isEqualTo(1);
    }

    private ObjectNode createOrderRequest(
            Long offerId,
            LocalDateTime visitDate
    ) {
        return objectMapper.createObjectNode()
                .put("idOffer", offerId)
                .put("visitDate", visitDate.toString())
                .put("paymentMethod", "GOTOWKA");
    }

    private long orderId(MvcResult result) throws Exception {
        return objectMapper.readTree(
                        result.getResponse().getContentAsString()
                )
                .get("orderId")
                .asLong();
    }

    private void createSecondUserIfMissing() {
        jdbcTemplate.update(
                """
                        INSERT INTO user (
                            firstname,
                            lastname,
                            email,
                            password,
                            role
                        )
                        SELECT ?, ?, ?, ?, ?
                        WHERE NOT EXISTS (
                            SELECT 1
                            FROM user
                            WHERE email = ?
                        )
                        """,
                "Second",
                "IdempotencyUser",
                SECOND_USER_EMAIL,
                "$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi",
                "USER",
                SECOND_USER_EMAIL
        );
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

    private int countOrders(
            String email,
            LocalDateTime visitDate
    ) {
        return count(
                """
                        SELECT COUNT(*)
                        FROM user_order o
                        INNER JOIN user u
                            ON u.id_user = o.id_user
                        WHERE u.email = ?
                          AND o.visit_date = ?
                        """,
                email,
                Timestamp.valueOf(visitDate)
        );
    }

    private int countOrderPayments(
            String email,
            LocalDateTime visitDate
    ) {
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
                email,
                Timestamp.valueOf(visitDate)
        );
    }

    private int countAppointmentSlots(LocalDateTime visitDate) {
        return count(
                """
                        SELECT COUNT(*)
                        FROM appointment_slot
                        WHERE visit_date = ?
                        """,
                Timestamp.valueOf(visitDate)
        );
    }

    private int countIdempotencyRequests(String idempotencyKey) {
        return count(
                """
                        SELECT COUNT(*)
                        FROM idempotency_request
                        WHERE idempotency_key = ?
                        """,
                idempotencyKey
        );
    }

    private int countCompletedIdempotencyRequests(
            String idempotencyKey
    ) {
        return count(
                """
                        SELECT COUNT(*)
                        FROM idempotency_request
                        WHERE idempotency_key = ?
                          AND status = 'COMPLETED'
                        """,
                idempotencyKey
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
