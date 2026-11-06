package pl.barbershopproject.barbershop.payment.refund;

/**
 * Represents the persisted result of a payment refund request.
 */
public record RefundResult(
        Long refundId,
        String stripeRefundId,
        RefundStatus refundStatus) { }
