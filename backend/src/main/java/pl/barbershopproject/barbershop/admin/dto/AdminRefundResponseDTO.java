
package pl.barbershopproject.barbershop.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Exposes persisted Stripe refund details to administrators.
 */
@Schema(name = "AdminRefundResponse",
        description = "Detailed information about a persisted Stripe refund")
public record AdminRefundResponseDTO(

        @Schema(description = "Local refund identifier", example = "9")
        Long refundId,

        @Schema(description = "Identifier of the associated payment", example = "47")
        Long paymentId,

        @Schema(description = "Stripe refund identifier. "
                        + "May be absent before submission",
                example = "re_123456789",
                nullable = true)
        String stripeRefundId,

        @Schema(description = "Current refund lifecycle status", example = "SUCCEEDED")
        RefundStatus status,

        @Schema(description = "Refund amount in major currency units",
                example = "65.00")
        BigDecimal amount,

        @Schema(description = "ISO 4217 currency code", example = "PLN")
        String currency,

        @Schema(description = "Timestamp when the refund was requested", example = "2026-11-05T14:30:00Z")
        Instant requestedAt,

        @Schema(
                description = "Timestamp when a terminal refund "
                        + "outcome was recorded. May be null",
                example = "2026-11-05T14:31:00Z",
                nullable = true)
        Instant completedAt,

        @Schema(description = "Failure reason returned by Stripe, "
                        + "when available",
                example = "expired_or_canceled_card",
                nullable = true)
        String failureReason
) {

    /**
     * Maps a persisted refund entity into its API representation.
     *
     * @param refund persisted refund
     * @return refund details response
     */
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
