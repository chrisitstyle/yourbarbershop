package pl.barbershopproject.barbershop.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;

import java.util.Objects;

/**
 * Applies payment rules when the offer assigned to an order is changed.
 */
@Service
@RequiredArgsConstructor
class PaymentOfferUpdateService implements PaymentOfferUpdater {

    private final OrderModificationPolicy orderModificationPolicy;

    @Override
    public void updateAfterOfferChange(
            Payment payment,
            Offer newOffer
    ) {
        Objects.requireNonNull(payment,
                "Płatność zamówienia nie może być null");

        Objects.requireNonNull(newOffer,
                "Nowa oferta nie może być null");

        Objects.requireNonNull(newOffer.getCost(),
                "Cena nowej oferty nie może być null");

        orderModificationPolicy.validateOfferChange(payment);

        payment.setAmount(newOffer.getCost());
    }
}