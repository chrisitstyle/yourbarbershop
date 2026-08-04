package pl.barbershopproject.barbershop.idempotency;

/**
 * Signals a concurrent attempt to register the same Idempotency-Key.
 *
 * <p>The surrounding order creation workflow uses this exception to retry
 * the lookup after the competing database transaction has completed.</p>
 */
public class IdempotencyRequestCollisionException extends RuntimeException {

    public IdempotencyRequestCollisionException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
