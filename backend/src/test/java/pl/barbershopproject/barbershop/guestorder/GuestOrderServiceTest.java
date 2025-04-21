package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestOrderServiceTest {


    @Mock
    private GuestOrderRepository guestOrderRepository;

    @InjectMocks
    private GuestOrderService guestOrderService;


    private final GuestOrder guestOrder = new GuestOrder();
    private final Offer offer = new Offer();

    @BeforeEach
    void setUp() {

        offer.setIdOffer(1L);
        offer.setKind("Test kind");
        offer.setCost(new BigDecimal("100"));

        guestOrder.setIdGuestOrder(1L);
        guestOrder.setFirstname("John Doe");
        guestOrder.setLastname("Doe");
        guestOrder.setPhonenumber("123456789");
        guestOrder.setEmail("johndoe@test.com");
        guestOrder.setOffer(offer);
        guestOrder.setOrderDate(LocalDateTime.parse("2025-03-23T10:00:00"));
        guestOrder.setVisitDate(LocalDateTime.parse("2025-03-24T12:00:00"));
        guestOrder.setStatus(Status.NOWE);

    }

    @Test
    void addGuestOrder_ShouldReturnGuestOrder(){

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrder guestOrderResult = guestOrderService.addGuestOrder(guestOrder);

        assertNotNull(guestOrderResult);
        assertEquals(guestOrder, guestOrderResult);
        verify(guestOrderRepository, times(1)).save(guestOrder);

    }

    @Test
    void getAllGuestOrders_ShouldReturnListOfGuestOrders(){

        when(guestOrderRepository.findAll()).thenReturn(List.of(guestOrder));

        List<GuestOrder> guestOrderResult = guestOrderService.getAllGuestOrders();


        assertEquals(guestOrder, guestOrderResult.getFirst());
        verify(guestOrderRepository, times(1)).findAll();
    }

    @Test
    void getGuestOrder_ShouldReturnGuestOrder_WhenOrderExists() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        GuestOrder guestOrderResult = guestOrderService.getGuestOrder(1L);

        assertNotNull(guestOrderResult);
        verify(guestOrderRepository, times(1)).findById(1L);
    }

    @Test
    void getGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> guestOrderService.getGuestOrder(1L));

        verify(guestOrderRepository, times(1)).findById(1L);
    }

    @Test
    void getGuestOrdersByStatus_ShouldReturnGuestOrders_WhenStatusExists() {

        when(guestOrderRepository.findGuestOrdersByStatus(Status.NOWE)).thenReturn(List.of(guestOrder));

        List<GuestOrder> guestOrdersByStatusResult = guestOrderService.getGuestOrdersByStatus(Status.NOWE);

        assertNotNull(guestOrdersByStatusResult);
        assertEquals(1, guestOrdersByStatusResult.size());

        verify(guestOrderRepository, times(1)).findGuestOrdersByStatus(Status.NOWE);
    }

    @Test
    void GuestOrderService_UpdateGuestOrder_ShouldUpdateAndReturnGuestOrder_WhenOrderExists() {
        GuestOrder updatedGuestOrder = new GuestOrder();
        updatedGuestOrder.setIdGuestOrder(1L);
        updatedGuestOrder.setFirstname("Jane");
        updatedGuestOrder.setLastname("Smith");
        updatedGuestOrder.setPhonenumber("987654321");
        updatedGuestOrder.setEmail("jane.smith@example.com");
        updatedGuestOrder.setOffer(offer);
        updatedGuestOrder.setVisitDate(LocalDateTime.parse("2025-03-26T10:00:00"));
        updatedGuestOrder.setStatus(Status.NOWE);


        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));
        when(guestOrderRepository.save(any(GuestOrder.class))).thenReturn(guestOrder);

        GuestOrder guestOrderResult = guestOrderService.updateGuestOrder(updatedGuestOrder, 1L);


        assertNotNull(guestOrderResult);
        assertAll(
                () -> assertEquals(updatedGuestOrder.getFirstname(), guestOrderResult.getFirstname()),
                () -> assertEquals(updatedGuestOrder.getLastname(), guestOrderResult.getLastname()),
                () -> assertEquals(updatedGuestOrder.getPhonenumber(), guestOrderResult.getPhonenumber()),
                () -> assertEquals(updatedGuestOrder.getEmail(), guestOrderResult.getEmail()),
                () -> assertEquals(updatedGuestOrder.getOffer(), guestOrderResult.getOffer()),
                () -> assertEquals(updatedGuestOrder.getVisitDate(), guestOrderResult.getVisitDate()),
                () -> assertEquals(updatedGuestOrder.getStatus(), guestOrderResult.getStatus())
        );

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(guestOrderRepository, times(1)).save(guestOrder);

    }

    @Test
    void GuestOrderService_UpdateGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        GuestOrder updatedGuestOrder = new GuestOrder();
        updatedGuestOrder.setIdGuestOrder(1L);
        updatedGuestOrder.setFirstname("Jane");
        updatedGuestOrder.setLastname("Smith");
        updatedGuestOrder.setPhonenumber("987654321");
        updatedGuestOrder.setEmail("jane.smith@example.com");
        updatedGuestOrder.setOffer(offer);
        updatedGuestOrder.setOrderDate(LocalDateTime.parse("2025-03-25T10:00:00"));
        updatedGuestOrder.setVisitDate(LocalDateTime.parse("2025-03-26T10:00:00"));
        updatedGuestOrder.setStatus(Status.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> guestOrderService.updateGuestOrder(updatedGuestOrder, 1L));

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
    }

    @Test
    void GuestOrderService_deleteGuestOrderById_ShouldDeleteGuestOrder_WhenGuestOrderExists() {
        when(guestOrderRepository.existsById(1L)).thenReturn(true);

        guestOrderService.deleteGuestOrderById(1L);

        verify(guestOrderRepository, times(1)).existsById(1L);
        verify(guestOrderRepository, times(1)).deleteById(1L);
    }

    @Test
    void GuestOrderService_deleteGuestOrderById_ShouldThrowException_WhenGuestOrderDoesNotExist() {
        when(guestOrderRepository.existsById(1L)).thenReturn(false);

        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> guestOrderService.deleteGuestOrderById(1L));

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository, times(1)).existsById(1L);
        verify(guestOrderRepository, never()).deleteById(1L);
    }

}
