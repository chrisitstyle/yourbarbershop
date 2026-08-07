package pl.barbershopproject.barbershop.payment.dto;

/**
 * Response returned when a payment link is successfully resolved
 * to an active Stripe Checkout Session.
 *
 * @param checkoutUrl URL used to continue the online payment
 */
public record PaymentLinkCheckoutResponseDTO(
        String checkoutUrl) { }
