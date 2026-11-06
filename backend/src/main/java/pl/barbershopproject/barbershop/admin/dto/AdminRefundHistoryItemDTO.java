
package pl.barbershopproject.barbershop.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a single refund entry in the administrator's refund history.
 */
@Schema(
        name = "AdminRefundHistoryItem",
        description = "A refund history record with its associated "
                + "reservation, payment, amount, and processing status")
public record AdminRefundHistoryItemDTO(

        @Schema(description = "Local refund identifier", example = "9")
        Long refundId,

        @Schema(description = "Associated payment identifier", example = "47")
        Long paymentId,

        @Schema(description = "Identifier of the registered or guest reservation", example = "18")
        Long reservationId,

        @Schema(description = "Reservation type. Registered and guest "
                        + "reservations use separate ID sequences",
                example = "REGISTERED")
        ReservationType reservationType,

        @Schema(description = "Refund amount in major currency units", example = "65.00")
        BigDecimal amount,

        @Schema(description = "ISO 4217 currency code", example = "PLN")
        String currency,

        @Schema(description = "Current refund lifecycle status", example = "SUCCEEDED")
        RefundStatus status,

        @Schema(description = "Timestamp when the refund was requested", example = "2026-11-05T14:30:00Z")
        Instant requestedAt,

        @Schema(description = "Timestamp when a terminal refund outcome "
                        + "was recorded. May be null",
                example = "2026-11-05T14:31:00Z",
                nullable = true)
        Instant completedAt
) {

    /**
     * Maps a persisted refund to an administrative history entry.
     *
     * @param refund persisted payment refund
     * @return refund entry with its associated reservation
     */
    public static AdminRefundHistoryItemDTO from(PaymentRefund refund) {
        Payment payment = refund.getPayment();

        Order registeredOrder = payment.getOrder();
        GuestOrder guestOrder = payment.getGuestOrder();

        if ((registeredOrder == null) == (guestOrder == null)) {
            throw new IllegalStateException("Refund payment must belong to exactly one reservation");
        }

        ReservationType reservationType = registeredOrder != null
                ? ReservationType.REGISTERED
                : ReservationType.GUEST;

        Long reservationId = registeredOrder != null
                ? registeredOrder.getIdOrder()
                : guestOrder.getIdGuestOrder();

        return new AdminRefundHistoryItemDTO(
                refund.getIdPaymentRefund(),
                payment.getIdPayment(),
                reservationId,
                reservationType,
                refund.getAmount(),
                refund.getCurrency(),
                refund.getRefundStatus(),
                refund.getRequestedAt(),
                refund.getCompletedAt());
    }
}
