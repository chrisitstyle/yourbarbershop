package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderDTOMapper;
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.TestClockConfig;
import pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;

@WebMvcTest(controllers = GuestOrderController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
@Import(TestClockConfig.class)
class GuestOrderControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private GuestOrderService guestOrderService;

    @MockitoBean
    private JwtService jwtService;

    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void addGuestOrder_ReturnsCreated() throws Exception {
        GuestOrderCreationDTO inputDto = GuestOrderTestEntities.createGuestOrderCreationDTO();
        GuestOrderCreationResponseDTO responseDTO =
                GuestOrderTestEntities.createGuestOrderCreationResponseDTO();

        Mockito.when(guestOrderService.addGuestOrder(Mockito.any(GuestOrderCreationDTO.class)))
                .thenReturn(responseDTO);

        mockMvc.perform(MockMvcRequestBuilders.post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.guestOrderId").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));
    }

    @Test
    void getAllGuestOrders_ReturnsAll_WhenNoStatusParam() throws Exception {
        // given
        GuestOrderDTO guestOrderDTO = GuestOrderTestEntities.createGuestOrderDTO();
        Mockito.when(guestOrderService.getAllGuestOrders()).thenReturn(List.of(guestOrderDTO));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstname").value("GuestJohn"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].paymentMethod").value("GOTOWKA"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));

        Mockito.verify(guestOrderService).getAllGuestOrders();
    }

    @Test
    void getAllGuestOrders_ReturnsFiltered_WhenStatusParamProvided() throws Exception {
        // given
        GuestOrderDTO guestOrderDTO = GuestOrderTestEntities.createGuestOrderDTO();
        Status statusParam = Status.NOWE;

        Mockito.when(guestOrderService.getGuestOrdersByStatus(statusParam))
                .thenReturn(List.of(guestOrderDTO));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders")
                        .queryParam("status", "NOWE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("NOWE"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].paymentMethod").value("GOTOWKA"));

        Mockito.verify(guestOrderService).getGuestOrdersByStatus(statusParam);
    }

    @Test
    void getGuestOrder_ReturnsOrder_WhenExists() throws Exception {
        // given
        GuestOrderDTO guestOrderDTO = GuestOrderTestEntities.createGuestOrderDTO();

        Mockito.when(guestOrderService.getGuestOrder(1L)).thenReturn(guestOrderDTO);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idGuestOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastname").value("GuestDoe"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentStatus").value("OCZEKUJE_NA_PLATNOSC"));
    }

    @Test
    void updateGuestOrder_ReturnsUpdatedOrder() throws Exception {
        // given
        GuestOrderUpdateRequestDTO inputDto = GuestOrderTestEntities.createGuestOrderUpdateRequestDTO();

        GuestOrder updatedOrder = GuestOrderTestEntities.guestOrderBuilder()
                .firstname("updated_firstname")
                .build();

        GuestOrderDTO responseDTO = GuestOrderDTOMapper.toDTO(updatedOrder);

        Mockito.when(guestOrderService.updateGuestOrder(
                        Mockito.any(GuestOrderUpdateRequestDTO.class),
                        Mockito.eq(1L)
                ))
                .thenReturn(responseDTO);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.put("/guestorders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idGuestOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstname").value("updated_firstname"));
    }

    @Test
    void deleteGuestOrderById_ReturnsNoContent() throws Exception {
        // given
        Mockito.doNothing().when(guestOrderService).deleteGuestOrderById(1L);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.delete("/guestorders/1"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getGuestOrder_ReturnsNotFound_WhenNoSuchElementException() throws Exception {
        // given
        Mockito.when(guestOrderService.getGuestOrder(99L))
                .thenThrow(new NoSuchElementException("Nie znaleziono zamówienia"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Nie znaleziono zamówienia"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void addGuestOrder_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        // given
        GuestOrderCreationDTO invalidDto = GuestOrderTestEntities.createGuestOrderCreationDTO();

        Mockito.when(guestOrderService.addGuestOrder(Mockito.any(GuestOrderCreationDTO.class)))
                .thenThrow(new IllegalArgumentException("Nieprawidłowy numer telefonu"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Nieprawidłowy numer telefonu"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"));
    }
}