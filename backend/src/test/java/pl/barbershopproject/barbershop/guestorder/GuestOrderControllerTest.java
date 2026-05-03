package pl.barbershopproject.barbershop.guestorder;

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
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.TestEntities;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;

@WebMvcTest(controllers = GuestOrderController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
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
        // given
        GuestOrder inputOrder = TestEntities.createGuestOrder();
        GuestOrder savedOrder = TestEntities.createGuestOrder();

        Mockito.when(guestOrderService.addGuestOrder(Mockito.any(GuestOrder.class)))
                .thenReturn(savedOrder);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrder)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.idGuestOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.firstname").value("GuestJohn"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.email").value("guestjohndoe@example.com"));
    }

    @Test
    void getAllGuestOrders_ReturnsAll_WhenNoStatusParam() throws Exception {
        // given
        GuestOrder guestOrder = TestEntities.createGuestOrder();
        Mockito.when(guestOrderService.getAllGuestOrders()).thenReturn(List.of(guestOrder));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].firstname").value("GuestJohn"));

        Mockito.verify(guestOrderService).getAllGuestOrders();
    }

    @Test
    void getAllGuestOrders_ReturnsFiltered_WhenStatusParamProvided() throws Exception {
        // given
        GuestOrder guestOrder = TestEntities.createGuestOrder();
        Status statusParam = Status.NOWE;

        Mockito.when(guestOrderService.getGuestOrdersByStatus(statusParam))
                .thenReturn(List.of(guestOrder));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders")
                        .queryParam("status", "NOWE"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("NOWE"));

        Mockito.verify(guestOrderService).getGuestOrdersByStatus(statusParam);
    }

    @Test
    void getGuestOrder_ReturnsOrder_WhenExists() throws Exception {
        // given
        GuestOrder guestOrder = TestEntities.createGuestOrder();

        Mockito.when(guestOrderService.getGuestOrder(1L)).thenReturn(guestOrder);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idGuestOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.lastname").value("GuestDoe"));
    }

    @Test
    void updateGuestOrder_ReturnsUpdatedOrder() throws Exception {
        // given
        GuestOrder inputOrder = TestEntities.createGuestOrder();

        GuestOrder updatedOrder = TestEntities.guestOrderBuilder()
                .firstname("updated_firstname")
                .build();

        Mockito.when(guestOrderService.updateGuestOrder(Mockito.any(GuestOrder.class), Mockito.eq(1L)))
                .thenReturn(updatedOrder);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.put("/guestorders/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrder)))
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
                .thenThrow(new NoSuchElementException("Guest order not found"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/guestorders/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Guest order not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void addGuestOrder_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        // given
        GuestOrder invalidOrder = TestEntities.createGuestOrder();

        Mockito.when(guestOrderService.addGuestOrder(Mockito.any(GuestOrder.class)))
                .thenThrow(new IllegalArgumentException("Invalid phone number"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/guestorders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid phone number"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"));
    }

}
