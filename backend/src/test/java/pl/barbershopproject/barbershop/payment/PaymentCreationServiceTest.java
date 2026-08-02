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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.guestOrderBuilder;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.offerBuilder;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.orderBuilder;

@ExtendWith(MockitoExtension.class)
class PaymentCreationServiceTest {

    private static final LocalDateTime PAYMENT_CREATED_AT =
            LocalDateTime.of(2026, Month.AUGUST, 2, 20, 0);

    private static final String STRIPE_SESSION_ID = "cs_test_123";

    private static final String CHECKOUT_URL =
            "https://checkout.stripe.com/c/pay/cs_test_123";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private StripeCheckoutService stripeCheckoutService;

    private PaymentCreationService paymentCreationService;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(
                PAYMENT_CREATED_AT.toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );

        paymentCreationService = new PaymentCreationService(
                paymentRepository,
                stripeCheckoutService,
                clock,
                " pln "
        );
    }

    @Test
    void shouldCreateOnlinePaymentForOrderAndReturnCheckoutUrl() {
        Offer offer = createOffer();

        Order order = orderBuilder()
                .offer(offer)
                .build();

        givenRepositoryReturnsPassedPayment();
        givenStripeReturnsCheckoutSession(offer);

        PaymentCreationResult result =
                paymentCreationService.createForOrder(
                        order,
                        offer,
                        PaymentMethod.KARTA_ONLINE
                );

        Payment payment = result.payment();

        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getGuestOrder()).isNull();

        assertThat(payment.getPaymentMethod())
                .isEqualTo(PaymentMethod.KARTA_ONLINE);

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.OCZEKUJE_NA_PLATNOSC);

        assertThat(payment.getAmount())
                .isEqualByComparingTo(offer.getCost());

        assertThat(payment.getCurrency()).isEqualTo("PLN");
        assertThat(payment.getCreatedAt()).isEqualTo(PAYMENT_CREATED_AT);

        assertThat(payment.getStripeCheckoutSessionId())
                .isEqualTo(STRIPE_SESSION_ID);

        assertThat(payment.isForOrder()).isTrue();
        assertThat(payment.isForGuestOrder()).isFalse();

        assertThat(order.getPayment()).isSameAs(payment);
        assertThat(result.checkoutUrl()).isEqualTo(CHECKOUT_URL);

        verify(paymentRepository, times(1)).save(same(payment));

        verify(stripeCheckoutService).createCheckoutSession(
                same(payment),
                same(offer)
        );
    }

    @Test
    void shouldCreateOnlinePaymentForGuestOrderAndReturnCheckoutUrl() {
        Offer offer = createOffer();

        GuestOrder guestOrder = guestOrderBuilder()
                .offer(offer)
                .build();

        givenRepositoryReturnsPassedPayment();
        givenStripeReturnsCheckoutSession(offer);

        PaymentCreationResult result =
                paymentCreationService.createForGuestOrder(
                        guestOrder,
                        offer,
                        PaymentMethod.KARTA_ONLINE
                );

        Payment payment = result.payment();

        assertThat(payment.getOrder()).isNull();
        assertThat(payment.getGuestOrder()).isSameAs(guestOrder);

        assertThat(payment.getPaymentMethod())
                .isEqualTo(PaymentMethod.KARTA_ONLINE);

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.OCZEKUJE_NA_PLATNOSC);

        assertThat(payment.getAmount())
                .isEqualByComparingTo(offer.getCost());

        assertThat(payment.getCurrency()).isEqualTo("PLN");
        assertThat(payment.getCreatedAt()).isEqualTo(PAYMENT_CREATED_AT);

        assertThat(payment.getStripeCheckoutSessionId())
                .isEqualTo(STRIPE_SESSION_ID);

        assertThat(payment.isForOrder()).isFalse();
        assertThat(payment.isForGuestOrder()).isTrue();

        assertThat(guestOrder.getPayment()).isSameAs(payment);
        assertThat(result.checkoutUrl()).isEqualTo(CHECKOUT_URL);

        verify(paymentRepository, times(1)).save(same(payment));

        verify(stripeCheckoutService).createCheckoutSession(
                same(payment),
                same(offer)
        );
    }

    @ParameterizedTest
    @EnumSource(
            value = PaymentMethod.class,
            names = {
                    "GOTOWKA",
                    "KARTA_NA_MIEJSCU"
            }
    )
    void shouldCreateOfflinePaymentWithoutCallingStripe(
            PaymentMethod paymentMethod
    ) {
        Offer offer = createOffer();

        Order order = orderBuilder()
                .offer(offer)
                .build();

        givenRepositoryReturnsPassedPayment();

        PaymentCreationResult result =
                paymentCreationService.createForOrder(
                        order,
                        offer,
                        paymentMethod
                );

        Payment payment = result.payment();

        assertThat(payment.getOrder()).isSameAs(order);
        assertThat(payment.getGuestOrder()).isNull();

        assertThat(payment.getPaymentMethod())
                .isEqualTo(paymentMethod);

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.NIE_WYMAGANA);

        assertThat(payment.getAmount())
                .isEqualByComparingTo(offer.getCost());

        assertThat(payment.getCurrency()).isEqualTo("PLN");
        assertThat(payment.getCreatedAt()).isEqualTo(PAYMENT_CREATED_AT);

        assertThat(payment.getStripeCheckoutSessionId()).isNull();
        assertThat(result.checkoutUrl()).isNull();

        assertThat(order.getPayment()).isSameAs(payment);

        verify(paymentRepository, times(1)).save(same(payment));
        verifyNoInteractions(stripeCheckoutService);
    }

    @Test
    void shouldThrowExceptionWhenOrderIsNull() {
        Offer offer = createOffer();

        assertThatThrownBy(() ->
                paymentCreationService.createForOrder(
                        null,
                        offer,
                        PaymentMethod.GOTOWKA
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Order nie może być null");

        verifyNoInteractions(
                paymentRepository,
                stripeCheckoutService
        );
    }

    @Test
    void shouldThrowExceptionWhenGuestOrderIsNull() {
        Offer offer = createOffer();

        assertThatThrownBy(() ->
                paymentCreationService.createForGuestOrder(
                        null,
                        offer,
                        PaymentMethod.GOTOWKA
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("GuestOrder nie może być null");

        verifyNoInteractions(
                paymentRepository,
                stripeCheckoutService
        );
    }

    @Test
    void shouldThrowExceptionWhenOfferIsNull() {
        Order order = orderBuilder().build();

        assertThatThrownBy(() ->
                paymentCreationService.createForOrder(
                        order,
                        null,
                        PaymentMethod.GOTOWKA
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Offer nie może być null");

        verifyNoInteractions(
                paymentRepository,
                stripeCheckoutService
        );
    }

    @Test
    void shouldThrowExceptionWhenOfferCostIsNull() {
        Offer offer = offerBuilder()
                .cost(null)
                .build();

        Order order = orderBuilder()
                .offer(offer)
                .build();

        assertThatThrownBy(() ->
                paymentCreationService.createForOrder(
                        order,
                        offer,
                        PaymentMethod.GOTOWKA
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Koszt oferty nie może być null");

        verifyNoInteractions(
                paymentRepository,
                stripeCheckoutService
        );
    }

    @Test
    void shouldThrowExceptionWhenPaymentMethodIsNull() {
        Offer offer = createOffer();

        Order order = orderBuilder()
                .offer(offer)
                .build();

        assertThatThrownBy(() ->
                paymentCreationService.createForOrder(
                        order,
                        offer,
                        null
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("PaymentMethod nie może być null");

        verifyNoInteractions(
                paymentRepository,
                stripeCheckoutService
        );
    }

    @Test
    void shouldThrowExceptionWhenStripeReturnsNullResponse() {
        Offer offer = createOffer();

        Order order = orderBuilder()
                .offer(offer)
                .build();

        givenRepositoryReturnsPassedPayment();

        when(stripeCheckoutService.createCheckoutSession(
                any(Payment.class),
                same(offer)
        )).thenReturn(null);

        assertThatThrownBy(() ->
                paymentCreationService.createForOrder(
                        order,
                        offer,
                        PaymentMethod.KARTA_ONLINE
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Stripe Checkout session nie może być null");

        verify(paymentRepository).save(any(Payment.class));

        verify(stripeCheckoutService).createCheckoutSession(
                any(Payment.class),
                same(offer)
        );
    }

    @Test
    void shouldThrowExceptionWhenStripeSessionIdIsNull() {
        Offer offer = createOffer();

        Order order = orderBuilder()
                .offer(offer)
                .build();

        givenRepositoryReturnsPassedPayment();

        when(stripeCheckoutService.createCheckoutSession(
                any(Payment.class),
                same(offer)
        )).thenReturn(new StripeCheckoutSessionResponse(
                null,
                CHECKOUT_URL
        ));

        assertThatThrownBy(() ->
                paymentCreationService.createForOrder(
                        order,
                        offer,
                        PaymentMethod.KARTA_ONLINE
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Stripe Checkout session ID nie może być null");

        verify(paymentRepository).save(any(Payment.class));

        verify(stripeCheckoutService).createCheckoutSession(
                any(Payment.class),
                same(offer)
        );
    }

    @Test
    void shouldRejectNullCurrency() {
        Clock clock = Clock.fixed(
                PAYMENT_CREATED_AT.toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );

        assertThatThrownBy(() ->
                new PaymentCreationService(
                        paymentRepository,
                        stripeCheckoutService,
                        clock,
                        null
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Waluta nie może być null");
    }

    @Test
    void shouldRejectBlankCurrency() {
        Clock clock = Clock.fixed(
                PAYMENT_CREATED_AT.toInstant(ZoneOffset.UTC),
                ZoneOffset.UTC
        );

        assertThatThrownBy(() ->
                new PaymentCreationService(
                        paymentRepository,
                        stripeCheckoutService,
                        clock,
                        "   "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Waluta nie może być pusta");
    }

    private void givenRepositoryReturnsPassedPayment() {
        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    private void givenStripeReturnsCheckoutSession(Offer offer) {
        when(stripeCheckoutService.createCheckoutSession(
                any(Payment.class),
                same(offer)
        )).thenReturn(new StripeCheckoutSessionResponse(
                STRIPE_SESSION_ID,
                CHECKOUT_URL
        ));
    }
}