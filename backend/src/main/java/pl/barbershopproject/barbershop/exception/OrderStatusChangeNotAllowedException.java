package pl.barbershopproject.barbershop.exception;

/**
 * Thrown when an order status transition is not allowed because
 * of the current payment state.
 */
public class OrderStatusChangeNotAllowedException extends RuntimeException {

    public OrderStatusChangeNotAllowedException(String message) {
        super(message);
    }
}
