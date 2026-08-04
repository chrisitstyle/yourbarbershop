package pl.barbershopproject.barbershop.idempotency;

import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

/**
 * Provides the application-facing operations required to register,
 * resume and complete idempotent order creation requests.
 */
public interface IdempotencyRequestManager {

    /**
     * Registers or resolves an authenticated order creation request.
     */
    IdempotencyRequestResult startOrderCreation(
            String idempotencyKey,
            String requestHash,
            Long userId
    );

    /**
     * Registers or resolves a guest order creation request.
     */
    IdempotencyRequestResult startGuestOrderCreation(
            String idempotencyKey,
            String requestHash
    );

    /**
     * Stores the order and payment data created inside the current transaction.
     */
    void markResourceCreated(
            Long requestId,
            Long resourceId,
            PaymentCheckoutRequest checkoutRequest
    );

    /**
     * Stores the final checkout result in a separate transaction.
     */
    void markCompleted(
            Long requestId,
            String checkoutUrl
    );
}
