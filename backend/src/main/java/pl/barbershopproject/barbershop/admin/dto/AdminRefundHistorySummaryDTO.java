
package pl.barbershopproject.barbershop.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Summarizes refund processing outcomes for the admin dashboard.
 * <p>
 * Statistics cover all persisted refunds, independently of
 * the currently selected refund history filters.
 *
 * @param completed number of successfully completed refunds
 * @param inProgress number of refunds still being processed
 * @param requiresAttention number of refunds requiring review
 */
@Schema(name = "AdminRefundHistorySummary",
        description = "Global refund processing statistics "
                + "for the administrator dashboard")
public record AdminRefundHistorySummaryDTO(

        @Schema(description = "Number of refunds with SUCCEEDED status", example = "24")
        long completed,

        @Schema(description = "Number of refunds in REQUESTED, "
                        + "SUBMITTING, or PENDING status", example = "2")
        long inProgress,

        @Schema(description = "Number of refunds in FAILED, CANCELED, "
                        + "REQUIRES_ACTION, or RECONCILIATION_REQUIRED status", example = "1")
        long requiresAttention
) {
}
