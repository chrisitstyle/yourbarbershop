package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.exception.PaymentLinkUnavailableException;
import pl.barbershopproject.barbershop.payment.link.*;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutService;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutSessionDetails;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutSessionStatus;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentLinkServiceTest {

    private static final String TOKEN = "payment-link-token";
    private static final Long PAYMENT_ID = 15L;
    private static final String SESSION_ID = "cs_test_123";
    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";
    private static final String STRIPE_CHECKOUT_IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";
    private static final Instant NOW = Instant.parse("2030-01-10T12:00:00Z");

    @Mock
    private PaymentLinkTokenService paymentLinkTokenService;

    @Mock
    private PaymentLinkPaymentQueryService paymentQueryService;

    @Mock
    private PaymentCheckout paymentCheckout;

    @Mock
    private StripeCheckoutService stripeCheckoutService;

    private PaymentLinkService paymentLinkService;

    @BeforeEach
    void setUp() {
        paymentLinkService = new PaymentLinkService(
                paymentLinkTokenService,
                paymentQueryService,
                paymentCheckout,
                stripeCheckoutService,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }

    @Test
    void shouldReturnExistingCheckoutUrl_WhenStripeSessionIsOpen() {
        // given
        mockPaymentLinkToken();
        PaymentLinkPaymentData payment =
                pendingPayment(SESSION_ID);

        when(paymentQueryService.getPayment(PAYMENT_ID))
                .thenReturn(payment);

        when(stripeCheckoutService.retrieveCheckoutSession(
                SESSION_ID
        )).thenReturn(
                new StripeCheckoutSessionDetails(
                        SESSION_ID,
                        StripeCheckoutSessionStatus.OPEN,
                        "unpaid",
                        CHECKOUT_URL,
                        NOW.plusSeconds(3600)
                )
        );

        // when
        String result = paymentLinkService.resolveCheckoutUrl(TOKEN);

        // then
        assertThat(result).isEqualTo(CHECKOUT_URL);

        verifyNoInteractions(paymentCheckout);
    }

    @Test
    void shouldCreateCheckout_WhenPaymentHasNoStripeSession() {
        // given
        mockPaymentLinkToken();
        PaymentLinkPaymentData payment = pendingPayment(null);

        when(paymentQueryService.getPayment(PAYMENT_ID))
                .thenReturn(payment);

        when(paymentCheckout.createCheckoutIfRequired(
                payment.checkoutRequest()
        )).thenReturn(CHECKOUT_URL);

        // when
        String result =
                paymentLinkService.resolveCheckoutUrl(TOKEN);

        // then
        assertThat(result).isEqualTo(CHECKOUT_URL);

        verifyNoInteractions(stripeCheckoutService);
    }

    @Test
    void shouldRejectPaymentLink_WhenPaymentIsAlreadyPaid() {
        // given
        mockPaymentLinkToken();

        PaymentLinkPaymentData payment =
                paymentWithStatus(
                        PaymentStatus.OPLACONA,
                        SESSION_ID
                );

        when(paymentQueryService.getPayment(PAYMENT_ID))
                .thenReturn(payment);

        // when then
        assertThatThrownBy(() -> paymentLinkService.resolveCheckoutUrl(TOKEN))
                .isInstanceOf(
                        PaymentLinkUnavailableException.class)
                .hasMessage(
                        "Płatność została już opłacona");

        verifyNoInteractions(
                paymentCheckout,
                stripeCheckoutService);
    }

    @Test
    void shouldRejectPaymentLink_WhenOrderIsCancelled() {
        // given
        mockPaymentLinkToken();

        PaymentLinkPaymentData payment =
                new PaymentLinkPaymentData(
                        checkoutRequest(
                                PaymentStatus.OCZEKUJE_NA_PLATNOSC
                        ),
                        SESSION_ID,
                        OrderStatus.ANULOWANE,
                        futureVisitDate()
                );

        when(paymentQueryService.getPayment(PAYMENT_ID))
                .thenReturn(payment);

        // when then
        assertThatThrownBy(() -> paymentLinkService.resolveCheckoutUrl(TOKEN)
        )
                .isInstanceOf(
                        PaymentLinkUnavailableException.class
                )
                .hasMessage(
                        "Nie można opłacić anulowanej wizyty"
                );

        verifyNoInteractions(
                paymentCheckout,
                stripeCheckoutService);
    }

    @Test
    void shouldRejectPaymentLink_WhenStripeSessionIsExpired() {
        // given
        mockPaymentLinkToken();

        when(paymentQueryService.getPayment(PAYMENT_ID))
                .thenReturn(pendingPayment(SESSION_ID));

        when(stripeCheckoutService.retrieveCheckoutSession(
                SESSION_ID
        )).thenReturn(
                new StripeCheckoutSessionDetails(
                        SESSION_ID,
                        StripeCheckoutSessionStatus.EXPIRED,
                        "unpaid",
                        null,
                        NOW.minusSeconds(1)
                )
        );

        // when then
        assertThatThrownBy(() -> paymentLinkService.resolveCheckoutUrl(TOKEN)
        )
                .isInstanceOf(
                        PaymentLinkUnavailableException.class
                )
                .hasMessage(
                        "Sesja płatności wygasła");

        verifyNoInteractions(paymentCheckout);
    }

    private void mockPaymentLinkToken() {
        when(paymentLinkTokenService.verifyToken(TOKEN))
                .thenReturn(
                        new PaymentLinkToken(
                                PAYMENT_ID,
                                NOW.plusSeconds(3600)));
    }

    private PaymentLinkPaymentData pendingPayment(
            String sessionId
    ) {
        return paymentWithStatus(
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                sessionId);
    }

    private PaymentLinkPaymentData paymentWithStatus(
            PaymentStatus paymentStatus,
            String sessionId
    ) {
        return new PaymentLinkPaymentData(
                checkoutRequest(paymentStatus),
                sessionId,
                OrderStatus.NOWE,
                futureVisitDate()
        );
    }

    private PaymentCheckoutRequest checkoutRequest(
            PaymentStatus paymentStatus
    ) {
        return new PaymentCheckoutRequest(
                PAYMENT_ID,
                PaymentMethod.KARTA_ONLINE,
                paymentStatus,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                BigDecimal.valueOf(80),
                "PLN",
                "Strzyżenie"
        );
    }

    private LocalDateTime futureVisitDate() {
        return LocalDateTime.of(
                2030,
                Month.JANUARY,
                11,
                12,
                0);
    }
}
