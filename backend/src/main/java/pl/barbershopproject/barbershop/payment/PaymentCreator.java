package pl.barbershopproject.barbershop.payment;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;

/**
 * Defines operations responsible for creating payments for registered-user
 * and guest orders.
 *
 * <p>The interface provides an abstraction over payment persistence and
 * external checkout session creation. It prevents order processing services
 * from depending directly on payment infrastructure.</p>
 */
public interface PaymentCreator {

    /**
     * Creates a payment associated with a registered-user order.
     *
     * <p>When the selected payment method requires online checkout, the result
     * contains the URL of the created checkout session.</p>
     *
     * @param order order for which the payment is created
     * @param offer offer used to determine the payment amount
     * @param paymentMethod selected payment method
     * @return result containing the created payment and an optional checkout URL
     * @throws NullPointerException if any argument is {@code null}
     */
    PaymentCreationResult createForOrder(
            Order order,
            Offer offer,
            PaymentMethod paymentMethod
    );

    /**
     * Creates a payment associated with a guest order.
     *
     * <p>When the selected payment method requires online checkout, the result
     * contains the URL of the created checkout session.</p>
     *
     * @param guestOrder guest order for which the payment is created
     * @param offer offer used to determine the payment amount
     * @param paymentMethod selected payment method
     * @return result containing the created payment and an optional checkout URL
     * @throws NullPointerException if any argument is {@code null}
     */
    PaymentCreationResult createForGuestOrder(
            GuestOrder guestOrder,
            Offer offer,
            PaymentMethod paymentMethod
    );
}