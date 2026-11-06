package pl.barbershopproject.barbershop.utils;

import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.exception.OrderDeletionNotAllowedException;
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

    private static final String ADMIN_CANCELLATION_ENDPOINT_REQUIRED_MESSAGE = "Anulowanie rezerwacji jest dostępne wyłącznie przez dedykowany endpoint administratora";

    /**
     * Validates whether an existing reservation can be updated.
     * <p>
     * Administrative cancellation must use the dedicated cancellation
     * endpoint to preserve payment and refund consistency.
     */
    public void validateUpdate(
            OrderStatus currentStatus,
            OrderStatus targetStatus,
            Payment payment
    ) {
        Objects.requireNonNull(
                currentStatus,
                "Aktualny status zamówienia nie może być null");

        Objects.requireNonNull(
                targetStatus,
                "Docelowy status zamówienia nie może być null");

        Objects.requireNonNull(
                payment,
                "Płatność zamówienia nie może być null");

        validateModificationAllowed(currentStatus);

        if (targetStatus == OrderStatus.ANULOWANE) {
            throw new OrderStatusChangeNotAllowedException(ADMIN_CANCELLATION_ENDPOINT_REQUIRED_MESSAGE);
        }

        if (currentStatus == targetStatus) {
            return;
        }

        switch (targetStatus) {
            case NOWE -> {
                // only active reservations can be edited
            }
            case ZREALIZOWANE -> validateCompletion(payment);
            case ANULOWANE -> throw new OrderStatusChangeNotAllowedException(ADMIN_CANCELLATION_ENDPOINT_REQUIRED_MESSAGE);
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

    public void validateDeletion(Payment payment) {
        Objects.requireNonNull(payment,
                "Płatność zamówienia nie może być null");

        boolean isOnSitePayment = payment.getPaymentMethod() == PaymentMethod.GOTOWKA
                || payment.getPaymentMethod() == PaymentMethod.KARTA_NA_MIEJSCU;

        boolean isPaymentNotRequired = payment.getPaymentStatus() == PaymentStatus.NIE_WYMAGANA;

        if (!isOnSitePayment || !isPaymentNotRequired) {
            throw new OrderDeletionNotAllowedException();
        }
    }

    /**
     * Validates administrative cancellation and determines
     * whether a full Stripe refund is required.
     */
    public boolean requiresFullRefundForAdminCancellation(
            OrderStatus currentOrderStatus,
            Payment payment
    ) {
        Objects.requireNonNull(payment, "Płatność zamówienia nie może być null");

        if (currentOrderStatus != OrderStatus.NOWE) {
            throw new OrderStatusChangeNotAllowedException("Można anulować tylko aktywną rezerwację");
        }

        PaymentMethod paymentMethod = payment.getPaymentMethod();
        PaymentStatus paymentStatus = payment.getPaymentStatus();

        if (paymentMethod == PaymentMethod.KARTA_ONLINE
                && paymentStatus == PaymentStatus.OPLACONA) {
            return true;
        }

        boolean isUnpaidOnSitePayment = (paymentMethod == PaymentMethod.GOTOWKA
                || paymentMethod == PaymentMethod.KARTA_NA_MIEJSCU)
                && paymentStatus == PaymentStatus.NIE_WYMAGANA;

        if (isUnpaidOnSitePayment) {
            return false;
        }

        throw new OrderStatusChangeNotAllowedException("Nie można anulować rezerwacji z obecnym statusem płatności");
    }
}
