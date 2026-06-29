package pl.barbershopproject.barbershop.offer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.offer.dto.OfferCreationDTO;
import pl.barbershopproject.barbershop.offer.dto.UpdateOfferDTO;
import pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities;
import tools.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;

@WebMvcTest(controllers = OfferController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
class OfferControllerTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-16T12:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private OfferService offerService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @MockitoBean
    private Clock clock;

    @BeforeEach
    void setUp() {
        Mockito.when(clock.getZone()).thenReturn(TEST_ZONE);
        Mockito.when(clock.instant()).thenReturn(TEST_INSTANT);
    }

    @Test
    void addOffer_ReturnsSavedOffer() throws Exception {

        OfferCreationDTO offerCreationDTO = new OfferCreationDTO(
                "test_kind",
                BigDecimal.valueOf(120)
        );

        Offer savedOffer = OfferTestEntities.createOffer();

        Mockito.when(offerService.addOffer(Mockito.any(OfferCreationDTO.class))).thenReturn(savedOffer);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(offerCreationDTO)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idOffer").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.header().exists("Location"));

    }

    @Test
    void getAllOffers_ReturnsAllOffers() throws Exception {
        Offer offer = OfferTestEntities.createOffer();
        List<Offer> offersList = List.of(offer);

        Mockito.when(offerService.getAllOffers()).thenReturn(offersList);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/offers"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].idOffer").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].cost").value(120));


    }

    @Test
    void getOfferById_ReturnsOffer() throws Exception {
        Offer offer = OfferTestEntities.createOffer();

        Mockito.when(offerService.getSingleOffer(1L)).thenReturn(offer);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/offers/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idOffer").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.cost").value(120));
    }

    @Test
    void updateOffer_ReturnsUpdatedOffer() throws Exception {
        UpdateOfferDTO updateOfferDTO = new UpdateOfferDTO(
                "updated_kind",
                BigDecimal.valueOf(120)
        );

        Offer updatedOffer = OfferTestEntities.createOffer();
        updatedOffer.setKind("updated_kind");

        Mockito.when(offerService.updateOffer(Mockito.any(UpdateOfferDTO.class), Mockito.eq(1L)))
                .thenReturn(updatedOffer);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.put("/offers/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateOfferDTO)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idOffer").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.kind").value("updated_kind"));

    }

    @Test
    void deleteOfferById_ReturnsNoContent() throws Exception {
        Mockito.doNothing().when(offerService).deleteOfferById(1L);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.delete("/offers/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getSingleOffer_ReturnsNotFound_WhenNoSuchElementException() throws Exception {
        // given
        Mockito.when(offerService.getSingleOffer(99L))
                .thenThrow(new NoSuchElementException("Offer not found"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/offers/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Offer not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void addOffer_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        // given
        OfferCreationDTO invalidOffer = new OfferCreationDTO(
                "invalid_kind",
                BigDecimal.valueOf(120)
        );

        Mockito.when(offerService.addOffer(Mockito.any(OfferCreationDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid offer data"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/offers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOffer)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid offer data"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"));
    }
}