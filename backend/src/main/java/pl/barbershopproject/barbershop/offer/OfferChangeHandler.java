package pl.barbershopproject.barbershop.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Handles offer changes for order-like entities and keeps the related payment
 * and booked offer snapshot in sync.
 */
@Component
@RequiredArgsConstructor
public class OfferChangeHandler {

    private final PaymentOfferUpdater paymentOfferUpdater;

    /**
     * Updates the target entity when the selected offer has changed.
     * The payment is updated first, followed by the current offer and its booked snapshot.
     *
     * @param currentOffer      currently assigned offer, or {@code null} when none is assigned
     * @param targetOffer       offer requested by the update operation
     * @param payment           payment associated with the order
     * @param offerSetter       callback used to assign the new offer
     * @param bookedOfferSetter callback used to assign the immutable booked offer snapshot
     */
    public void updateIfChanged(
            Offer currentOffer,
            Offer targetOffer,
            Payment payment,
            Consumer<Offer> offerSetter,
            Consumer<BookedOffer> bookedOfferSetter
    ) {
        if (!hasChanged(currentOffer, targetOffer)) {
            return;
        }

        paymentOfferUpdater.updateAfterOfferChange(
                payment,
                targetOffer
        );

        offerSetter.accept(targetOffer);
        bookedOfferSetter.accept(
                BookedOffer.from(targetOffer)
        );
    }

    private boolean hasChanged(
            Offer currentOffer,
            Offer targetOffer
    ) {
        return currentOffer == null || !Objects.equals(
                currentOffer.getIdOffer(),
                targetOffer.getIdOffer());
    }
}
