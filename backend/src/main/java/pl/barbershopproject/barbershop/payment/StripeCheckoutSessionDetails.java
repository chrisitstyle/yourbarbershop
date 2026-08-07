package pl.barbershopproject.barbershop.payment;

import java.time.Instant;

/**
 * Contains details of an existing Stripe Checkout Session.
 *
 * <p>The data is retrieved from Stripe and is used to determine
 * whether a customer can continue an existing online payment.</p>
 *
 * @param sessionId identifier of the Stripe Checkout Session
 * @param status current lifecycle status of the Checkout Session
 * @param paymentStatus Stripe payment status associated with the session
 * @param checkoutUrl URL used to continue the checkout, when available
 * @param expiresAt expiration time of the Checkout Session
 */
public record StripeCheckoutSessionDetails(
        String sessionId,
        StripeCheckoutSessionStatus status,
        String paymentStatus,
        String checkoutUrl,
        Instant expiresAt
) {

    /**
     * Checks whether the Checkout Session is still open
     * and can potentially be used to continue payment.
     *
     * @return {@code true} when the session status is {@code OPEN}
     */
    public boolean isOpen() {
        return status == StripeCheckoutSessionStatus.OPEN;
    }
}
