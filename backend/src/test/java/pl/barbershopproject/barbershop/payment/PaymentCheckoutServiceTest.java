package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCheckoutServiceTest {

    private static final Long PAYMENT_ID = 10L;
    private static final String SESSION_ID = "cs_test_123";
    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";

    @Mock
    private StripeCheckoutService stripeCheckoutService;
    @Mock
    private PaymentCheckoutSessionUpdater checkoutSessionUpdater;

    private PaymentCheckoutService paymentCheckoutService;

    @BeforeEach
    void setUp() {
        paymentCheckoutService = new PaymentCheckoutService(
                stripeCheckoutService,
                checkoutSessionUpdater
        );
    }

    @Test
    void shouldCreateCheckoutAndAssignSessionForOnlinePayment() {
        PaymentCheckoutRequest checkoutRequest = createOnlineCheckoutRequest();

        StripeCheckoutSessionResponse checkoutSession = new StripeCheckoutSessionResponse(
                        SESSION_ID,
                        CHECKOUT_URL);

        when(stripeCheckoutService.createCheckoutSession(checkoutRequest))
                .thenReturn(checkoutSession);

        String result = paymentCheckoutService.createCheckoutIfRequired(
                checkoutRequest
        );

        assertThat(result).isEqualTo(CHECKOUT_URL);

        InOrder inOrder = inOrder(
                stripeCheckoutService,
                checkoutSessionUpdater
        );

        inOrder.verify(stripeCheckoutService)
                .createCheckoutSession(checkoutRequest);

        inOrder.verify(checkoutSessionUpdater)
                .assignSession(PAYMENT_ID, SESSION_ID);

        inOrder.verifyNoMoreInteractions();
    }

    @Test
    void shouldNotCreateCheckoutForCashPayment() {
        PaymentCheckoutRequest checkoutRequest =
                createOfflineCheckoutRequest(PaymentMethod.GOTOWKA);

        String result = paymentCheckoutService.createCheckoutIfRequired(
                checkoutRequest
        );

        assertThat(result).isNull();

        verifyNoInteractions(stripeCheckoutService,
                checkoutSessionUpdater);
    }

    @Test
    void shouldNotCreateCheckoutForOnSiteCardPayment() {
        PaymentCheckoutRequest checkoutRequest = createOfflineCheckoutRequest(
                        PaymentMethod.KARTA_NA_MIEJSCU);

        String result = paymentCheckoutService.createCheckoutIfRequired(
                checkoutRequest
        );

        assertThat(result).isNull();

        verifyNoInteractions(
                stripeCheckoutService,
                checkoutSessionUpdater
        );
    }

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        assertThatThrownBy(() ->
                paymentCheckoutService.createCheckoutIfRequired(null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("PaymentCheckoutRequest nie może być null");

        verifyNoInteractions(stripeCheckoutService, checkoutSessionUpdater);
    }

    @Test
    void shouldThrowExceptionWhenStripeReturnsNullSession() {
        PaymentCheckoutRequest checkoutRequest =
                createOnlineCheckoutRequest();

        when(stripeCheckoutService.createCheckoutSession(checkoutRequest))
                .thenReturn(null);

        assertThatThrownBy(() ->
                paymentCheckoutService.createCheckoutIfRequired(
                        checkoutRequest
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Stripe Checkout session nie może być null"
                );

        verifyNoInteractions(checkoutSessionUpdater);
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsBlank() {
        PaymentCheckoutRequest checkoutRequest =
                createOnlineCheckoutRequest();

        StripeCheckoutSessionResponse checkoutSession =
                new StripeCheckoutSessionResponse(
                        "   ",
                        CHECKOUT_URL
                );

        when(stripeCheckoutService.createCheckoutSession(checkoutRequest))
                .thenReturn(checkoutSession);

        assertThatThrownBy(() ->
                paymentCheckoutService.createCheckoutIfRequired(
                        checkoutRequest
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Stripe Checkout session ID nie może być pusty"
                );

        verifyNoInteractions(checkoutSessionUpdater);
    }

    @Test
    void shouldThrowExceptionWhenCheckoutUrlIsBlank() {
        PaymentCheckoutRequest checkoutRequest =
                createOnlineCheckoutRequest();

        StripeCheckoutSessionResponse checkoutSession =
                new StripeCheckoutSessionResponse(
                        SESSION_ID,
                        "   "
                );

        when(stripeCheckoutService.createCheckoutSession(checkoutRequest))
                .thenReturn(checkoutSession);

        assertThatThrownBy(() ->
                paymentCheckoutService.createCheckoutIfRequired(
                        checkoutRequest
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Stripe Checkout URL nie może być pusty"
                );

        verifyNoInteractions(checkoutSessionUpdater);
    }

    private PaymentCheckoutRequest createOnlineCheckoutRequest() {
        return new PaymentCheckoutRequest(
                PAYMENT_ID,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                new BigDecimal("120.00"),
                "PLN",
                "Strzyżenie męskie"
        );
    }

    private PaymentCheckoutRequest createOfflineCheckoutRequest(PaymentMethod paymentMethod) {
        return new PaymentCheckoutRequest(
                PAYMENT_ID,
                paymentMethod,
                PaymentStatus.NIE_WYMAGANA,
                new BigDecimal("120.00"),
                "PLN",
                "Strzyżenie męskie"
        );
    }
}
