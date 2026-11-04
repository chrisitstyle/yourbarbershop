
package pl.barbershopproject.barbershop.admin.dto;

import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Exposes the persisted refund status to administrators.
 */
public record AdminRefundResponseDTO(
        Long refundId,
        Long paymentId,
        String stripeRefundId,
        RefundStatus status,
        BigDecimal amount,
        String currency,
        Instant requestedAt,
        Instant completedAt,
        String failureReason
) {

    public static AdminRefundResponseDTO from(PaymentRefund refund) {
        return new AdminRefundResponseDTO(
                refund.getIdPaymentRefund(),
                refund.getPayment().getIdPayment(),
                refund.getStripeRefundId(),
                refund.getRefundStatus(),
                refund.getAmount(),
                refund.getCurrency(),
                refund.getRequestedAt(),
                refund.getCompletedAt(),
                refund.getFailureReason());
    }
}
