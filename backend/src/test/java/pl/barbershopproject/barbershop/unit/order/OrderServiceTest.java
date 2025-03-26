package pl.barbershopproject.barbershop.unit.order;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.order.OrderService;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
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
        offer = new Offer();
        offer.setIdOffer(1L);
        offer.setKind("Test kind");
        offer.setCost(new BigDecimal("100"));

        user = new User();
        user.setIdUser(1L);
        user.setFirstname("John");
        user.setLastname("Doe");
        user.setEmail("john@doe.com");
        user.setPassword("test_password");
        user.setRole(Role.USER);

        order = new Order();
        order.setIdOrder(1L);
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(LocalDateTime.parse("2025-03-23T10:00:00"));
        order.setVisitDate(LocalDateTime.parse("2025-03-24T12:00:00"));
        order.setStatus(Status.NOWE);

    }

    @Test
    void OrderService_AddOrder_ShouldSaveOrder(){

        orderService.addOrder(order);

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    void OrderService_GetAllOrders_ShouldReturnAllOrders(){

        when(orderRepository.findAll()).thenReturn(List.of(order));

        List<Order> ordersResult = orderService.getAllOrders();


        assertEquals(order, ordersResult.getFirst());
        verify(orderRepository, times(1)).findAll();


    }


    @Test
    void OrderService_GetSingleOrder_ShouldReturnOrder_WhenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        Order orderResult = orderService.getSingleOrder(1L);

        assertNotNull(orderResult);
        assertAll(
                () -> assertEquals(order.getIdOrder(), orderResult.getIdOrder()),
                () -> assertEquals(order.getUser(), orderResult.getUser()),
                () -> assertEquals(order.getOffer(), orderResult.getOffer()),
                () -> assertEquals(order.getOrderDate(), orderResult.getOrderDate()),
                () -> assertEquals(order.getVisitDate(), orderResult.getVisitDate()),
                () -> assertEquals(order.getStatus(), orderResult.getStatus())
        );

        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void OrderService_GetSingleOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> orderService.getSingleOrder(1L));

        verify(orderRepository, times(1)).findById(1L);
    }


    @Test
    void OrderService_GetOrdersByStatus_ShouldReturnOrdersByStatus() {
        when(orderRepository.findOrdersByStatus("Test status")).thenReturn(List.of(order));

        List<Order> ordersByStatusResult = orderService.getOrdersByStatus("Test status");

        assertNotNull(ordersByStatusResult);
        assertEquals(1, ordersByStatusResult.size());

        verify(orderRepository, times(1)).findOrdersByStatus("Test status");
    }

    @Test
    void OrderService_UpdateOrder_ShouldUpdateAndReturnOrder_WhenOrderExists() {

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
    void OrderService_UpdateOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> orderService.updateOrder(updatedOrder, 1L));

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void OrderService_DeleteOrderById_ShouldDeleteOrder_WhenOrderExists() {
        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        orderService.deleteOrderById(1L);

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).deleteById(1L);
    }

    @Test
    void OrderService_DeleteOrderById_ShouldThrowException_WhenOrderDoesNotExist() {
        when(orderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> orderService.deleteOrderById(1L));

        assertEquals("Zamówienie o podanym ID nie istnieje", exception.getMessage());

        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).deleteById(1L);
    }

}
