package pl.barbershopproject.barbershop.exception;

public class InvalidPasswordTokenException extends RuntimeException {
    public InvalidPasswordTokenException(String message) {
        super(message);
    }
}
