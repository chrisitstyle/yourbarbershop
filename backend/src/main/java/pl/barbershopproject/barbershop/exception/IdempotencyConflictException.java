package pl.barbershopproject.barbershop.exception;

/**
 * Indicates that an Idempotency-Key cannot be accepted because it was
 * already used for different request data or by a different owner.
 */
public class IdempotencyConflictException extends RuntimeException {

    public IdempotencyConflictException(String message) {
        super(message);
    }
}
