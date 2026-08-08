package pl.barbershopproject.barbershop.exception;

public class InvalidPaymentLinkTokenException extends RuntimeException {

    public InvalidPaymentLinkTokenException(String message) {
        super(message);
    }
}
