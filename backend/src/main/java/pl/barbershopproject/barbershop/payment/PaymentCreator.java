package pl.barbershopproject.barbershop.payment;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.Order;

/**
 * Creates payments associated with registered-user and guest orders.
 *
 * <p>The payment must be created inside the transaction responsible for
 * persisting the corresponding order.</p>
 */
public interface PaymentCreator {

    /**
     * Creates a payment associated with a registered-user order.
     *
     * @param order         persisted order containing a booked-offer snapshot
     * @param paymentMethod selected payment method
     * @return persisted payment and immutable checkout data
     */
    PaymentCreationResult createForOrder(
            Order order,
            PaymentMethod paymentMethod
    );

    /**
     * Creates a payment associated with a guest order.
     *
     * @param guestOrder    persisted guest order containing a booked-offer snapshot
     * @param paymentMethod selected payment method
     * @return persisted payment and immutable checkout data
     */
    PaymentCreationResult createForGuestOrder(
            GuestOrder guestOrder,
            PaymentMethod paymentMethod
    );
}