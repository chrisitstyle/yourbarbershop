package pl.barbershopproject.barbershop.payment.checkout;

/**
 * Persists the Stripe Checkout session identifier assigned to a payment.
 */
public interface PaymentCheckoutSessionUpdater {

    void assignSession(Long paymentId, String sessionId);
}
