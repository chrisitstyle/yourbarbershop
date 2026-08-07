package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.event.OnlinePaymentPendingEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class GuestOrderEventsTest {

    private ApplicationEventPublisher eventPublisher;
    private GuestOrderEvents guestOrderEvents;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        guestOrderEvents = new GuestOrderEvents(eventPublisher);
    }

    @Test
    void shouldPublishOnlinePaymentPendingEvent_ForOnlinePayment() {
        // given
        Offer offer = mock(Offer.class);
        BookedOffer bookedOffer = mock(BookedOffer.class);
        GuestOrder guestOrder = mock(GuestOrder.class);
        Payment payment = mock(Payment.class);

        LocalDateTime visitDate =
                LocalDateTime.of(2030, 1, 11, 12, 0);

        when(offer.getKind()).thenReturn("Strzyżenie");
        when(offer.getCost()).thenReturn(BigDecimal.valueOf(80));

        when(bookedOffer.getName()).thenReturn("Strzyżenie");
        when(bookedOffer.getPrice()).thenReturn(BigDecimal.valueOf(80));

        when(guestOrder.getIdGuestOrder()).thenReturn(10L);
        when(guestOrder.getEmail()).thenReturn("guest@example.com");
        when(guestOrder.getFirstname()).thenReturn("Jan");
        when(guestOrder.getOffer()).thenReturn(offer);
        when(guestOrder.getBookedOffer()).thenReturn(bookedOffer);
        when(guestOrder.getVisitDate()).thenReturn(visitDate);

        when(payment.getIdPayment()).thenReturn(15L);
        when(payment.getPaymentMethod())
                .thenReturn(PaymentMethod.KARTA_ONLINE);

        // when
        guestOrderEvents.created(guestOrder, payment);

        // then
        verify(eventPublisher).publishEvent(
                argThat((Object event) -> event instanceof OnlinePaymentPendingEvent(
                                Long paymentId, String email, String firstname, LocalDateTime date, String offerName,
                                BigDecimal offerCost
                        )
                                && paymentId.equals(15L)
                                && email.equals("guest@example.com")
                                && firstname.equals("Jan")
                                && date.equals(visitDate)
                                && offerName.equals("Strzyżenie")
                                && offerCost.compareTo(
                                BigDecimal.valueOf(80)
                        ) == 0
                )
        );

        verify(eventPublisher, never()).publishEvent(
                isA(OrderCreatedEvent.class)
        );
    }

    @Test
    void shouldPublishConfirmationEvent_ForOfflinePayment() {
        // given
        Offer offer = mock(Offer.class);
        GuestOrder guestOrder = mock(GuestOrder.class);
        Payment payment = mock(Payment.class);

        when(offer.getKind()).thenReturn("Strzyżenie");
        when(offer.getCost()).thenReturn(BigDecimal.valueOf(80));

        when(guestOrder.getIdGuestOrder()).thenReturn(10L);
        when(guestOrder.getEmail()).thenReturn("guest@example.com");
        when(guestOrder.getFirstname()).thenReturn("Jan");
        when(guestOrder.getOffer()).thenReturn(offer);
        when(guestOrder.getVisitDate()).thenReturn(
                LocalDateTime.of(2030, 1, 11, 12, 0)
        );

        when(payment.getPaymentMethod())
                .thenReturn(PaymentMethod.GOTOWKA);
        when(payment.getPaymentStatus())
                .thenReturn(PaymentStatus.NIE_WYMAGANA);

        // when
        guestOrderEvents.created(guestOrder, payment);

        // then
        verify(eventPublisher).publishEvent(
                isA(OrderCreatedEvent.class)
        );

        verify(eventPublisher, never()).publishEvent(
                isA(OnlinePaymentPendingEvent.class)
        );
    }
}