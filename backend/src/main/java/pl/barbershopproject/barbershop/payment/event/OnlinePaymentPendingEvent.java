package pl.barbershopproject.barbershop.payment.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Event published when an online payment is awaiting customer completion.
 *
 * <p>The event contains all customer and reservation data required
 * to send a payment reminder without accessing JPA entities.</p>
 *
 * @param paymentId identifier of the payment
 * @param email customer email address
 * @param firstname customer first name
 * @param visitDate scheduled appointment date
 * @param offerName historical name of the booked offer
 * @param offerCost historical price of the booked offer
 */
public record OnlinePaymentPendingEvent(
        Long paymentId,
        String email,
        String firstname,
        LocalDateTime visitDate,
        String offerName,
        BigDecimal offerCost
) {
}
