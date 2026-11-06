
package pl.barbershopproject.barbershop.payment.refund.event;

import pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;

import java.math.BigDecimal;

/**
 * Contains the information required to notify a customer
 * after a Stripe refund has successfully completed.
 * <p>
 * The event is published within the refund status transaction
 * and handled only after that transaction has been committed.
 *
 * @param refundId local refund identifier
 * @param reservationId associated reservation identifier
 * @param reservationType registered or guest reservation type
 * @param customerEmail recipient email address
 * @param customerFirstname recipient first name
 * @param amount successfully refunded amount
 * @param currency ISO currency code
 */
public record RefundSucceededEvent(
        Long refundId,
        Long reservationId,
        ReservationType reservationType,
        String customerEmail,
        String customerFirstname,
        BigDecimal amount,
        String currency
) {
}
