package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.Test;
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
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.utils.TestClockConfig;
import pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@WebMvcTest(
        controllers = OrderController.class,
        excludeAutoConfiguration = {
                OAuth2ClientWebSecurityAutoConfiguration.class
        }
)
@AutoConfigureMockMvc(addFilters = false)
@Import(TestClockConfig.class)
class OrderControllerTest {

    private static final String IDEMPOTENCY_KEY = "order-controller-test-key";
    private static final String TOO_LONG_IDEMPOTENCY_KEY = "a".repeat(256);

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
    @MockitoBean
    private StringRedisTemplate stringRedisTemplate;

    @Test
    void addOrder_ReturnsCreated() throws Exception {
        // given
        OrderCreationDTO inputDto = OrderTestEntities.createOrderCreationDTO();

        OrderCreationResponseDTO responseDTO =
                new OrderCreationResponseDTO(
                        10L,
                        PaymentMethod.GOTOWKA,
                        PaymentStatus.NIE_WYMAGANA,
                        null
                );

        when(orderService.addOrder(
                any(OrderCreationDTO.class),
                any(),
                eq(IDEMPOTENCY_KEY)
        )).thenReturn(responseDTO);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value(10L))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentMethod").value("GOTOWKA"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentStatus").value("NIE_WYMAGANA"));

        verify(orderService).addOrder(
                any(OrderCreationDTO.class),
                any(),
                eq(IDEMPOTENCY_KEY)
        );
    }

    @Test
    void addOrder_ReturnsBadRequest_WhenIdempotencyKeyIsMissing()
            throws Exception {
        // given
        OrderCreationDTO inputDto = OrderTestEntities.createOrderCreationDTO();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        verifyNoInteractions(orderService);
    }

    @Test
    void addOrder_ReturnsBadRequest_WhenIdempotencyKeyIsBlank()
            throws Exception {
        // given
        OrderCreationDTO inputDto =
                OrderTestEntities.createOrderCreationDTO();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .header("Idempotency-Key", "   ")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].fieldName")
                        .value("Idempotency-Key"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].errorMessage")
                        .value("Idempotency-Key nie może być pusty"));

        verifyNoInteractions(orderService);
    }

    @Test
    void addOrder_ReturnsBadRequest_WhenIdempotencyKeyIsTooLong()
            throws Exception {
        // given
        OrderCreationDTO inputDto =
                OrderTestEntities.createOrderCreationDTO();

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .header(
                                "Idempotency-Key",
                                TOO_LONG_IDEMPOTENCY_KEY
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].fieldName")
                        .value("Idempotency-Key"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].errorMessage")
                        .value(
                                "Idempotency-Key nie może przekraczać 255 znaków"
                        ));

        verifyNoInteractions(orderService);
    }

    @Test
    void getAllOrders_ReturnsAllOrders_WhenNoStatusParam() throws Exception {
        // given
        OrderDTO orderDTO = OrderTestEntities.createOrderDTO();

        when(orderService.getAllOrders()).thenReturn(List.of(orderDTO));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders"))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].idOrder").value(1L))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].user.firstname").value("John"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].offer.kind").value("test_kind"))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderStatus").value("NOWE"));
    }

    @Test
    void getAllOrders_ReturnsFilteredOrders_WhenStatusParamProvided() throws Exception {
        // given
        OrderDTO orderDTO = OrderTestEntities.createOrderDTO();
        String statusParam = "NOWE";

        when(orderService.getOrdersByStatus(statusParam))
                .thenReturn(List.of(orderDTO));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders")
                        .queryParam("orderStatus", statusParam))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$[0].orderStatus").value("NOWE"));

        verify(orderService).getOrdersByStatus(statusParam);
    }

    @Test
    void getSingleOrder_ReturnsOrderDTO() throws Exception {
        // given
        OrderDTO orderDTO = OrderTestEntities.createOrderDTO();

        when(orderService.getSingleOrder(1L)).thenReturn(orderDTO);

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
        OrderUpdatedRequestDTO inputDto = OrderTestEntities.createOrderUpdatedRequestDTO();

        OrderDTO responseDTO = OrderTestEntities.createOrderDTO();

        when(orderService.updateOrder(
                any(OrderUpdatedRequestDTO.class),
                eq(10L)
        )).thenReturn(responseDTO);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.put("/orders/10")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(inputDto)))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.idOrder")
                        .value(responseDTO.idOrder()));

        verify(orderService).updateOrder(
                any(OrderUpdatedRequestDTO.class),
                eq(10L)
        );
    }

    @Test
    void deleteOrderById_ReturnsNoContent() throws Exception {
        // given
        doNothing().when(orderService).deleteOrderById(10L);

        // when then
        mockMvc.perform(MockMvcRequestBuilders.delete("/orders/10"))
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    void getSingleOrder_ReturnsNotFound_WhenNoSuchElementException()
            throws Exception {
        // given
        when(orderService.getSingleOrder(99L))
                .thenThrow(new NoSuchElementException(
                        "Zamówienie o ID: 99 nie istnieje"
                ));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.get("/orders/99"))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Zamówienie o ID: 99 nie istnieje"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status")
                        .value("NOT_FOUND"));
    }

    @Test
    void addOrder_ReturnsBadRequest_WhenIllegalArgumentException()
            throws Exception {
        // given
        OrderCreationDTO invalidDto = OrderTestEntities.createOrderCreationDTO();

        when(orderService.addOrder(
                any(OrderCreationDTO.class),
                any(),
                eq(IDEMPOTENCY_KEY)
        )).thenThrow(new IllegalArgumentException("Nieprawidłowa data wizyty"));

        // when then
        mockMvc.perform(MockMvcRequestBuilders.post("/orders")
                        .header("Idempotency-Key", IDEMPOTENCY_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message")
                        .value("Nieprawidłowa data wizyty"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.status")
                        .value("BAD_REQUEST"));
    }
}