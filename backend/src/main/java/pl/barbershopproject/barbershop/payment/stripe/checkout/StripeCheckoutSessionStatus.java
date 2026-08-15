package pl.barbershopproject.barbershop.payment.stripe.checkout;

import java.util.Locale;
import java.util.Objects;

/**
 * Represents the lifecycle status of a Stripe Checkout Session.
 *
 * <p>The values correspond to statuses returned by Stripe:
 * {@code open}, {@code complete}, and {@code expired}.</p>
 */
public enum StripeCheckoutSessionStatus {

    /**
     * The Checkout Session is active and can still be used by the customer.
     */
    OPEN,

    /**
     * The Checkout Session has been completed.
     */
    COMPLETE,

    /**
     * The Checkout Session expired before it was completed.
     */
    EXPIRED;

    /**
     * Converts a Stripe Checkout Session status value to the application enum.
     *
     * @param value status returned by Stripe
     * @return corresponding Checkout Session status
     * @throws NullPointerException when the value is {@code null}
     * @throws IllegalStateException when Stripe returns an unsupported status
     */
    public static StripeCheckoutSessionStatus from(String value) {
        String requiredValue = Objects.requireNonNull(value,
                "Stripe Checkout status nie może być null");

        try {
            return valueOf(
                    requiredValue.toUpperCase(Locale.ROOT)
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException(
                    "Nieznany status Stripe Checkout: " + value,
                    exception);
        }
    }
}