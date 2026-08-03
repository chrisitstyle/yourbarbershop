package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.utils.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.*;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

@ExtendWith(MockitoExtension.class)
class GuestOrderServiceTest {

    @Mock
    private GuestOrderRepository guestOrderRepository;
    @Mock
    private OfferQuery offerQuery;
    @Mock
    private AppointmentReservation appointmentReservation;
    @Mock
    private PaymentOfferUpdater paymentOfferUpdater;
    @Mock
    private GuestOrderEvents guestOrderEvents;
    @Mock
    private GuestOrderCreationTransaction guestOrderCreationTransaction;
    @Mock
    private PaymentCheckout paymentCheckout;

    @InjectMocks
    private GuestOrderService guestOrderService;

    private GuestOrder guestOrder;
    private Offer offer;

    @BeforeEach
    void setUp() {
        offer = createOffer();

        guestOrder = guestOrderBuilder()
                .idGuestOrder(1L)
                .offer(offer)
                .bookedOffer(createBookedOffer(offer))
                .build();
    }

    @Test
    void addGuestOrder_ShouldReturnResponseWithoutCheckoutUrl_ForOfflinePayment() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.GOTOWKA,
                PaymentStatus.NIE_WYMAGANA,
                offer.getCost(), "PLN", offer.getKind());

        GuestOrderCreationTransactionResult transactionResult = new GuestOrderCreationTransactionResult(
                1L, checkoutRequest);

        when(guestOrderCreationTransaction.create(guestOrderCreationDTO)).thenReturn(transactionResult);
        when(paymentCheckout.createCheckoutIfRequired(checkoutRequest)).thenReturn(null);

        GuestOrderCreationResponseDTO result = guestOrderService.addGuestOrder(guestOrderCreationDTO);

        assertNotNull(result);
        assertEquals(PaymentMethod.GOTOWKA, result.paymentMethod());
        assertEquals(PaymentStatus.NIE_WYMAGANA, result.paymentStatus());
        assertNull(result.checkoutUrl());

        verify(guestOrderCreationTransaction).create(guestOrderCreationDTO);

        verify(paymentCheckout).createCheckoutIfRequired(checkoutRequest);
    }

    @Test
    void addGuestOrder_ShouldReturnCheckoutUrl_ForOnlinePayment() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                10L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                offer.getCost(),
                "PLN", offer.getKind());

        GuestOrderCreationTransactionResult transactionResult = new GuestOrderCreationTransactionResult(
                1L, checkoutRequest);

        String checkoutUrl = "https://checkout.stripe.com/c/pay/cs_test_123";

        when(guestOrderCreationTransaction.create(guestOrderCreationDTO)).thenReturn(transactionResult);

        when(paymentCheckout.createCheckoutIfRequired(checkoutRequest)).thenReturn(checkoutUrl);

        GuestOrderCreationResponseDTO result = guestOrderService.addGuestOrder(guestOrderCreationDTO);

        assertNotNull(result);
        assertEquals(PaymentMethod.KARTA_ONLINE, result.paymentMethod());
        assertEquals(PaymentStatus.OCZEKUJE_NA_PLATNOSC, result.paymentStatus());
        assertEquals(checkoutUrl, result.checkoutUrl());

        verify(guestOrderCreationTransaction).create(guestOrderCreationDTO);

        verify(paymentCheckout).createCheckoutIfRequired(checkoutRequest);
    }

    @Test
    void addGuestOrder_ShouldNotCreateCheckout_WhenTransactionFails() {
        GuestOrderCreationDTO guestOrderCreationDTO = createGuestOrderCreationDTO();

        when(guestOrderCreationTransaction.create(guestOrderCreationDTO))
                .thenThrow(new NoSuchElementException("Oferta o ID: " + guestOrderCreationDTO.idOffer() + " nie istnieje"));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.addGuestOrder(guestOrderCreationDTO));

        assertEquals("Oferta o ID: " + guestOrderCreationDTO.idOffer() + " nie istnieje", exception.getMessage());

        verify(guestOrderCreationTransaction).create(guestOrderCreationDTO);

        verifyNoInteractions(paymentCheckout);
    }

    @Test
    void getAllGuestOrders_ShouldReturnListOfGuestOrderDTOs() {
        when(guestOrderRepository.findAll()).thenReturn(List.of(guestOrder));

        List<GuestOrderDTO> result = guestOrderService.getAllGuestOrders();

        assertEquals(1, result.size());
        assertEquals(guestOrder.getIdGuestOrder(), result.getFirst().idGuestOrder());
        assertEquals(guestOrder.getBookedOffer().getName(), result.getFirst().offer().kind());
        assertEquals(guestOrder.getBookedOffer().getPrice(), result.getFirst().offer().cost());

        verify(guestOrderRepository).findAll();
    }

    @Test
    void getGuestOrder_ShouldReturnGuestOrderDTO_WhenOrderExists() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        GuestOrderDTO result = guestOrderService.getGuestOrder(1L);

        assertNotNull(result);
        assertEquals(guestOrder.getIdGuestOrder(), result.idGuestOrder());
        assertEquals(guestOrder.getBookedOffer().getName(), result.offer().kind());
        assertEquals(guestOrder.getBookedOffer().getPrice(), result.offer().cost());

        verify(guestOrderRepository).findById(1L);
    }

    @Test
    void getGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.getGuestOrder(1L));

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);
    }

    @Test
    void getGuestOrdersByStatus_ShouldReturnGuestOrderDTOs() {
        when(guestOrderRepository.findGuestOrdersByStatus(Status.NOWE)).thenReturn(List.of(guestOrder));

        List<GuestOrderDTO> result = guestOrderService.getGuestOrdersByStatus(Status.NOWE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Status.NOWE, result.getFirst().status());

        verify(guestOrderRepository).findGuestOrdersByStatus(Status.NOWE);
    }

    @Test
    void updateGuestOrder_ShouldUpdateExistingOrderWithoutChangingOffer() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        Status currentStatus = guestOrder.getStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));
        when(offerQuery.getRequiredOffer(request.idOffer())).thenReturn(offer);
        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        assertNotNull(result);

        assertAll(
                () -> assertEquals(request.firstname(), result.firstname()),
                () -> assertEquals(request.lastname(), result.lastname()),
                () -> assertEquals(request.phonenumber(), result.phonenumber()),
                () -> assertEquals(request.email(), result.email()),
                () -> assertEquals(offer.getIdOffer(), result.offer().idOffer()),
                () -> assertEquals(guestOrder.getBookedOffer().getName(), result.offer().kind()),
                () -> assertEquals(guestOrder.getBookedOffer().getPrice(), result.offer().cost()),
                () -> assertEquals(request.visitDate(), result.visitDate()),
                () -> assertEquals(request.status(), result.status()));

        verify(offerQuery).getRequiredOffer(request.idOffer());
        verifyNoInteractions(paymentOfferUpdater);
        verify(appointmentReservation).updateSlotReservation(
                currentVisitDate, currentStatus, request.visitDate(), request.status());

        verify(guestOrderRepository).save(guestOrder);
        verify(guestOrderEvents).updated(guestOrder, currentStatus);
    }

    @Test
    void updateGuestOrder_ShouldUpdateBookedOffer_WhenAssignedOfferChanges() {
        Offer targetOffer = createOffer(2L, "Strzyżenie i broda", new BigDecimal("180.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(offer.getCost()).build();

        guestOrder.setPayment(payment);

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        Status currentStatus = guestOrder.getStatus();

        GuestOrderUpdateRequestDTO request = new GuestOrderUpdateRequestDTO(
                "UpdatedJohn",
                "UpdatedDoe",
                "987654321",
                "updated@example.com",
                targetOffer.getIdOffer(),
                LocalDateTime.of(2026, 11, 10, 12, 0), Status.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(targetOffer.getIdOffer())).thenReturn(targetOffer);

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        verify(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        verify(appointmentReservation).updateSlotReservation(currentVisitDate,
                currentStatus,
                request.visitDate(), request.status());

        assertSame(targetOffer, guestOrder.getOffer());
        assertEquals(targetOffer.getKind(), guestOrder.getBookedOffer().getName());
        assertEquals(0, targetOffer.getCost().compareTo(guestOrder.getBookedOffer().getPrice()));
        assertEquals(targetOffer.getIdOffer(), result.offer().idOffer());
        assertEquals(targetOffer.getKind(), result.offer().kind());
        assertEquals(0, targetOffer.getCost().compareTo(result.offer().cost()));
    }

    @Test
    void updateGuestOrder_ShouldPreserveBookedOffer_WhenCatalogOfferDataChanged() {
        Offer changedCatalogOffer = createOffer(
                offer.getIdOffer(),
                "Nowa nazwa katalogowa",
                new BigDecimal("999.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.GOTOWKA)
                .paymentStatus(PaymentStatus.NIE_WYMAGANA)
                .amount(guestOrder.getBookedOffer().getPrice()).build();

        guestOrder.setPayment(payment);

        String bookedName = guestOrder.getBookedOffer().getName();

        BigDecimal bookedPrice = guestOrder.getBookedOffer().getPrice();

        GuestOrderUpdateRequestDTO request = new GuestOrderUpdateRequestDTO(
                "UpdatedJohn",
                "UpdatedDoe",
                "987654321",
                "updated@example.com",
                changedCatalogOffer.getIdOffer(),
                LocalDateTime.of(2026, 11, 10, 12, 0), Status.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(changedCatalogOffer.getIdOffer())).thenReturn(changedCatalogOffer);

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        verifyNoInteractions(paymentOfferUpdater);

        assertSame(offer, guestOrder.getOffer());
        assertEquals(bookedName, guestOrder.getBookedOffer().getName());
        assertEquals(0, bookedPrice.compareTo(guestOrder.getBookedOffer().getPrice()));
        assertEquals(0, bookedPrice.compareTo(payment.getAmount()));
        assertEquals(bookedName, result.offer().kind());
        assertEquals(0, bookedPrice.compareTo(result.offer().cost()));
    }

    @Test
    void updateGuestOrder_ShouldNotUpdateOrder_WhenPaymentRejectsOfferChange() {
        Offer targetOffer = createOffer(
                2L,
                "Strzyżenie i broda",
                new BigDecimal("180.00"));

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .amount(offer.getCost()).build();

        guestOrder.setPayment(payment);

        String currentFirstname = guestOrder.getFirstname();
        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        GuestOrderUpdateRequestDTO request = new GuestOrderUpdateRequestDTO(
                "UpdatedJohn",
                "UpdatedDoe",
                "987654321",
                "updated@example.com",
                targetOffer.getIdOffer(),
                LocalDateTime.of(2026, 11, 10, 12, 0), Status.NOWE);

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(targetOffer.getIdOffer())).thenReturn(targetOffer);

        doThrow(new OrderOfferChangeNotAllowedException("Nie można zmienić oferty w opłaconym zamówieniu"))
                .when(paymentOfferUpdater).updateAfterOfferChange(payment, targetOffer);

        OrderOfferChangeNotAllowedException exception = assertThrows(OrderOfferChangeNotAllowedException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Nie można zmienić oferty w opłaconym zamówieniu", exception.getMessage());

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));

        verifyNoInteractions(appointmentReservation, guestOrderEvents);

        assertSame(offer, guestOrder.getOffer());
        assertEquals(offer.getKind(), guestOrder.getBookedOffer().getName());
        assertEquals(0, offer.getCost().compareTo(guestOrder.getBookedOffer().getPrice()));
        assertEquals(currentFirstname, guestOrder.getFirstname());
        assertEquals(currentVisitDate, guestOrder.getVisitDate());
    }

    @Test
    void updateGuestOrder_ShouldUseCurrentStatus_WhenUpdatedStatusIsNull() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTOWithNullStatus();

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();

        Status currentStatus = guestOrder.getStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(request.idOffer())).thenReturn(offer);

        when(guestOrderRepository.save(guestOrder)).thenReturn(guestOrder);

        GuestOrderDTO result = guestOrderService.updateGuestOrder(request, 1L);

        assertNotNull(result);
        assertEquals(request.visitDate(), result.visitDate());
        assertEquals(currentStatus, result.status());

        verifyNoInteractions(paymentOfferUpdater);

        verify(appointmentReservation).updateSlotReservation(currentVisitDate,
                currentStatus, request.visitDate(), currentStatus);

        verify(guestOrderRepository).save(guestOrder);
        verify(guestOrderEvents).updated(guestOrder, currentStatus);
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);

        verifyNoInteractions(offerQuery, appointmentReservation, paymentOfferUpdater, guestOrderEvents);

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOfferDoesNotExist() {
        GuestOrderUpdateRequestDTO request = createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        when(offerQuery.getRequiredOffer(request.idOffer()))
                .thenThrow(new NoSuchElementException("Oferta o ID: " + request.idOffer() + " nie istnieje"));

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(request, 1L));

        assertEquals("Oferta o ID: " + request.idOffer() + " nie istnieje", exception.getMessage());

        verify(guestOrderRepository).findById(1L);
        verify(offerQuery).getRequiredOffer(request.idOffer());

        verifyNoInteractions(appointmentReservation, paymentOfferUpdater, guestOrderEvents);

        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
    }

    @Test
    void deleteGuestOrderById_ShouldDeleteGuestOrder() {
        LocalDateTime visitDate = guestOrder.getVisitDate();
        Status status = guestOrder.getStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        guestOrderService.deleteGuestOrderById(1L);

        verify(guestOrderRepository).findById(1L);
        verify(appointmentReservation).releaseIfReserved(visitDate, status);
        verify(guestOrderRepository).delete(guestOrder);
        verify(guestOrderEvents).deleted(1L);
    }

    @Test
    void deleteGuestOrderById_ShouldThrowException_WhenOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(NoSuchElementException.class,
                () -> guestOrderService.deleteGuestOrderById(1L));

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository).findById(1L);

        verifyNoInteractions(appointmentReservation, guestOrderEvents);

        verify(guestOrderRepository, never()).delete(any(GuestOrder.class));
    }
}