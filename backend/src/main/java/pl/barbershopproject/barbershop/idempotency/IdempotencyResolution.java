package pl.barbershopproject.barbershop.idempotency;

/**
 * Describes how an incoming request should be handled after looking up
 * its Idempotency-Key.
 */
public enum IdempotencyResolution {

    /**
     * The key has not been used before and the resource may be created.
     */
    NEW,

    /**
     * Another request using the same key is currently being processed.
     */
    IN_PROGRESS,

    /**
     * The order and payment already exist, but checkout processing
     * has not been completed yet.
     */
    RESOURCE_CREATED,

    /**
     * The complete stored result can be returned without repeating
     * any business operation.
     */
    COMPLETED
}
