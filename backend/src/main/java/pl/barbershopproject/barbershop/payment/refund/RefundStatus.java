package pl.barbershopproject.barbershop.payment.refund;

/**
 * Represents the possible states of a payment refund lifecycle.
 */
public enum RefundStatus {
    REQUESTED,
    SUBMITTING,
    PENDING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REQUIRES_ACTION,
    RECONCILIATION_REQUIRED
}
