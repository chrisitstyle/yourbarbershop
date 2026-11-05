package pl.barbershopproject.barbershop.admin.dto;

/**
 * Summarizes refund processing outcomes for the admin dashboard.
 *
 * @param completed number of successfully completed refunds
 * @param inProgress number of refunds still being processed
 * @param requiresAttention number of refunds requiring administrative review
 */
public record AdminRefundHistorySummaryDTO(
        long completed,
        long inProgress,
        long requiresAttention) { }
