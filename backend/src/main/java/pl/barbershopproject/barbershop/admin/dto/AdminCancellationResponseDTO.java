package pl.barbershopproject.barbershop.admin.dto;

import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.payment.refund.RefundResult;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.utils.OrderStatus;

/**
 * Exposes an administrative cancellation result through the REST API.
 */
public record AdminCancellationResponseDTO(
        Long reservationId,
        AdminCancellationResult.ReservationType reservationType,
        OrderStatus orderStatus,
        RefundDetails refund
) {

    public static AdminCancellationResponseDTO from(
            AdminCancellationResult cancellationResult
    ) {
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

    public record RefundDetails(
            Long refundId,
            String stripeRefundId,
            RefundStatus status) { }
}
