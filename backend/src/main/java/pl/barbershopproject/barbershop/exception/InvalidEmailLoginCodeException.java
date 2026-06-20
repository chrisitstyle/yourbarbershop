package pl.barbershopproject.barbershop.exception;

public class InvalidEmailLoginCodeException extends RuntimeException {
    public InvalidEmailLoginCodeException(String message) {
        super(message);
    }
}
