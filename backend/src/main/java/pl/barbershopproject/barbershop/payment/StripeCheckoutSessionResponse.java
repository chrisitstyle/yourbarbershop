package pl.barbershopproject.barbershop.payment;

public record StripeCheckoutSessionResponse(
        String sessionId,
        String checkoutUrl
) {
}
