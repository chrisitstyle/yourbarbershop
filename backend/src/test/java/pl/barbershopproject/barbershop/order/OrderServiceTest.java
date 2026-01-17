package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.TestEntities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {


    @Mock
    private OrderRepository orderRepository;
    @InjectMocks
    private OrderService orderService;

    private Order order;
    private Order updatedOrder;
    private User user;
    private Offer offer;

    @BeforeEach
    void setUp() {
        offer = TestEntities.createOffer();
        user = TestEntities.createUser();

        order = new Order();
        order.setIdOrder(1L);
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(LocalDateTime.parse("2025-03-23T10:00:00"));
        order.setVisitDate(LocalDateTime.parse("2025-03-24T12:00:00"));
        order.setStatus(Status.NOWE);

    }

    @Test
    void addOrder_ShouldSaveOrder() {

        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order result = orderService.addOrder(order);

        assertNotNull(result);
        assertEquals(order.getIdOrder(), result.getIdOrder());
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void getAllOrders_ShouldReturnListOfOrderDTOs() {
        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<OrderDTO> result = orderService.getAllOrders();

        assertEquals(1, result.size());
        assertEquals(order.getIdOrder(), result.getFirst().idOrder());
        verify(orderRepository, times(1)).findAll();
    }

    @Test
    void getSingleOrder_ShouldReturnOrderDTO_WhenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        OrderDTO result = orderService.getSingleOrder(1L);

        assertNotNull(result);
        assertEquals(order.getIdOrder(), result.idOrder());
    }

    @Test
    void getSingleOrder_ShouldThrowException_WhenOrderNotFound() {
        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> orderService.getSingleOrder(2L));
    }

    @Test
    void getOrdersByStatus_ShouldReturnFilteredOrders() {
        List<Order> orders = List.of(order);
        when(orderRepository.findOrdersByStatus(Status.NOWE)).thenReturn(orders);

        List<OrderDTO> result = orderService.getOrdersByStatus("NOWE");

        assertEquals(1, result.size());
        assertEquals(Status.NOWE, result.getFirst().status());
    }

    @Test
    void getOrdersByStatus_ShouldThrowException_ForInvalidStatus() {
        Exception exception = assertThrows(IllegalArgumentException.class,
                () -> orderService.getOrdersByStatus("invalid"));

        assertTrue(exception.getMessage().contains("Dostępne statusy"));
    }

    @Test
    void updateOrder_ShouldUpdateExistingOrder() {
        updatedOrder = new Order();
        updatedOrder.setUser(user);
        updatedOrder.setOffer(offer);
        updatedOrder.setOrderDate(LocalDateTime.parse("2025-03-25T10:00:00"));
        updatedOrder.setVisitDate(LocalDateTime.parse("2025-03-26T10:00:00"));
        updatedOrder.setStatus(Status.NOWE);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);

        Order updatedOrderResult = orderService.updateOrder(updatedOrder, 1L);

        assertNotNull(updatedOrderResult);
        assertAll(
                () -> assertEquals(updatedOrder.getUser(), updatedOrderResult.getUser()),
                () -> assertEquals(updatedOrder.getOffer(), updatedOrderResult.getOffer()),
                () -> assertEquals(updatedOrder.getOrderDate(), updatedOrderResult.getOrderDate()),
                () -> assertEquals(updatedOrder.getVisitDate(), updatedOrderResult.getVisitDate()),
                () -> assertEquals(updatedOrder.getStatus(), updatedOrderResult.getStatus())
        );

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void updateOrder_ShouldThrowException_WhenOrderNotFound() {

        when(orderRepository.findById(2L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class,
                () -> orderService.updateOrder(updatedOrder, 2L));
    }

    @Test
    void deleteOrderById_ShouldDeleteExistingOrder() {
        when(orderRepository.existsById(1L)).thenReturn(true);

        orderService.deleteOrderById(1L);

        verify(orderRepository).deleteById(1L);
    }

    @Test
    void deleteOrderById_ShouldThrowException_WhenOrderNotExists() {
        when(orderRepository.existsById(2L)).thenReturn(false);

        assertThrows(NoSuchElementException.class,
                () -> orderService.deleteOrderById(2L));

        verify(orderRepository, never()).deleteById(anyLong());
    }


}
