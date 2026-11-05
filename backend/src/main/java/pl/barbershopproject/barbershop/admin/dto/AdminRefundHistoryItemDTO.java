package pl.barbershopproject.barbershop.admin.dto;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Represents a single refund entry in the administrator's refund history.
 */
public record AdminRefundHistoryItemDTO(
        Long refundId,
        Long paymentId,
        Long reservationId,
        AdminCancellationResult.ReservationType reservationType,
        BigDecimal amount,
        String currency,
        RefundStatus status,
        Instant requestedAt,
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

        AdminCancellationResult.ReservationType reservationType = registeredOrder != null
                ? AdminCancellationResult.ReservationType.REGISTERED
                : AdminCancellationResult.ReservationType.GUEST;

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
