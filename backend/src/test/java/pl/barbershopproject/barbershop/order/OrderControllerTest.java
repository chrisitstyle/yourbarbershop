package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.client.autoconfigure.servlet.OAuth2ClientWebSecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import pl.barbershopproject.barbershop.config.JwtAuthFilter;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.utils.TestEntities;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;

@WebMvcTest(controllers = OrderController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        })
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @MockitoBean
    private OrderService orderService;
    @MockitoBean
    private JwtService jwtService;
    @MockitoBean
    private JwtAuthFilter jwtAuthFilter;

    @Test
    void addOrder_ReturnsCreatedString() throws Exception {
        // given
        Order order = TestEntities.createOrder();

        Order savedOrder = TestEntities.orderBuilder()
                .idOrder(10L)
                .build();

        Mockito.when(orderService.addOrder(Mockito.any(Order.class))).thenReturn(savedOrder);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(order)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.content().string("Wizyta została dodana. ID Wizyty: 10"));
    }

    @Test
    void getAllOrders_ReturnsAllOrders_WhenNoStatusParam() throws Exception {
        // given
        OrderDTO orderDTO = TestEntities.createOrderDTO();

        Mockito.when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].idOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user.firstname").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].offer.kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("NOWE"));
    }

    @Test
    void getAllOrders_ReturnsFilteredOrders_WhenStatusParamProvided() throws Exception {
        // given
        OrderDTO orderDTO = TestEntities.createOrderDTO();
        String statusParam = "NOWE";

        Mockito.when(orderService.getOrdersByStatus(statusParam)).thenReturn(List.of(orderDTO));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .queryParam("status", statusParam))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].status").value("NOWE"));

        Mockito.verify(orderService).getOrdersByStatus(statusParam);
    }

    @Test
    void getSingleOrder_ReturnsOrderDTO() throws Exception {
        // given
        OrderDTO orderDTO = TestEntities.createOrderDTO();

        Mockito.when(orderService.getSingleOrder(1L)).thenReturn(orderDTO);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/1"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.user.email").value("johndoe@example.com"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.offer.cost").value(120.0));
    }

    @Test
    void updateOrder_ReturnsUpdatedOrder() throws Exception {
        // given
        Order inputOrder = TestEntities.createOrder();

        Order updatedOrder = TestEntities.orderBuilder()
                .idOrder(10L)
                .build();

        Mockito.when(orderService.updateOrder(Mockito.any(Order.class), Mockito.eq(10L)))
                .thenReturn(updatedOrder);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.put("/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputOrder)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idOrder").value(10L));
    }

    @Test
    void deleteOrderById_ReturnsNoContent() throws Exception {
        // given
        Mockito.doNothing().when(orderService).deleteOrderById(10L);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/10"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getSingleOrder_ReturnsNotFound_WhenNoSuchElementException() throws Exception {
        // given
        Mockito.when(orderService.getSingleOrder(99L))
                .thenThrow(new NoSuchElementException("Order not found"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Order not found"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("NOT_FOUND"));
    }

    @Test
    void addOrder_ReturnsBadRequest_WhenIllegalArgumentException() throws Exception {
        // given
        Order invalidOrder = TestEntities.createOrder();

        Mockito.when(orderService.addOrder(Mockito.any(Order.class)))
                .thenThrow(new IllegalArgumentException("Invalid date"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidOrder)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid date"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("BAD_REQUEST"));
    }
}
