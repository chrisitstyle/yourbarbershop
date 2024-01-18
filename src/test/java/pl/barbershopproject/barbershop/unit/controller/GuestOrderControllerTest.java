package pl.barbershopproject.barbershop.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.barbershopproject.barbershop.controller.GuestOrderController;
import pl.barbershopproject.barbershop.model.GuestOrder;
import pl.barbershopproject.barbershop.service.GuestOrderService;

import java.util.Arrays;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GuestOrderControllerTest {

    @Mock
    private GuestOrderService guestOrderService;

    @InjectMocks
    private GuestOrderController guestOrderController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addGuestOrderSuccessTest() {
        GuestOrder guestOrder = new GuestOrder();
        when(guestOrderService.addGuestOrder(any(GuestOrder.class))).thenReturn(ResponseEntity.ok("Wizyta gościa została zarejestrowana"));

        ResponseEntity<String> responseEntity = guestOrderController.addGuestOrder(guestOrder);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Wizyta gościa została zarejestrowana", responseEntity.getBody());
    }

    @Test
    void addGuestOrderFailureTest() {
        GuestOrder guestOrder = new GuestOrder();
        when(guestOrderService.addGuestOrder(any(GuestOrder.class))).thenThrow(new RuntimeException("Wystapil blad"));

        ResponseEntity<String> responseEntity = guestOrderController.addGuestOrder(guestOrder);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Nie udało się dodać Zamówienia.", responseEntity.getBody());
    }

    @Test
    void getAllGuestOrdersTest() {
        List<GuestOrder> guestOrders = Arrays.asList(new GuestOrder(), new GuestOrder());
        when(guestOrderService.getAllGuestOrders()).thenReturn(guestOrders);

        List<GuestOrder> result = guestOrderController.getAllGuestOrders();

        assertEquals(guestOrders, result);
    }

    @Test
    void getGuestOrderTest() {
        long guestOrderId = 1L;
        GuestOrder guestOrder = new GuestOrder();
        when(guestOrderService.getGuestOrder(guestOrderId)).thenReturn(guestOrder);

        GuestOrder result = guestOrderController.getGuestOrder(guestOrderId);

        assertEquals(guestOrder, result);
    }

    @Test
    void updateGuestOrderTest() {
        long guestOrderId = 1L;
        GuestOrder updatedGuestOrder = new GuestOrder();
        when(guestOrderService.updateGuestOrder(any(GuestOrder.class), eq(guestOrderId))).thenReturn(ResponseEntity.ok(updatedGuestOrder));

        ResponseEntity<GuestOrder> result = guestOrderController.updateGuestOrder(updatedGuestOrder, guestOrderId);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(updatedGuestOrder, result.getBody());
    }

    @Test
    void deleteGuestOrderByIdSuccessTest() {
        long guestOrderId = 1L;
        doNothing().when(guestOrderService).deleteGuestOrderById(guestOrderId);

        ResponseEntity<String> responseEntity = guestOrderController.deleteGuestOrderById(guestOrderId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Zamówienie o ID 1 zostało usunięte.", responseEntity.getBody());
    }

    @Test
    void deleteGuestOrderByIdNotFoundTest() {
        long guestOrderId = 1L;
        doThrow(new NoSuchElementException("Zamowienie goscia nie zostalo znalezione")).when(guestOrderService).deleteGuestOrderById(guestOrderId);

        ResponseEntity<String> responseEntity = guestOrderController.deleteGuestOrderById(guestOrderId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Zamowienie goscia nie zostalo znalezione", responseEntity.getBody());
    }

}
