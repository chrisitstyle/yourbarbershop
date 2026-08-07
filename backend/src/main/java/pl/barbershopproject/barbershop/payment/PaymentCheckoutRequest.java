package pl.barbershopproject.barbershop.payment;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Contains immutable data required to create a Stripe Checkout session.
 *
 * <p>The object is prepared while the payment and order are still inside
 * the database transaction. It can then be safely used after the transaction
 * has been committed, without accessing detached JPA entities.</p>
 *
 * @param paymentId identifier of the persisted payment
 * @param paymentMethod selected payment method
 * @param paymentStatus current payment status
 * @param amount amount that should be charged
 * @param currency payment currency
 * @param productName historical name of the booked offer
 */
public record PaymentCheckoutRequest(
        Long paymentId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String stripeCheckoutIdempotencyKey,
        BigDecimal amount,
        String currency,
        String productName
) {

    public PaymentCheckoutRequest {
        Objects.requireNonNull(paymentId,"Payment ID nie może być null");

        Objects.requireNonNull(paymentMethod,"PaymentMethod nie może być null");
        Objects.requireNonNull(paymentStatus, "PaymentStatus nie może być null");

        if (paymentMethod == PaymentMethod.KARTA_ONLINE
                && (stripeCheckoutIdempotencyKey == null
                || stripeCheckoutIdempotencyKey.isBlank())) {
            throw new IllegalArgumentException(
                    "Stripe Checkout idempotency key nie może być pusty dla płatności online");
        }

        Objects.requireNonNull(amount, "Kwota płatności nie może być null");

        Objects.requireNonNull(currency, "Waluta nie może być null");

        Objects.requireNonNull(productName, "Nazwa usługi nie może być null");

        if (currency.isBlank()) {
            throw new IllegalArgumentException("Waluta nie może być pusta");
        }

        if (productName.isBlank()) {
            throw new IllegalArgumentException("Nazwa usługi nie może być pusta");
        }
    }

    public static PaymentCheckoutRequest from(Payment payment, String productName) {
        Objects.requireNonNull(payment, "Payment nie może być null");

        return new PaymentCheckoutRequest(
                payment.getIdPayment(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus(),
                payment.getStripeCheckoutIdempotencyKey(),
                payment.getAmount(),
                payment.getCurrency(),
                productName
        );
    }

    public boolean requiresOnlineCheckout() {
        return paymentMethod == PaymentMethod.KARTA_ONLINE;
    }
}
