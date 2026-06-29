package pl.barbershopproject.barbershop.integration.appointment;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.appointment.AppointmentSlotRepository;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.guestorder.GuestOrderRepository;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.LocalDateTime;
import java.time.Month;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class BookingConflictIntegrationTest extends BaseIntegrationTest {

    private static final LocalDateTime FUTURE_VISIT_DATE =
            LocalDateTime.of(2030, Month.JANUARY, 15, 10, 0);

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

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private GuestOrderRepository guestOrderRepository;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();

        cleanBookingData();
    }

    @AfterEach
    void tearDown() {
        cleanBookingData();
    }

    @DisplayName("Should reject guest order when guest order already reserved the same visit date")
    @Test
    void shouldRejectGuestOrder_WhenGuestOrderAlreadyReservedSameVisitDate() throws Exception {
        // given
        LocalDateTime visitDate = futureVisitDate();
        Long offerId = firstOfferId();

        ObjectNode firstGuestOrder = createGuestOrderRequest(
                "GuestJohn",
                "GuestDoe",
                "guestjohn@example.com",
                offerId,
                visitDate
        );

        ObjectNode secondGuestOrder = createGuestOrderRequest(
                "GuestJane",
                "GuestSmith",
                "guestjane@example.com",
                offerId,
                visitDate
        );

        // when + then
        mockMvc.perform(post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstGuestOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestOrderId").exists())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));

        mockMvc.perform(post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondGuestOrder)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Termin " + visitDate + " jest już zajęty"))
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        assertEquals(1, guestOrderRepository.count());
        assertEquals(0, orderRepository.count());
        assertEquals(1, appointmentSlotRepository.count());
    }

    @DisplayName("Should reject user order when guest order already reserved the same visit date")
    @Test
    void shouldRejectUserOrder_WhenGuestOrderAlreadyReservedSameVisitDate() throws Exception {
        // given
        LocalDateTime visitDate = futureVisitDate();
        Long offerId = firstOfferId();
        String token = createJwtTokenForUser("johndoe@example.com");

        ObjectNode guestOrder = createGuestOrderRequest(
                "GuestJohn",
                "GuestDoe",
                "guestjohn@example.com",
                offerId,
                visitDate
        );

        ObjectNode userOrder = createUserOrderRequest(offerId, visitDate);

        // when + then
        mockMvc.perform(post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.guestOrderId").exists())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userOrder)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Termin " + visitDate + " jest już zajęty"))
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        assertEquals(1, guestOrderRepository.count());
        assertEquals(0, orderRepository.count());
        assertEquals(1, appointmentSlotRepository.count());
    }

    @DisplayName("Should reject guest order when user order already reserved the same visit date")
    @Test
    void shouldRejectGuestOrder_WhenUserOrderAlreadyReservedSameVisitDate() throws Exception {
        // given
        LocalDateTime visitDate = futureVisitDate();
        Long offerId = firstOfferId();
        String token = createJwtTokenForUser("johndoe@example.com");

        ObjectNode userOrder = createUserOrderRequest(offerId, visitDate);

        ObjectNode guestOrder = createGuestOrderRequest(
                "GuestJohn",
                "GuestDoe",
                "guestjohn@example.com",
                offerId,
                visitDate
        );

        // when + then
        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));

        mockMvc.perform(post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(guestOrder)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Termin " + visitDate + " jest już zajęty"))
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        assertEquals(0, guestOrderRepository.count());
        assertEquals(1, orderRepository.count());
        assertEquals(1, appointmentSlotRepository.count());
    }

    @DisplayName("Should reject second user order when user order already reserved the same visit date")
    @Test
    void shouldRejectUserOrder_WhenUserOrderAlreadyReservedSameVisitDate() throws Exception {
        // given
        LocalDateTime visitDate = futureVisitDate();
        Long offerId = firstOfferId();
        String token = createJwtTokenForUser("johndoe@example.com");

        ObjectNode firstUserOrder = createUserOrderRequest(offerId, visitDate);
        ObjectNode secondUserOrder = createUserOrderRequest(offerId, visitDate);

        // when + then
        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(firstUserOrder)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderId").exists())
                .andExpect(jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));

        mockMvc.perform(post("/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(secondUserOrder)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Termin " + visitDate + " jest już zajęty"))
                .andExpect(jsonPath("$.status").value("CONFLICT"));

        assertEquals(0, guestOrderRepository.count());
        assertEquals(1, orderRepository.count());
        assertEquals(1, appointmentSlotRepository.count());
    }

    private ObjectNode createUserOrderRequest(Long offerId, LocalDateTime visitDate) {
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

    private String createJwtTokenForUser(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow();

        return jwtService.generateAccessToken(user);
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

    private void cleanBookingData() {
        guestOrderRepository.deleteAll();
        orderRepository.deleteAll();
        appointmentSlotRepository.deleteAll();
    }
}