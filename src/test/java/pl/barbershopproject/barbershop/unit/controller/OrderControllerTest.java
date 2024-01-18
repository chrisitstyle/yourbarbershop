package pl.barbershopproject.barbershop.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.barbershopproject.barbershop.controller.OrderController;
import pl.barbershopproject.barbershop.model.Order;
import pl.barbershopproject.barbershop.service.OrderService;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addOrderSuccessTest() {
        Order order = new Order();
        when(orderService.addOrder(any(Order.class))).thenReturn(ResponseEntity.ok("Zamowienie zostalo dodane"));

        ResponseEntity<String> responseEntity = orderController.addOrder(order);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Zamowienie zostalo dodane", responseEntity.getBody());
    }

    @Test
    void addOrderFailureTest() {
        Order order = new Order();
        when(orderService.addOrder(any(Order.class))).thenThrow(new RuntimeException("Wystapil blad"));

        ResponseEntity<String> responseEntity = orderController.addOrder(order);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Nie udało się dodać Zamówienia.", responseEntity.getBody());
    }

    @Test
    void getAllOrdersTest() {
        List<Order> orders = Arrays.asList(new Order(), new Order());
        when(orderService.getAllOrders()).thenReturn(orders);

        List<Order> result = orderController.getAllOrders();

        assertEquals(orders, result);
    }

    @Test
    void getSingleOrderTest() {
        long orderId = 1L;
        Order order = new Order();
        when(orderService.getSingleOrder(orderId)).thenReturn(order);

        Order result = orderController.getSingleOrder(orderId);

        assertEquals(order, result);
    }

    @Test
    void updateOrderTest() {
        long orderId = 1L;
        Order updatedOrder = new Order();
        when(orderService.updateOrder(any(Order.class), eq(orderId))).thenReturn(ResponseEntity.ok(updatedOrder));

        ResponseEntity<Order> result = orderController.updateOrder(updatedOrder, orderId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updatedOrder, result.getBody());
    }

    @Test
    void deleteOrderByIdSuccessTest() {
        long orderId = 1L;
        doNothing().when(orderService).deleteOrderById(orderId);

        ResponseEntity<String> responseEntity = orderController.deleteOrderById(orderId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Zamówienie o ID 1 zostało usunięte.", responseEntity.getBody());
    }

    @Test
    void deleteOrderByIdNotFoundTest() {
        long orderId = 1L;
        doThrow(new NoSuchElementException("Nie znaleziono zamowienia")).when(orderService).deleteOrderById(orderId);

        ResponseEntity<String> responseEntity = orderController.deleteOrderById(orderId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Nie znaleziono zamowienia", responseEntity.getBody());
    }

}
