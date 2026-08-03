package pl.barbershopproject.barbershop.payment;

import java.util.Objects;

/**
 * Represents a payment persisted as part of an order transaction together
 * with immutable data required for optional checkout creation.
 *
 * @param payment persisted payment
 * @param checkoutRequest immutable checkout data prepared inside the transaction
 */
public record PaymentCreationResult(
        Payment payment,
        PaymentCheckoutRequest checkoutRequest
) {

    public PaymentCreationResult {
       Objects.requireNonNull(payment, "Payment nie może być null");

        Objects.requireNonNull(checkoutRequest,"PaymentCheckoutRequest nie może być null"
        );
    }
}