package pl.barbershopproject.barbershop.exception;

import pl.barbershopproject.barbershop.utils.OrderStatus;

/**
 * Thrown when an order cannot be modified because it has reached
 * a terminal lifecycle status.
 */
public class OrderModificationNotAllowedException extends RuntimeException {

    public OrderModificationNotAllowedException(OrderStatus status) {
        super("Nie można edytować zamówienia ze statusem: " + status);
    }
}
