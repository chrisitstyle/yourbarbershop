package pl.barbershopproject.barbershop.idempotency;

/**
 * Identifies the API operation protected by an idempotency key.
 *
 * <p>The operation is part of the unique key, so the same client-generated
 * key may be used independently for authenticated and guest order creation.</p>
 */
enum IdempotencyOperation {

    /**
     * Creation of an order by an authenticated user.
     */
    ORDER_CREATION,

    /**
     * Creation of an order by a guest user.
     */
    GUEST_ORDER_CREATION
}
