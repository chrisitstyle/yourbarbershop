package pl.barbershopproject.barbershop.integration.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Sql(
        scripts = "/sql/guest-order-idempotency-cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class GuestOrderIdempotencyIntegrationTest extends BaseIntegrationTest {

    private static final String REPLAY_IDEMPOTENCY_KEY =
            "guest-order-idempotency-replay-test-key";

    private static final String CONFLICT_IDEMPOTENCY_KEY =
            "guest-order-idempotency-conflict-test-key";

    private static final String GUEST_EMAIL =
            "guest.idempotency@example.com";

    private static final LocalDateTime VISIT_DATE =
            LocalDateTime.of(2031, Month.JANUARY, 16, 12, 0);

    private static final LocalDateTime CHANGED_VISIT_DATE =
            LocalDateTime.of(2031, Month.JANUARY, 16, 13, 0);

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Should return the same guest order when the request is retried with the same idempotency key")
    @Test
    void shouldReturnSameGuestOrder_WhenRequestIsRetriedWithSameKey() throws Exception {
        // given
        Long offerId = firstOfferId();
        ObjectNode request = createGuestOrderRequest(offerId, VISIT_DATE);

        // when
        MvcResult firstResult = mockMvc.perform(post("/guestorders")
                        .header("Idempotency-Key", REPLAY_IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("NIE_WYMAGANA"))
                .andReturn();

        MvcResult secondResult = mockMvc.perform(post("/guestorders")
                        .header("Idempotency-Key", REPLAY_IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("NIE_WYMAGANA"))
                .andReturn();

        // then
        long firstGuestOrderId = guestOrderId(firstResult);
        long secondGuestOrderId = guestOrderId(secondResult);

        assertThat(secondGuestOrderId).isEqualTo(firstGuestOrderId);

        assertThat(countGuestOrders()).isEqualTo(1);
        assertThat(countGuestOrderPayments()).isEqualTo(1);
        assertThat(countAppointmentSlots(VISIT_DATE)).isEqualTo(1);
        assertThat(countIdempotencyRequests(REPLAY_IDEMPOTENCY_KEY)).isEqualTo(1);
        assertThat(countCompletedIdempotencyRequests(REPLAY_IDEMPOTENCY_KEY)).isEqualTo(1);
    }

    @DisplayName("Should reject a different guest order request using the same idempotency key")
    @Test
    void shouldReturnConflict_WhenSameKeyIsUsedWithDifferentRequest() throws Exception {
        // given
        Long offerId = firstOfferId();

        ObjectNode originalRequest = createGuestOrderRequest(
                offerId,
                VISIT_DATE
        );

        ObjectNode changedRequest = createGuestOrderRequest(
                offerId,
                CHANGED_VISIT_DATE
        );

        mockMvc.perform(post("/guestorders")
                        .header("Idempotency-Key", CONFLICT_IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(originalRequest)))
                .andExpect(status().isCreated());

        // when then
        mockMvc.perform(post("/guestorders")
                        .header("Idempotency-Key", CONFLICT_IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changedRequest)))
                .andExpect(status().isConflict());

        assertThat(countGuestOrders()).isEqualTo(1);
        assertThat(countGuestOrderPayments()).isEqualTo(1);
        assertThat(countAppointmentSlots(VISIT_DATE)).isEqualTo(1);
        assertThat(countAppointmentSlots(CHANGED_VISIT_DATE)).isZero();
        assertThat(countIdempotencyRequests(CONFLICT_IDEMPOTENCY_KEY)).isEqualTo(1);
        assertThat(countCompletedIdempotencyRequests(CONFLICT_IDEMPOTENCY_KEY)).isEqualTo(1);
    }

    private ObjectNode createGuestOrderRequest(
            Long offerId,
            LocalDateTime visitDate
    ) {
        return objectMapper.createObjectNode()
                .put("firstname", "Guest")
                .put("lastname", "Idempotency")
                .put("phonenumber", "123456789")
                .put("email", GUEST_EMAIL)
                .put("idOffer", offerId)
                .put("visitDate", visitDate.toString())
                .put("paymentMethod", "GOTOWKA");
    }

    private long guestOrderId(MvcResult result) throws Exception {
        return objectMapper.readTree(
                        result.getResponse().getContentAsString()
                )
                .get("guestOrderId")
                .asLong();
    }

    private Long firstOfferId() {
        return jdbcTemplate.queryForObject(
                "SELECT MIN(id_offer) FROM offer",
                Long.class
        );
    }

    private int countGuestOrders() {
        return count(
                "SELECT COUNT(*) FROM guest_order WHERE email = ?",
                GUEST_EMAIL
        );
    }

    private int countGuestOrderPayments() {
        return count(
                """
                SELECT COUNT(*)
                FROM payment p
                INNER JOIN guest_order g
                    ON g.id_guest_order = p.id_guest_order
                WHERE g.email = ?
                """,
                GUEST_EMAIL
        );
    }

    private int countAppointmentSlots(LocalDateTime visitDate) {
        return count(
                "SELECT COUNT(*) FROM appointment_slot WHERE visit_date = ?",
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

    private int countCompletedIdempotencyRequests(String idempotencyKey) {
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
}
