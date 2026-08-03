package pl.barbershopproject.barbershop.payment;

import pl.barbershopproject.barbershop.offer.Offer;

/**
 * Updates payment details after changing the offer assigned to an order.
 */
public interface PaymentOfferUpdater {

    /**
     * Validates whether the offer can be changed and updates the payment amount.
     *
     * @param payment payment associated with the order
     * @param newOffer newly selected offer
     */
    void updateAfterOfferChange(
            Payment payment,
            Offer newOffer
    );
}
