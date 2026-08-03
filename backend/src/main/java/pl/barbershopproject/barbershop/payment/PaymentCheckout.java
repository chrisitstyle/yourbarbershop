package pl.barbershopproject.barbershop.payment;

/**
 * Creates an external checkout session for payments that require
 * an online card payment.
 */
public interface PaymentCheckout {

    /**
     * Creates a Stripe Checkout session when the payment method requires it.
     *
     * @param request immutable checkout data prepared during payment creation
     * @return Stripe Checkout URL or {@code null} for offline payment methods
     */
    String createCheckoutIfRequired(PaymentCheckoutRequest request);
}
