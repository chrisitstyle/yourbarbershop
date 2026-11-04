package pl.barbershopproject.barbershop.exception;

public class OrderDeletionNotAllowedException extends RuntimeException {

    public OrderDeletionNotAllowedException() {
        super("Można usunąć tylko nieopłacone rezerwacje z płatnością na miejscu");
    }
}
