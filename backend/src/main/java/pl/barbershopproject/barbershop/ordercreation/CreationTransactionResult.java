package pl.barbershopproject.barbershop.ordercreation;

import pl.barbershopproject.barbershop.idempotency.IdempotencyResolution;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

/**
 * Defines the common result contract for idempotent order creation flows.
 * Implementations expose the idempotency state and payment checkout data
 * required to complete or resume creation processing.
 */
public interface CreationTransactionResult {

    /**
     * Returns the identifier of the persisted idempotency request.
     *
     * @return idempotency request identifier
     */
    Long idempotencyRequestId();

    /**
     * Returns the current resolution of the idempotent operation.
     *
     * @return current idempotency resolution
     */
    IdempotencyResolution resolution();

    /**
     * Returns immutable payment data required to create or resume checkout.
     *
     * @return checkout request, or {@code null} while the request is still in progress
     */
    PaymentCheckoutRequest checkoutRequest();

    /**
     * Returns the previously created checkout URL when available.
     *
     * @return checkout URL, or {@code null} when checkout is not required or not completed yet
     */
    String checkoutUrl();

    /**
     * Indicates whether another request with the same idempotency key is still being processed.
     *
     * @return {@code true} when the result is in progress
     */
    default boolean isInProgress() {
        return resolution() == IdempotencyResolution.IN_PROGRESS;
    }

    /**
     * Indicates whether the creation flow has already been completed.
     *
     * @return {@code true} when the result is completed
     */
    default boolean isCompleted() {
        return resolution() == IdempotencyResolution.COMPLETED;
    }
}
