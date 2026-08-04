package pl.barbershopproject.barbershop.idempotency;

/**
 * Represents the current stage of an idempotent order creation request.
 *
 * <p>The status allows an interrupted request to be resumed without
 * creating another order, payment or Stripe Checkout Session.</p>
 */
enum IdempotencyStatus {

    /**
     * The request was registered, but the order transaction
     * has not been completed yet.
     */
    PROCESSING,

    /**
     * The order, appointment slot and payment were persisted,
     * but checkout processing may still need to be completed.
     */
    RESOURCE_CREATED,

    /**
     * The complete response was produced and can be returned
     * for every repeated request using the same key.
     */
    COMPLETED
}
