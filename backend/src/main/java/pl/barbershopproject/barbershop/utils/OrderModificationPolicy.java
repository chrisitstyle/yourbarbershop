package pl.barbershopproject.barbershop.utils;

import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.exception.OrderModificationNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderOfferChangeNotAllowedException;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.util.Objects;

/**
 * Defines business rules governing order modifications.
 *
 * <p>The policy is shared by registered-user orders and guest orders
 * to keep lifecycle and payment rules consistent.</p>
 */
@Component
public class OrderModificationPolicy {

    private static final String PAID_ORDER_OFFER_MESSAGE = "Nie można zmienić oferty w opłaconym lub zwróconym zamówieniu";

    private static final String PENDING_ONLINE_OFFER_MESSAGE = "Nie można zmienić oferty, gdy płatność online oczekuje na zakończenie";

    private static final String COMPLETION_PAYMENT_MESSAGE = "Nie można zrealizować zamówienia przed rozliczeniem płatności";

    private static final String PAID_ORDER_CANCELLATION_MESSAGE = "Nie można anulować opłaconego zamówienia bez wykonania zwrotu";

    private static final String PENDING_ONLINE_CANCELLATION_MESSAGE = "Nie można anulować zamówienia podczas aktywnej płatności online";

    /**
     * Validates whether an existing order can be updated and whether
     * the requested status transition is allowed.
     */
    public void validateUpdate(OrderStatus currentStatus, OrderStatus targetStatus, Payment payment) {
        Objects.requireNonNull(
                currentStatus,
                "Aktualny status zamówienia nie może być null"
        );

        Objects.requireNonNull(
                targetStatus,
                "Docelowy status zamówienia nie może być null"
        );

        Objects.requireNonNull(
                payment,
                "Płatność zamówienia nie może być null"
        );

        validateModificationAllowed(currentStatus);

        if (currentStatus == targetStatus) {
            return;
        }

        switch (targetStatus) {
            case NOWE -> {
                // The only editable current status is NOWE,
                // so this transition does not require additional validation.
            }
            case ZREALIZOWANE -> validateCompletion(payment);
            case ANULOWANE -> validateCancellation(payment);
        }
    }

    /**
     * Validates whether the offer assigned to an order can be changed.
     */
    public void validateOfferChange(Payment payment) {
        Objects.requireNonNull(
                payment,
                "Płatność zamówienia nie może być null"
        );

        PaymentStatus paymentStatus = payment.getPaymentStatus();

        if (paymentStatus == PaymentStatus.OPLACONA
                || paymentStatus == PaymentStatus.ZWROCONA) {
            throw new OrderOfferChangeNotAllowedException(
                    PAID_ORDER_OFFER_MESSAGE
            );
        }

        if (payment.getPaymentMethod() == PaymentMethod.KARTA_ONLINE
                && paymentStatus == PaymentStatus.OCZEKUJE_NA_PLATNOSC) {
            throw new OrderOfferChangeNotAllowedException(
                    PENDING_ONLINE_OFFER_MESSAGE
            );
        }
    }

    private void validateModificationAllowed(
            OrderStatus currentStatus
    ) {
        if (currentStatus != OrderStatus.NOWE) {
            throw new OrderModificationNotAllowedException(
                    currentStatus
            );
        }
    }

    private void validateCompletion(Payment payment) {
        PaymentStatus paymentStatus = payment.getPaymentStatus();
        PaymentMethod paymentMethod = payment.getPaymentMethod();

        if (paymentStatus == PaymentStatus.OPLACONA) {
            return;
        }

        boolean paymentOnSiteDoesNotRequireOnlinePayment =
                paymentMethod != PaymentMethod.KARTA_ONLINE
                        && paymentStatus == PaymentStatus.NIE_WYMAGANA;

        if (!paymentOnSiteDoesNotRequireOnlinePayment) {
            throw new OrderStatusChangeNotAllowedException(COMPLETION_PAYMENT_MESSAGE);
        }
    }

    private void validateCancellation(Payment payment) {
        PaymentStatus paymentStatus = payment.getPaymentStatus();

        if (paymentStatus == PaymentStatus.OPLACONA) {
            throw new OrderStatusChangeNotAllowedException(PAID_ORDER_CANCELLATION_MESSAGE);
        }

        if (payment.getPaymentMethod() == PaymentMethod.KARTA_ONLINE
                && paymentStatus == PaymentStatus.OCZEKUJE_NA_PLATNOSC) {
            throw new OrderStatusChangeNotAllowedException(PENDING_ONLINE_CANCELLATION_MESSAGE);
        }
    }
}
