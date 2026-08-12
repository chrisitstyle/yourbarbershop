package pl.barbershopproject.barbershop.ordercreation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.idempotency.IdempotencyResolution;
import pl.barbershopproject.barbershop.payment.PaymentCheckout;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class OrderCreationCompletionHandlerTest {

    private static final Long IDEMPOTENCY_REQUEST_ID = 100L;
    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";

    private PaymentCheckout paymentCheckout;
    private IdempotencyRequestManager idempotencyRequestManager;

    private OrderCreationCompletionHandler orderCreationCompletionHandler;

    @BeforeEach
    void setUp() {
        paymentCheckout = mock(PaymentCheckout.class);

        idempotencyRequestManager = mock(IdempotencyRequestManager.class);

        orderCreationCompletionHandler =
                new OrderCreationCompletionHandler(
                        paymentCheckout,
                        idempotencyRequestManager
                );
    }

    @Test
    void shouldThrowExceptionWhenRequestIsStillInProgress() {
        CreationTransactionResult transactionResult = mock(CreationTransactionResult.class);

        when(transactionResult.isInProgress())
                .thenReturn(true);

        IdempotencyConflictException exception = assertThrows(
                        IdempotencyConflictException.class,
                        () -> orderCreationCompletionHandler.complete(
                                transactionResult));

        assertEquals("Żądanie z tym Idempotency-Key jest nadal przetwarzane",
                exception.getMessage());

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager
        );
    }

    @Test
    void shouldReturnStoredCheckoutUrlWhenRequestIsCompleted() {
        CreationTransactionResult transactionResult = mock(CreationTransactionResult.class);

        when(transactionResult.isInProgress())
                .thenReturn(false);

        when(transactionResult.isCompleted())
                .thenReturn(true);

        when(transactionResult.checkoutUrl())
                .thenReturn(CHECKOUT_URL);

        String result = orderCreationCompletionHandler.complete(transactionResult);

        assertEquals(CHECKOUT_URL, result);

        verifyNoInteractions(
                paymentCheckout,
                idempotencyRequestManager
        );
    }

    @Test
    void shouldCreateCheckoutAndMarkRequestCompletedWhenResourceWasCreated() {
        CreationTransactionResult transactionResult = mock(CreationTransactionResult.class);

        PaymentCheckoutRequest checkoutRequest = new PaymentCheckoutRequest(
                        10L,
                        PaymentMethod.KARTA_ONLINE,
                        PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                        "550e8400-e29b-41d4-a716-446655440000",
                        new BigDecimal("150.00"),
                        "PLN",
                        "Strzyżenie"
                );

        when(transactionResult.resolution())
                .thenReturn(IdempotencyResolution.RESOURCE_CREATED);

        when(transactionResult.checkoutRequest())
                .thenReturn(checkoutRequest);

        when(transactionResult.idempotencyRequestId())
                .thenReturn(IDEMPOTENCY_REQUEST_ID);

        when(paymentCheckout.createCheckoutIfRequired(
                checkoutRequest
        )).thenReturn(CHECKOUT_URL);

        String result = orderCreationCompletionHandler.complete(
                        transactionResult);

        assertEquals(CHECKOUT_URL, result);

        verify(paymentCheckout).createCheckoutIfRequired(checkoutRequest);

        verify(idempotencyRequestManager).markCompleted(
                        IDEMPOTENCY_REQUEST_ID,
                        CHECKOUT_URL);
    }
}
