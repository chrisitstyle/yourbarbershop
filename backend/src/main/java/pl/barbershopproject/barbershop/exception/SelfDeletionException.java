package pl.barbershopproject.barbershop.exception;

public class SelfDeletionException extends RuntimeException {
    public SelfDeletionException(String message) {
        super(message);
    }
}
