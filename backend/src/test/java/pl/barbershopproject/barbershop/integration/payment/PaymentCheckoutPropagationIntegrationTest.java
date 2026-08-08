package pl.barbershopproject.barbershop.integration.payment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.payment.*;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class PaymentCheckoutPropagationIntegrationTest extends BaseIntegrationTest {

    private static final Long PAYMENT_ID = 10L;
    private static final String SESSION_ID = "cs_test_123";
    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";
    private static final String STRIPE_CHECKOUT_IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";

    @Autowired
    private PaymentCheckout paymentCheckout;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockitoBean
    private StripeCheckoutService stripeCheckoutService;

    @MockitoBean
    private PaymentCheckoutSessionUpdater checkoutSessionUpdater;

    @Test
    void shouldCreateCheckoutOutsideTransaction() {
        PaymentCheckoutRequest checkoutRequest = createOnlineCheckoutRequest();

        when(stripeCheckoutService.createCheckoutSession(checkoutRequest))
                .thenAnswer(invocation -> {
                    assertThat(TransactionSynchronizationManager.isActualTransactionActive())
                            .isFalse();

                    return new StripeCheckoutSessionResponse(
                            SESSION_ID,
                            CHECKOUT_URL
                    );
                });

        String result = paymentCheckout.createCheckoutIfRequired(checkoutRequest);

        assertThat(result).isEqualTo(CHECKOUT_URL);

        verify(stripeCheckoutService)
                .createCheckoutSession(checkoutRequest);

        verify(checkoutSessionUpdater)
                .assignSession(PAYMENT_ID, SESSION_ID);
    }

    @Test
    void shouldRejectCheckoutInsideActiveTransaction() {
        PaymentCheckoutRequest checkoutRequest =
                createOnlineCheckoutRequest();

        TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);

        assertThatThrownBy(() -> transactionTemplate.executeWithoutResult(status -> {
                    assertThat(TransactionSynchronizationManager
                                    .isActualTransactionActive()
                    ).isTrue();

                    paymentCheckout.createCheckoutIfRequired(
                            checkoutRequest
                    );
                })
        )
                .isInstanceOf(IllegalTransactionStateException.class);

        verifyNoInteractions(
                stripeCheckoutService,
                checkoutSessionUpdater
        );
    }

    private PaymentCheckoutRequest createOnlineCheckoutRequest() {
        return new PaymentCheckoutRequest(
                PAYMENT_ID,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                new BigDecimal("120.00"),
                "PLN",
                "Strzyżenie męskie"
        );
    }
}
