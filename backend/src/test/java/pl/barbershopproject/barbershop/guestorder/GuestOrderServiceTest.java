package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities;
import pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GuestOrderServiceTest {

    private static final ZoneId TEST_ZONE = ZoneId.of("Europe/Warsaw");
    private static final Instant TEST_INSTANT = Instant.parse("2026-01-16T12:00:00Z");

    @Mock
    private GuestOrderRepository guestOrderRepository;

    @Mock
    private OfferRepository offerRepository;

    @Mock
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private StripeCheckoutService stripeCheckoutService;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private Clock clock;

    @InjectMocks
    private GuestOrderService guestOrderService;

    private GuestOrder guestOrder;
    private Offer offer;

    @BeforeEach
    void setUp() {
        lenient().when(clock.instant()).thenReturn(TEST_INSTANT);
        lenient().when(clock.getZone()).thenReturn(TEST_ZONE);

        offer = OfferTestEntities.createOffer();

        guestOrder = GuestOrderTestEntities.createGuestOrder();
        guestOrder.setOffer(offer);
    }

    @Test
    void addGuestOrder_ShouldReturnGuestOrderCreationResponse() {
        GuestOrderCreationDTO dto = GuestOrderTestEntities.createGuestOrderCreationDTO();

        when(offerRepository.findById(dto.idOffer())).thenReturn(Optional.of(offer));
        when(guestOrderRepository.save(any(GuestOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GuestOrderCreationResponseDTO result = guestOrderService.addGuestOrder(dto);

        assertNotNull(result);
        assertEquals(PaymentMethod.GOTOWKA, result.paymentMethod());
        assertEquals(PaymentStatus.OCZEKUJE_NA_PLATNOSC, result.paymentStatus());
        assertNull(result.checkoutUrl());

        verify(offerRepository, times(1)).findById(dto.idOffer());
        verify(appointmentAvailabilityService, times(1)).reserveSlot(dto.visitDate());
        verify(guestOrderRepository, times(1)).save(any(GuestOrder.class));
        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(eventPublisher, times(1)).publishEvent(any(OrderCreatedEvent.class));
        verifyNoInteractions(stripeCheckoutService);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void addGuestOrder_ShouldThrowException_WhenOfferDoesNotExist() {
        GuestOrderCreationDTO guestOrderCreationDTO = GuestOrderTestEntities.createGuestOrderCreationDTO();

        when(offerRepository.findById(guestOrderCreationDTO.idOffer())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> guestOrderService.addGuestOrder(guestOrderCreationDTO)
        );

        assertEquals("Oferta o ID: " + guestOrderCreationDTO.idOffer() + " nie istnieje", exception.getMessage());

        verify(offerRepository, times(1)).findById(guestOrderCreationDTO.idOffer());
        verify(appointmentAvailabilityService, never()).reserveSlot(any());
        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void addGuestOrder_ShouldThrowException_WhenAppointmentSlotIsTaken() {
        GuestOrderCreationDTO dto = GuestOrderTestEntities.createGuestOrderCreationDTO();

        when(offerRepository.findById(dto.idOffer())).thenReturn(Optional.of(offer));
        doThrow(new AppointmentSlotTakenException(dto.visitDate()))
                .when(appointmentAvailabilityService)
                .reserveSlot(dto.visitDate());

        assertThrows(
                AppointmentSlotTakenException.class,
                () -> guestOrderService.addGuestOrder(dto)
        );

        verify(offerRepository, times(1)).findById(dto.idOffer());
        verify(appointmentAvailabilityService, times(1)).reserveSlot(dto.visitDate());
        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
        verify(eventPublisher, never()).publishEvent(any());
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getAllGuestOrders_ShouldReturnListOfGuestOrderDTOs() {
        when(guestOrderRepository.findAll()).thenReturn(List.of(guestOrder));

        List<GuestOrderDTO> result = guestOrderService.getAllGuestOrders();

        assertEquals(1, result.size());
        assertEquals(guestOrder.getIdGuestOrder(), result.getFirst().idGuestOrder());

        verify(guestOrderRepository, times(1)).findAll();
    }

    @Test
    void getGuestOrder_ShouldReturnGuestOrderDTO_WhenOrderExists() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        GuestOrderDTO result = guestOrderService.getGuestOrder(1L);

        assertNotNull(result);
        assertEquals(guestOrder.getIdGuestOrder(), result.idGuestOrder());

        verify(guestOrderRepository, times(1)).findById(1L);
    }

    @Test
    void getGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> guestOrderService.getGuestOrder(1L)
        );

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository, times(1)).findById(1L);
    }

    @Test
    void getGuestOrdersByStatus_ShouldReturnGuestOrderDTOs_WhenStatusExists() {
        when(guestOrderRepository.findGuestOrdersByStatus(Status.NOWE))
                .thenReturn(List.of(guestOrder));

        List<GuestOrderDTO> result = guestOrderService.getGuestOrdersByStatus(Status.NOWE);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(Status.NOWE, result.getFirst().status());

        verify(guestOrderRepository, times(1)).findGuestOrdersByStatus(Status.NOWE);
    }

    @Test
    void updateGuestOrder_ShouldUpdateAndReturnGuestOrderDTO_WhenOrderExists() {
        GuestOrderUpdateRequestDTO updatedGuestOrder = GuestOrderTestEntities.createGuestOrderUpdateRequestDTO();

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();
        Status currentStatus = guestOrder.getStatus();

        LocalDateTime targetVisitDate = updatedGuestOrder.visitDate();
        Status targetStatus = updatedGuestOrder.status();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));
        when(offerRepository.findById(updatedGuestOrder.idOffer())).thenReturn(Optional.of(offer));
        when(guestOrderRepository.save(any(GuestOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GuestOrderDTO guestOrderResult = guestOrderService.updateGuestOrder(updatedGuestOrder, 1L);

        assertNotNull(guestOrderResult);
        assertAll(
                () -> assertEquals(updatedGuestOrder.firstname(), guestOrderResult.firstname()),
                () -> assertEquals(updatedGuestOrder.lastname(), guestOrderResult.lastname()),
                () -> assertEquals(updatedGuestOrder.phonenumber(), guestOrderResult.phonenumber()),
                () -> assertEquals(updatedGuestOrder.email(), guestOrderResult.email()),
                () -> assertEquals(offer, guestOrderResult.offer()),
                () -> assertEquals(targetVisitDate, guestOrderResult.visitDate()),
                () -> assertEquals(targetStatus, guestOrderResult.status())
        );

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).findById(updatedGuestOrder.idOffer());
        verify(appointmentAvailabilityService, times(1)).updateSlotReservation(
                currentVisitDate,
                currentStatus,
                targetVisitDate,
                targetStatus
        );
        verify(guestOrderRepository, times(1)).save(guestOrder);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void updateGuestOrder_ShouldUseCurrentStatus_WhenUpdatedStatusIsNull() {
        GuestOrderUpdateRequestDTO updatedGuestOrder =
                GuestOrderTestEntities.createGuestOrderUpdateRequestDTOWithNullStatus();

        LocalDateTime currentVisitDate = guestOrder.getVisitDate();
        Status currentStatus = guestOrder.getStatus();

        LocalDateTime targetVisitDate = updatedGuestOrder.visitDate();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));
        when(offerRepository.findById(updatedGuestOrder.idOffer())).thenReturn(Optional.of(offer));
        when(guestOrderRepository.save(any(GuestOrder.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        GuestOrderDTO guestOrderResult = guestOrderService.updateGuestOrder(updatedGuestOrder, 1L);

        assertNotNull(guestOrderResult);
        assertEquals(targetVisitDate, guestOrderResult.visitDate());
        assertEquals(currentStatus, guestOrderResult.status());

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).findById(updatedGuestOrder.idOffer());
        verify(appointmentAvailabilityService, times(1)).updateSlotReservation(
                currentVisitDate,
                currentStatus,
                targetVisitDate,
                currentStatus
        );
        verify(guestOrderRepository, times(1)).save(guestOrder);
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOrderDoesNotExist() {
        GuestOrderUpdateRequestDTO updatedGuestOrder = GuestOrderTestEntities.createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(updatedGuestOrder, 1L)
        );

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(offerRepository, never()).findById(any());
        verify(appointmentAvailabilityService, never()).updateSlotReservation(any(), any(), any(), any());
        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
    }

    @Test
    void updateGuestOrder_ShouldThrowException_WhenOfferDoesNotExist() {
        GuestOrderUpdateRequestDTO updatedGuestOrder = GuestOrderTestEntities.createGuestOrderUpdateRequestDTO();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));
        when(offerRepository.findById(updatedGuestOrder.idOffer())).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> guestOrderService.updateGuestOrder(updatedGuestOrder, 1L)
        );

        assertEquals("Oferta o ID: " + updatedGuestOrder.idOffer() + " nie istnieje", exception.getMessage());

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).findById(updatedGuestOrder.idOffer());
        verify(appointmentAvailabilityService, never()).updateSlotReservation(any(), any(), any(), any());
        verify(guestOrderRepository, never()).save(any(GuestOrder.class));
    }

    @Test
    void deleteGuestOrderById_ShouldDeleteGuestOrder_WhenGuestOrderExists() {
        LocalDateTime visitDate = guestOrder.getVisitDate();
        Status status = guestOrder.getStatus();

        when(guestOrderRepository.findById(1L)).thenReturn(Optional.of(guestOrder));

        guestOrderService.deleteGuestOrderById(1L);

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(appointmentAvailabilityService, times(1)).releaseIfReserved(visitDate, status);
        verify(guestOrderRepository, times(1)).delete(guestOrder);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void deleteGuestOrderById_ShouldThrowException_WhenGuestOrderDoesNotExist() {
        when(guestOrderRepository.findById(1L)).thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> guestOrderService.deleteGuestOrderById(1L)
        );

        assertEquals("Nie znaleziono zamówienia o ID: 1", exception.getMessage());

        verify(guestOrderRepository, times(1)).findById(1L);
        verify(appointmentAvailabilityService, never()).releaseIfReserved(any(), any());
        verify(guestOrderRepository, never()).delete(any(GuestOrder.class));
    }
}