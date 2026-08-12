package pl.barbershopproject.barbershop.ordercreation;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.payment.PaymentCheckout;

/**
 * Completes the shared post-transaction processing for idempotent order creation.
 * It resolves in-progress and completed requests, creates a payment checkout when
 * required, and marks newly created resources as completed.
 */
@Component
@RequiredArgsConstructor
public class OrderCreationCompletionHandler {

    private final PaymentCheckout paymentCheckout;
    private final IdempotencyRequestManager idempotencyRequestManager;

    /**
     * Completes the creation flow represented by the supplied transaction result.
     *
     * @param transactionResult result returned by the order creation transaction
     * @return existing or newly created checkout URL, or {@code null} when checkout is not required
     * @throws IdempotencyConflictException when the same idempotent request is still being processed
     */
    public String complete(
            CreationTransactionResult transactionResult
    ) {
        if (transactionResult.isInProgress()) {
            throw new IdempotencyConflictException(
                    "Żądanie z tym Idempotency-Key jest nadal przetwarzane");
        }

        if (transactionResult.isCompleted()) {
            return transactionResult.checkoutUrl();
        }

        String checkoutUrl = paymentCheckout.createCheckoutIfRequired(
                        transactionResult.checkoutRequest());

        idempotencyRequestManager.markCompleted(
                transactionResult.idempotencyRequestId(),
                checkoutUrl);

        return checkoutUrl;
    }
}
