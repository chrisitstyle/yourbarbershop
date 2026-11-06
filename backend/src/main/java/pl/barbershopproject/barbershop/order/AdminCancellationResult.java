package pl.barbershopproject.barbershop.order;

import pl.barbershopproject.barbershop.payment.refund.RefundResult;
import pl.barbershopproject.barbershop.utils.OrderStatus;

/**
 * Represents the result of an administrative appointment cancellation.
 */
public record AdminCancellationResult(
        Long reservationId,
        ReservationType reservationType,
        OrderStatus orderStatus,
        RefundResult refundResult) {

    public enum ReservationType {
        REGISTERED,
        GUEST
    }
}
