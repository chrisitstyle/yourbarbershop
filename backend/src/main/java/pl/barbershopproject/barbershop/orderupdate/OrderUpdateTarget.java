package pl.barbershopproject.barbershop.orderupdate;

import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;

/**
 * Defines the common order data required by the shared update workflow.
 * Implementations provide access to the selected offer, booked offer snapshot,
 * visit date, and current order status.
 */
public interface OrderUpdateTarget {

    /**
     * Returns the currently assigned offer.
     *
     * @return current offer, or {@code null} when no offer is assigned
     */
    Offer getOffer();

    /**
     * Assigns the selected offer.
     *
     * @param offer offer to assign
     */
    void setOffer(Offer offer);

    /**
     * Assigns the immutable snapshot of the selected offer.
     *
     * @param bookedOffer booked offer snapshot to assign
     */
    void setBookedOffer(BookedOffer bookedOffer);

    /**
     * Returns the currently reserved visit date.
     *
     * @return current visit date
     */
    LocalDateTime getVisitDate();

    /**
     * Returns the current order status.
     *
     * @return current order status
     */
    OrderStatus getOrderStatus();
}
