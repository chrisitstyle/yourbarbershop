package pl.barbershopproject.barbershop.exception;

public class InvalidCaptchaException extends RuntimeException {
    public InvalidCaptchaException(String message) {
        super(message);
    }
}