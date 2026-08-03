package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.guestOrderBuilder;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.orderBuilder;

@ExtendWith(MockitoExtension.class)
class PaymentCreationServiceTest {

    private static final Long PAYMENT_ID = 15L;

    private static final LocalDateTime PAYMENT_CREATED_AT = LocalDateTime
            .of(2026, Month.AUGUST, 2, 20, 0);

    @Mock
    private PaymentRepository paymentRepository;

    private PaymentCreationService paymentCreationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(PAYMENT_CREATED_AT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        paymentCreationService = new PaymentCreationService(paymentRepository, clock, " pln ");
    }

    @Test
    void shouldCreateOnlinePaymentForOrder() {
        Offer offer = createOffer();

        Order order = orderBuilder().offer(offer).bookedOffer(createBookedOffer(offer)).build();

        givenRepositoryReturnsPaymentWithId();

        PaymentCreationResult result = paymentCreationService.createForOrder(order, PaymentMethod.KARTA_ONLINE);

        Payment payment = result.payment();
        PaymentCheckoutRequest checkoutRequest = result.checkoutRequest();

        assertThat(payment.getIdPayment()).isEqualTo(PAYMENT_ID);
        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getGuestOrder()).isNull();
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.KARTA_ONLINE);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.OCZEKUJE_NA_PLATNOSC);
        assertThat(payment.getAmount()).isEqualByComparingTo(order.getBookedOffer().getPrice());
        assertThat(payment.getCurrency()).isEqualTo("PLN");
        assertThat(payment.getCreatedAt()).isEqualTo(PAYMENT_CREATED_AT);
        assertThat(payment.getStripeCheckoutSessionId()).isNull();

        assertThat(payment.isForOrder()).isTrue();
        assertThat(payment.isForGuestOrder()).isFalse();
        assertThat(order.getPayment()).isSameAs(payment);

        assertThat(checkoutRequest.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(checkoutRequest.paymentMethod()).isEqualTo(PaymentMethod.KARTA_ONLINE);
        assertThat(checkoutRequest.paymentStatus()).isEqualTo(PaymentStatus.OCZEKUJE_NA_PLATNOSC);
        assertThat(checkoutRequest.amount()).isEqualByComparingTo(order.getBookedOffer().getPrice());
        assertThat(checkoutRequest.currency()).isEqualTo("PLN");
        assertThat(checkoutRequest.productName()).isEqualTo(order.getBookedOffer().getName());
        assertThat(checkoutRequest.requiresOnlineCheckout()).isTrue();

        verify(paymentRepository).saveAndFlush(same(payment));
    }

    @Test
    void shouldCreateOnlinePaymentForGuestOrder() {
        Offer offer = createOffer();

        GuestOrder guestOrder = guestOrderBuilder().offer(offer).bookedOffer(createBookedOffer(offer)).build();

        givenRepositoryReturnsPaymentWithId();

        PaymentCreationResult result = paymentCreationService
                .createForGuestOrder(guestOrder, PaymentMethod.KARTA_ONLINE);

        Payment payment = result.payment();
        PaymentCheckoutRequest checkoutRequest = result.checkoutRequest();

        assertThat(payment.getIdPayment()).isEqualTo(PAYMENT_ID);
        assertThat(payment.getOrder()).isNull();
        assertThat(payment.getGuestOrder()).isSameAs(guestOrder);
        assertThat(payment.getPaymentMethod()).isEqualTo(PaymentMethod.KARTA_ONLINE);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.OCZEKUJE_NA_PLATNOSC);
        assertThat(payment.getAmount()).isEqualByComparingTo(guestOrder.getBookedOffer().getPrice());
        assertThat(payment.getCurrency()).isEqualTo("PLN");
        assertThat(payment.getCreatedAt()).isEqualTo(PAYMENT_CREATED_AT);
        assertThat(payment.getStripeCheckoutSessionId()).isNull();

        assertThat(payment.isForOrder()).isFalse();
        assertThat(payment.isForGuestOrder()).isTrue();
        assertThat(guestOrder.getPayment()).isSameAs(payment);

        assertThat(checkoutRequest.paymentId()).isEqualTo(PAYMENT_ID);
        assertThat(checkoutRequest.productName()).isEqualTo(guestOrder.getBookedOffer().getName());
        assertThat(checkoutRequest.requiresOnlineCheckout()).isTrue();

        verify(paymentRepository).saveAndFlush(same(payment));
    }

    @ParameterizedTest
    @EnumSource(value = PaymentMethod.class, names = {"GOTOWKA", "KARTA_NA_MIEJSCU"})
    void shouldCreateOfflinePaymentWithCheckoutRequest(PaymentMethod paymentMethod) {
        Offer offer = createOffer();

        Order order = orderBuilder().offer(offer).bookedOffer(createBookedOffer(offer)).build();

        givenRepositoryReturnsPaymentWithId();

        PaymentCreationResult result = paymentCreationService.createForOrder(order, paymentMethod);

        Payment payment = result.payment();
        PaymentCheckoutRequest checkoutRequest = result.checkoutRequest();

        assertThat(payment.getPaymentMethod()).isEqualTo(paymentMethod);
        assertThat(payment.getPaymentStatus()).isEqualTo(PaymentStatus.NIE_WYMAGANA);
        assertThat(payment.getStripeCheckoutSessionId()).isNull();

        assertThat(checkoutRequest.paymentMethod()).isEqualTo(paymentMethod);
        assertThat(checkoutRequest.paymentStatus()).isEqualTo(PaymentStatus.NIE_WYMAGANA);
        assertThat(checkoutRequest.requiresOnlineCheckout()).isFalse();

        verify(paymentRepository).saveAndFlush(same(payment));
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        assertThatThrownBy(() -> paymentCreationService.createForOrder(
                null,
                PaymentMethod.GOTOWKA))
                .isInstanceOf(NullPointerException.class).hasMessage("Order nie może być null");

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldThrowExceptionWhenGuestOrderIsNull() {
        assertThatThrownBy(() -> paymentCreationService.createForGuestOrder(null, PaymentMethod.GOTOWKA))
                .isInstanceOf(NullPointerException.class).hasMessage("GuestOrder nie może być null");

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldThrowExceptionWhenOrderHasNoBookedOffer() {
        Order order = orderBuilder().bookedOffer(null).build();

        assertThatThrownBy(() -> paymentCreationService.createForOrder(order, PaymentMethod.GOTOWKA))
                .isInstanceOf(NullPointerException.class).hasMessage("BookedOffer nie może być null");

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldThrowExceptionWhenPaymentMethodIsNull() {
        Offer offer = createOffer();

        Order order = orderBuilder().offer(offer).bookedOffer(createBookedOffer(offer)).build();

        assertThatThrownBy(() -> paymentCreationService.createForOrder(order, null))
                .isInstanceOf(NullPointerException.class).hasMessage("PaymentMethod nie może być null");

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldRejectNullCurrency() {
        Clock clock = Clock.fixed(PAYMENT_CREATED_AT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        assertThatThrownBy(() -> new PaymentCreationService(paymentRepository, clock, null))
                .isInstanceOf(NullPointerException.class).hasMessage("Waluta nie może być null");
    }

    @Test
    void shouldRejectBlankCurrency() {
        Clock clock = Clock.fixed(PAYMENT_CREATED_AT.toInstant(ZoneOffset.UTC), ZoneOffset.UTC);

        assertThatThrownBy(() -> new PaymentCreationService(paymentRepository, clock, "   "))
                .isInstanceOf(IllegalArgumentException.class).hasMessage("Waluta nie może być pusta");
    }

    private void givenRepositoryReturnsPaymentWithId() {
        when(paymentRepository.saveAndFlush(any(Payment.class))).thenAnswer(invocation -> {
            Payment payment = invocation.getArgument(0);
            payment.setIdPayment(PAYMENT_ID);
            return payment;
        });
    }
}