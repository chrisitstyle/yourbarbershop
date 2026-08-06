package pl.barbershopproject.barbershop.exception;

public class MissingPaymentException extends RuntimeException {

    public MissingPaymentException(
            String orderType,
            Long orderId
    ) {
        super(orderType + " o ID " + orderId + " nie ma powiązanej płatności"
        );
    }
}
