package pl.barbershopproject.barbershop.exception;

/**
 * Thrown when an offer assigned to an existing order cannot be changed
 * because of the current payment state.
 */
public class OrderOfferChangeNotAllowedException extends RuntimeException {

    public OrderOfferChangeNotAllowedException(String message) {
        super(message);
    }
}
