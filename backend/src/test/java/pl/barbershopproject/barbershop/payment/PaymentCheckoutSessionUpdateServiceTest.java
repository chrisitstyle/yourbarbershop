package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentCheckoutSessionUpdateServiceTest {

    private static final Long PAYMENT_ID = 10L;
    private static final String SESSION_ID = "cs_test_123";
    private static final String OTHER_SESSION_ID = "cs_test_456";

    @Mock
    private PaymentRepository paymentRepository;
    private PaymentCheckoutSessionUpdateService checkoutSessionUpdateService;

    @BeforeEach
    void setUp() {
        checkoutSessionUpdateService = new PaymentCheckoutSessionUpdateService(paymentRepository);
    }

    @Test
    void shouldAssignStripeCheckoutSessionToOnlinePayment() {
        Payment payment = createPayment(PaymentMethod.KARTA_ONLINE);

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        checkoutSessionUpdateService.assignSession(
                PAYMENT_ID,
                SESSION_ID
        );

        assertThat(payment.getStripeCheckoutSessionId())
                .isEqualTo(SESSION_ID);

        InOrder inOrder = inOrder(paymentRepository);

        inOrder.verify(paymentRepository).findById(PAYMENT_ID);
        inOrder.verify(paymentRepository).save(payment);
        inOrder.verifyNoMoreInteractions();
    }

    @ParameterizedTest
    @EnumSource(
            value = PaymentMethod.class,
            names = {
                    "GOTOWKA",
                    "KARTA_NA_MIEJSCU"
            }
    )
    void shouldRejectSessionForOfflinePayment(
            PaymentMethod paymentMethod
    ) {
        Payment payment = createPayment(paymentMethod);

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        assertThatThrownBy(() -> checkoutSessionUpdateService.assignSession(
                        PAYMENT_ID,
                        SESSION_ID
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage(
                        "Sesję Stripe można przypisać tylko do płatności online"
                );

        assertThat(payment.getStripeCheckoutSessionId()).isNull();

        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentRepository, never()).save(payment);
    }

    @Test
    void shouldThrowExceptionWhenPaymentDoesNotExist() {
        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                checkoutSessionUpdateService.assignSession(
                        PAYMENT_ID,
                        SESSION_ID)
        )
                .isInstanceOf(NoSuchElementException.class)
                .hasMessage(
                        "Nie znaleziono płatności o ID: " + PAYMENT_ID
                );

        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentRepository, never()).save(
                org.mockito.ArgumentMatchers.any(Payment.class));
    }

    @Test
    void shouldThrowExceptionWhenPaymentIdIsNull() {
        assertThatThrownBy(() ->
                checkoutSessionUpdateService.assignSession(
                        null,
                        SESSION_ID
                )
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage("Payment ID nie może być null");

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsNull() {
        assertThatThrownBy(() -> checkoutSessionUpdateService.assignSession(
                        PAYMENT_ID,
                        null)
        )
                .isInstanceOf(NullPointerException.class)
                .hasMessage(
                        "Stripe Checkout session ID nie może być null"
                );

        verifyNoInteractions(paymentRepository);
    }

    @Test
    void shouldThrowExceptionWhenSessionIdIsBlank() {
        assertThatThrownBy(() ->
                checkoutSessionUpdateService.assignSession(
                        PAYMENT_ID,
                        "   "
                )
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Stripe Checkout session ID nie może być pusty"
                );

        verifyNoInteractions(paymentRepository);
    }

    private Payment createPayment(PaymentMethod paymentMethod) {
        PaymentStatus paymentStatus = paymentMethod == PaymentMethod.KARTA_ONLINE
                        ? PaymentStatus.OCZEKUJE_NA_PLATNOSC
                        : PaymentStatus.NIE_WYMAGANA;

        return Payment.builder()
                .idPayment(PAYMENT_ID)
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .build();
    }

    @Test
    void shouldDoNothingWhenTheSameSessionIsAlreadyAssigned() {
        Payment payment = createPayment(PaymentMethod.KARTA_ONLINE);
        payment.setStripeCheckoutSessionId(SESSION_ID);

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        checkoutSessionUpdateService.assignSession(PAYMENT_ID, SESSION_ID);

        assertThat(payment.getStripeCheckoutSessionId())
                .isEqualTo(SESSION_ID);

        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void shouldRejectOverwritingExistingSessionWithAnotherSession() {
        Payment payment = createPayment(PaymentMethod.KARTA_ONLINE);
        payment.setStripeCheckoutSessionId(SESSION_ID);

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        assertThatThrownBy(() ->
                checkoutSessionUpdateService.assignSession(
                        PAYMENT_ID,
                        OTHER_SESSION_ID
                )
        )
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Płatność o ID: " + PAYMENT_ID + " ma już przypisaną inną sesję Stripe");

        assertThat(payment.getStripeCheckoutSessionId())
                .isEqualTo(SESSION_ID);

        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentRepository, never()).save(any(Payment.class));
    }
}
