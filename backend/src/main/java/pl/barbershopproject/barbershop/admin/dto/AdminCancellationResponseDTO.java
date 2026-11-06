
package pl.barbershopproject.barbershop.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.payment.refund.RefundResult;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.utils.OrderStatus;

/**
 * Exposes an administrative reservation cancellation result.
 * <p>
 * The refund field is null when no Stripe refund was required.
 */
@Schema(name = "AdminCancellationResponse",
        description = "Result of an administrative reservation "
                + "cancellation, including optional Stripe refund details")
public record AdminCancellationResponseDTO(

        @Schema(description = "Identifier of the cancelled reservation", example = "18")
        Long reservationId,

        @Schema(description = "Type of the cancelled reservation", example = "REGISTERED")
        AdminCancellationResult.ReservationType reservationType,

        @Schema(description = "Resulting reservation status", example = "ANULOWANE")
        OrderStatus orderStatus,

        @Schema(description = "Stripe refund details. Null when the "
                        + "reservation did not require an online refund",
                nullable = true)
        RefundDetails refund
) {

    /**
     * Maps an internal cancellation result to its API representation.
     *
     * @param cancellationResult internal cancellation result
     * @return API cancellation response
     */
    public static AdminCancellationResponseDTO from(AdminCancellationResult cancellationResult) {
        RefundResult refundResult = cancellationResult.refundResult();

        RefundDetails refundDetails = refundResult == null
                ? null
                : new RefundDetails(
                refundResult.refundId(),
                refundResult.stripeRefundId(),
                refundResult.refundStatus());

        return new AdminCancellationResponseDTO(
                cancellationResult.reservationId(),
                cancellationResult.reservationType(),
                cancellationResult.orderStatus(),
                refundDetails);
    }

    /**
     * Contains the current result of a Stripe refund submission.
     *
     * @param refundId local refund identifier
     * @param stripeRefundId Stripe refund identifier
     * @param status current refund lifecycle status
     */
    @Schema(name = "AdminCancellationRefundDetails",
            description = "Refund information returned after "
                    + "an administrative cancellation")
    public record RefundDetails(

    @Schema(description = "Local refund identifier", example = "9")
    Long refundId,

    @Schema(description = "Stripe refund identifier. "
                    + "May be null before Stripe assigns one",
            example = "re_123456789",
            nullable = true)
    String stripeRefundId,

    @Schema(description = "Current Stripe refund lifecycle status", example = "PENDING")
    RefundStatus status) { }
}
