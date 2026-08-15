package pl.barbershopproject.barbershop.payment.link;

import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;

/**
 * Contains immutable payment and reservation data required
 * to resolve a customer payment link outside a database transaction.
 *
 * @param checkoutRequest immutable data required to create Stripe Checkout
 * @param stripeCheckoutSessionId existing Stripe Checkout Session identifier
 * @param orderStatus current reservation status
 * @param visitDate scheduled appointment date
 */
public record PaymentLinkPaymentData(
        PaymentCheckoutRequest checkoutRequest,
        String stripeCheckoutSessionId,
        OrderStatus orderStatus,
        LocalDateTime visitDate
) {
}
