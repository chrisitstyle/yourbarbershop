package pl.barbershopproject.barbershop.payment.stripe.checkout;

public record StripeCheckoutSessionResponse(
        String sessionId,
        String checkoutUrl
) {
}
