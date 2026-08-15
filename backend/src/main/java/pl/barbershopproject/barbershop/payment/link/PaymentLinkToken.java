package pl.barbershopproject.barbershop.payment.link;

import java.time.Instant;

/**
 * Contains data extracted from a verified payment link token.
 *
 * @param paymentId identifier of the payment
 * @param expiresAt expiration time of the payment link
 */
public record PaymentLinkToken(
        Long paymentId,
        Instant expiresAt
) {
}
