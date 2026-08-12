package pl.barbershopproject.barbershop.orderupdate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferChangeHandler;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class OrderUpdateCoordinatorTest {

    private OrderModificationPolicy orderModificationPolicy;
    private OfferQuery offerQuery;
    private OfferChangeHandler offerChangeHandler;
    private AppointmentReservation appointmentReservation;

    private OrderUpdateCoordinator orderUpdateCoordinator;

    @BeforeEach
    void setUp() {
        orderModificationPolicy = mock(OrderModificationPolicy.class);

        offerQuery = mock(OfferQuery.class);

        offerChangeHandler = mock(OfferChangeHandler.class);

        appointmentReservation = mock(AppointmentReservation.class);

        orderUpdateCoordinator =
                new OrderUpdateCoordinator(
                        orderModificationPolicy,
                        offerQuery,
                        offerChangeHandler,
                        appointmentReservation
                );
    }

    @Test
    void shouldPrepareUpdateWithRequestedStatus() {
        OrderUpdateTarget order = mock(OrderUpdateTarget.class);

        Payment payment = mock(Payment.class);

        Offer currentOffer = mock(Offer.class);

        Offer targetOffer = mock(Offer.class);

        Long targetOfferId = 2L;

        LocalDateTime currentVisitDate = LocalDateTime.of(
                2026,
                11,
                10,
                10,
                0);

        LocalDateTime targetVisitDate = LocalDateTime.of(
                2026,
                Month.NOVEMBER,
                10,
                12,
                0);

        OrderStatus currentStatus = OrderStatus.NOWE;

        OrderStatus requestedStatus = OrderStatus.ZREALIZOWANE;

        when(order.getOrderStatus())
                .thenReturn(currentStatus);

        when(order.getOffer())
                .thenReturn(currentOffer);

        when(order.getVisitDate())
                .thenReturn(currentVisitDate);

        when(offerQuery.getRequiredOffer(targetOfferId))
                .thenReturn(targetOffer);

        OrderUpdateResult result = orderUpdateCoordinator.prepareUpdate(
                order,
                payment,
                targetOfferId,
                targetVisitDate,
                requestedStatus);

        assertEquals(currentStatus, result.currentStatus());

        assertEquals(requestedStatus, result.targetStatus());

        verify(orderModificationPolicy).validateUpdate(
                currentStatus,
                requestedStatus,
                payment);

        verify(offerQuery).getRequiredOffer(targetOfferId);

        verify(offerChangeHandler).updateIfChanged(
                eq(currentOffer),
                eq(targetOffer),
                eq(payment),
                any(),
                any());

        verify(appointmentReservation).updateSlotReservation(
                currentVisitDate,
                currentStatus,
                targetVisitDate,
                requestedStatus);
    }

    @Test
    void shouldUseCurrentStatusWhenRequestedStatusIsNull() {
        OrderUpdateTarget order = mock(OrderUpdateTarget.class);

        Payment payment = mock(Payment.class);

        Offer currentOffer = mock(Offer.class);

        Offer targetOffer = mock(Offer.class);

        Long targetOfferId = 2L;

        LocalDateTime currentVisitDate = LocalDateTime.of(
                2026,
                Month.NOVEMBER,
                10,
                10,
                0);

        LocalDateTime targetVisitDate = LocalDateTime.of(
                2026,
                Month.NOVEMBER,
                10,
                12,
                0);

        OrderStatus currentStatus = OrderStatus.NOWE;

        when(order.getOrderStatus())
                .thenReturn(currentStatus);

        when(order.getOffer())
                .thenReturn(currentOffer);

        when(order.getVisitDate())
                .thenReturn(currentVisitDate);

        when(offerQuery.getRequiredOffer(targetOfferId))
                .thenReturn(targetOffer);

        OrderUpdateResult result = orderUpdateCoordinator.prepareUpdate(
                order,
                payment,
                targetOfferId,
                targetVisitDate,
                null);

        assertEquals(currentStatus, result.currentStatus());

        assertEquals(currentStatus, result.targetStatus());

        verify(orderModificationPolicy).validateUpdate(
                currentStatus,
                currentStatus,
                payment);

        verify(appointmentReservation).updateSlotReservation(
                currentVisitDate,
                currentStatus,
                targetVisitDate,
                currentStatus);
    }

    @Test
    void shouldStopUpdateWhenModificationPolicyRejectsUpdate() {
        OrderUpdateTarget order = mock(OrderUpdateTarget.class);

        Payment payment = mock(Payment.class);

        OrderStatus currentStatus = OrderStatus.NOWE;

        OrderStatus requestedStatus = OrderStatus.ZREALIZOWANE;

        when(order.getOrderStatus())
                .thenReturn(currentStatus);

        doThrow(new OrderStatusChangeNotAllowedException(
                "Nie można zrealizować zamówienia przed rozliczeniem płatności"))
                .when(orderModificationPolicy)
                .validateUpdate(
                        currentStatus,
                        requestedStatus,
                        payment);

        OrderStatusChangeNotAllowedException exception = assertThrows(
                OrderStatusChangeNotAllowedException.class,
                () -> orderUpdateCoordinator.prepareUpdate(
                        order,
                        payment,
                        2L,
                        LocalDateTime.of(
                                2026,
                                11,
                                10,
                                12,
                                0
                        ),
                        requestedStatus));

        assertEquals("Nie można zrealizować zamówienia przed rozliczeniem płatności",
                exception.getMessage());

        verify(orderModificationPolicy)
                .validateUpdate(
                        currentStatus,
                        requestedStatus,
                        payment
                );

        verifyNoInteractions(
                offerQuery,
                offerChangeHandler,
                appointmentReservation);
    }

    @Test
    void shouldStopUpdateWhenOfferDoesNotExist() {
        OrderUpdateTarget order = mock(OrderUpdateTarget.class);

        Payment payment = mock(Payment.class);

        Long targetOfferId = 2L;

        OrderStatus currentStatus = OrderStatus.NOWE;

        when(order.getOrderStatus())
                .thenReturn(currentStatus);

        when(offerQuery.getRequiredOffer(targetOfferId))
                .thenThrow(
                        new NoSuchElementException(
                                "Oferta o ID: 2 nie istnieje"
                        )
                );

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> orderUpdateCoordinator.prepareUpdate(
                        order,
                        payment,
                        targetOfferId,
                        LocalDateTime.of(
                                2026,
                                Month.NOVEMBER,
                                10,
                                12,
                                0
                        ), currentStatus));

        assertEquals("Oferta o ID: 2 nie istnieje",
                exception.getMessage());

        verify(orderModificationPolicy)
                .validateUpdate(
                        currentStatus,
                        currentStatus,
                        payment);

        verify(offerQuery).getRequiredOffer(targetOfferId);

        verifyNoInteractions(
                offerChangeHandler,
                appointmentReservation);
    }

    @Test
    void shouldStopUpdateWhenOfferChangeFails() {
        OrderUpdateTarget order = mock(OrderUpdateTarget.class);

        Payment payment = mock(Payment.class);

        Offer currentOffer = mock(Offer.class);

        Offer targetOffer = mock(Offer.class);

        Long targetOfferId = 2L;

        OrderStatus currentStatus = OrderStatus.NOWE;

        LocalDateTime targetVisitDate = LocalDateTime.of(
                2026,
                Month.NOVEMBER,
                10,
                12,
                0
        );

        when(order.getOrderStatus())
                .thenReturn(currentStatus);

        when(order.getOffer())
                .thenReturn(currentOffer);

        when(offerQuery.getRequiredOffer(targetOfferId))
                .thenReturn(targetOffer);

        doThrow(new OrderOfferChangeNotAllowedException(
                "Nie można zmienić oferty w opłaconym zamówieniu"))
                .when(offerChangeHandler)
                .updateIfChanged(
                        eq(currentOffer),
                        eq(targetOffer),
                        eq(payment),
                        any(),
                        any()
                );

        OrderOfferChangeNotAllowedException exception = assertThrows(
                OrderOfferChangeNotAllowedException.class,
                () -> orderUpdateCoordinator.prepareUpdate(
                        order,
                        payment,
                        targetOfferId,
                        targetVisitDate,
                        currentStatus));

        assertEquals("Nie można zmienić oferty w opłaconym zamówieniu",
                exception.getMessage());

        verify(offerChangeHandler)
                .updateIfChanged(
                        eq(currentOffer),
                        eq(targetOffer),
                        eq(payment),
                        any(),
                        any());

        verifyNoInteractions(appointmentReservation);
    }
}
