package pl.barbershopproject.barbershop.exception;

public class PaymentLinkUnavailableException extends RuntimeException {
    public PaymentLinkUnavailableException(String message) {
        super(message);
    }
}
