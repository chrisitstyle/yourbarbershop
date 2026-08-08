package pl.barbershopproject.barbershop.order.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.event.OnlinePaymentPendingEvent;
import pl.barbershopproject.barbershop.user.User;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.mockito.Mockito.*;

class OrderEventsTest {

    private ApplicationEventPublisher eventPublisher;
    private OrderEvents orderEvents;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        orderEvents = new OrderEvents(eventPublisher);
    }

    @Test
    void shouldPublishOnlinePaymentPendingEvent_ForOnlinePayment() {
        // given
        User user = mock(User.class);
        Offer offer = mock(Offer.class);
        BookedOffer bookedOffer = mock(BookedOffer.class);
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        LocalDateTime visitDate =
                LocalDateTime.of(2030, 1, 11, 12, 0);

        when(user.getEmail()).thenReturn("customer@example.com");
        when(user.getFirstname()).thenReturn("Jan");

        when(offer.getKind()).thenReturn("Strzyżenie");
        when(offer.getCost()).thenReturn(BigDecimal.valueOf(80));

        when(bookedOffer.getName()).thenReturn("Strzyżenie");
        when(bookedOffer.getPrice()).thenReturn(BigDecimal.valueOf(80));

        when(order.getIdOrder()).thenReturn(10L);
        when(order.getUser()).thenReturn(user);
        when(order.getOffer()).thenReturn(offer);
        when(order.getBookedOffer()).thenReturn(bookedOffer);
        when(order.getVisitDate()).thenReturn(visitDate);

        when(payment.getIdPayment()).thenReturn(15L);
        when(payment.getPaymentMethod())
                .thenReturn(PaymentMethod.KARTA_ONLINE);

        // when
        orderEvents.created(order, payment);

        // then
        verify(eventPublisher).publishEvent(
                argThat((Object event) ->
                        event instanceof OnlinePaymentPendingEvent(
                                Long paymentId, String email, String firstname, LocalDateTime date, String offerName,
                                BigDecimal offerCost
                        )
                                && paymentId.equals(15L)
                                && email.equals("customer@example.com")
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
        User user = mock(User.class);
        Offer offer = mock(Offer.class);
        Order order = mock(Order.class);
        Payment payment = mock(Payment.class);

        when(user.getEmail()).thenReturn("customer@example.com");
        when(user.getFirstname()).thenReturn("Jan");

        when(offer.getKind()).thenReturn("Strzyżenie");
        when(offer.getCost()).thenReturn(BigDecimal.valueOf(80));

        when(order.getIdOrder()).thenReturn(10L);
        when(order.getUser()).thenReturn(user);
        when(order.getOffer()).thenReturn(offer);
        when(order.getVisitDate()).thenReturn(
                LocalDateTime.of(2030, 1, 11, 12, 0)
        );

        when(payment.getPaymentMethod())
                .thenReturn(PaymentMethod.GOTOWKA);
        when(payment.getPaymentStatus())
                .thenReturn(PaymentStatus.NIE_WYMAGANA);

        // when
        orderEvents.created(order, payment);

        // then
        verify(eventPublisher).publishEvent(
                isA(OrderCreatedEvent.class)
        );

        verify(eventPublisher, never()).publishEvent(
                isA(OnlinePaymentPendingEvent.class)
        );
    }
}