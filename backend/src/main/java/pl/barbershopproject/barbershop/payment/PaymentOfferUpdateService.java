package pl.barbershopproject.barbershop.payment;

import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.offer.Offer;

import java.util.Objects;

/**
 * Applies payment rules when the offer assigned to an order is changed.
 */
@Service
class PaymentOfferUpdateService implements PaymentOfferUpdater {

    private static final String PAID_ORDER_MESSAGE =
            "Nie można zmienić oferty w opłaconym zamówieniu";

    private static final String ONLINE_PAYMENT_MESSAGE =
            "Nie można zmienić oferty, gdy płatność online oczekuje na zakończenie";

    @Override
    public void updateAfterOfferChange(
            Payment payment,
            Offer newOffer
    ) {
        Objects.requireNonNull(payment,
                "Płatność zamówienia nie może być null"
        );

        Objects.requireNonNull(newOffer,
                "Nowa oferta nie może być null"
        );

        Objects.requireNonNull(newOffer.getCost(),
                "Cena nowej oferty nie może być null"
        );

        validatePaymentState(payment);

        payment.setAmount(newOffer.getCost());
    }

    private void validatePaymentState(Payment payment) {
        PaymentStatus paymentStatus = payment.getPaymentStatus();

        if (paymentStatus == PaymentStatus.OPLACONA
                || paymentStatus == PaymentStatus.ZWROCONA) {
            throw new OrderOfferChangeNotAllowedException(
                    PAID_ORDER_MESSAGE
            );
        }

        if (payment.getPaymentMethod() == PaymentMethod.KARTA_ONLINE
                && paymentStatus == PaymentStatus.OCZEKUJE_NA_PLATNOSC) {
            throw new OrderOfferChangeNotAllowedException(
                    ONLINE_PAYMENT_MESSAGE
            );
        }
    }
}
