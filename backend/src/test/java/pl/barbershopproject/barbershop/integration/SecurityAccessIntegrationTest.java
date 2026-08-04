package pl.barbershopproject.barbershop.integration;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.SqlConfig;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.Month;

import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@Sql(scripts = "/sql/security-access-cleanup.sql",
        executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD,
        config = @SqlConfig(transactionMode = SqlConfig.TransactionMode.ISOLATED)
)
class SecurityAccessIntegrationTest extends BaseIntegrationTest {

    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String USER_EMAIL = "johndoe@example.com";
    private static final LocalDateTime FUTURE_VISIT_DATE = LocalDateTime
            .of(2030, Month.JANUARY, 16, 12, 0);

    private static final String ORDER_IDEMPOTENCY_KEY = "security-order-test-key";
    private static final String GUEST_ORDER_IDEMPOTENCY_KEY = "security-guest-order-test-key";

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @DisplayName("Should allow anonymous access to public endpoints")
    @Test
    void shouldAllowAnonymousAccessToPublicEndpoints() throws Exception {
        // given
        Long offerId = firstOfferId();

        ObjectNode guestOrderData = createGuestOrderRequest(
                "GuestJohn",
                "GuestDoe",
                "guestjohn.security@example.com",
                offerId,
                futureVisitDate()
        );

        // when + then
        mockMvc.perform(get("/offers"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(post("/guestorders")
                        .header("Idempotency-Key", GUEST_ORDER_IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestOrderData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestOrderId").exists())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("NIE_WYMAGANA"));
    }

    @DisplayName("Should return unauthorized when anonymous user accesses protected endpoints")
    @Test
    void shouldReturnUnauthorized_WhenAnonymousUserAccessesProtectedEndpoints() throws Exception {
        // given
        Long userId = userIdByEmail(USER_EMAIL);
        Long offerId = firstOfferId();

        ObjectNode orderData = createOrderRequest(offerId, futureVisitDate());

        // when + then
        mockMvc.perform(get("/users"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/users/{idUser}", userId))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/orders"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/guestorders"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isUnauthorized());
    }

    @DisplayName("Should return forbidden when regular user accesses admin-only endpoints")
    @Test
    void shouldReturnForbidden_WhenUserAccessesAdminOnlyEndpoints() throws Exception {
        // given
        String userToken = tokenFor(USER_EMAIL);
        Long userId = userIdByEmail(USER_EMAIL);
        Long offerId = firstOfferId();

        ObjectNode offerData = createOfferRequest();

        // when + then
        mockMvc.perform(get("/users")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/users/{idUser}", userId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/orders")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/guestorders")
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/offers")
                        .header("Authorization", bearer(userToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerData)))
                .andExpect(status().isForbidden());

        mockMvc.perform(delete("/offers/{idOffer}", offerId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isForbidden());
    }

    @DisplayName("Should allow admin access to admin-only read endpoints")
    @Test
    void shouldAllowAdminAccessToAdminOnlyReadEndpoints() throws Exception {
        // given
        String adminToken = tokenFor(ADMIN_EMAIL);

        // when + then
        mockMvc.perform(get("/users")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/orders")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());

        mockMvc.perform(get("/guestorders")
                        .header("Authorization", bearer(adminToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @DisplayName("Should allow admin to create offer")
    @Test
    void shouldAllowAdminToCreateOffer() throws Exception {
        // given
        String adminToken = tokenFor(ADMIN_EMAIL);
        ObjectNode offerData = createOfferRequest();

        // when + then
        mockMvc.perform(post("/offers")
                        .header("Authorization", bearer(adminToken))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idOffer").exists())
                .andExpect(jsonPath("$.kind").value("Security Test Service"))
                .andExpect(jsonPath("$.cost").value(99.99));
    }

    @DisplayName("Should allow authenticated user to access single user endpoint")
    @Test
    void shouldAllowAuthenticatedUserToAccessSingleUserEndpoint() throws Exception {
        // given
        String userToken = tokenFor(USER_EMAIL);
        Long userId = userIdByEmail(USER_EMAIL);

        // when + then
        mockMvc.perform(get("/users/{idUser}", userId)
                        .header("Authorization", bearer(userToken)))
                .andExpect(status().isOk());
    }

    @DisplayName("Should allow authenticated user to create order")
    @Test
    void shouldAllowAuthenticatedUserToCreateOrder() throws Exception {
        // given
        String userToken = tokenFor(USER_EMAIL);
        Long offerId = firstOfferId();

        ObjectNode orderData = createOrderRequest(offerId, futureVisitDate());

        // when + then
        mockMvc.perform(post("/orders")
                        .header("Authorization", bearer(userToken))
                        .header("Idempotency-Key", ORDER_IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(orderData)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("NIE_WYMAGANA"));
    }

    private ObjectNode createOfferRequest() {
        return objectMapper.createObjectNode()
                .put("kind", "Security Test Service")
                .put("cost", 99.99);
    }

    private ObjectNode createOrderRequest(Long offerId, LocalDateTime visitDate) {
        return objectMapper.createObjectNode()
                .put("idOffer", offerId)
                .put("visitDate", visitDate.toString())
                .put("paymentMethod", "GOTOWKA");
    }

    private ObjectNode createGuestOrderRequest(
            String firstname,
            String lastname,
            String email,
            Long offerId,
            LocalDateTime visitDate
    ) {
        return objectMapper.createObjectNode()
                .put("firstname", firstname)
                .put("lastname", lastname)
                .put("phonenumber", "123456789")
                .put("email", email)
                .put("idOffer", offerId)
                .put("visitDate", visitDate.toString())
                .put("paymentMethod", "GOTOWKA");
    }

    private String tokenFor(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return jwtService.generateAccessToken(user);
    }

    private Long userIdByEmail(String email) {
        return userRepository.findByEmail(email)
                .map(User::getIdUser)
                .orElseThrow();
    }

    private Long firstOfferId() {
        Offer offer = offerRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        return offer.getIdOffer();
    }

    private LocalDateTime futureVisitDate() {
        return FUTURE_VISIT_DATE;
    }

    private String bearer(String token) {
        return "Bearer " + token;
    }
}