
package pl.barbershopproject.barbershop.admin.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contains paginated refund history and dashboard summary statistics.
 */
@Schema(
        name = "AdminRefundHistoryResponse",
        description = "Paginated Stripe refund history with "
                + "global administrator dashboard statistics")
public record AdminRefundHistoryResponseDTO(

        @Schema(description = "Refund records for the requested page")
        List<AdminRefundHistoryItemDTO> content,

        @Schema(description = "Current zero-based page index", example = "0")
        int page,

        @Schema(description = "Maximum number of refunds on each page",
                example = "20")
        int size,

        @Schema(description = "Total number of refunds matching "
                        + "the current filters", example = "27")
        long totalElements,

        @Schema(description = "Total number of pages matching "
                        + "the current filters", example = "2")
        int totalPages,

        @Schema(description = "Global refund summary statistics. "
                        + "These values are not restricted by "
                        + "the current history filters")
        AdminRefundHistorySummaryDTO summary
) {

    /**
     * Creates a response from a paginated refund result.
     *
     * @param refundPage paginated refund entries
     * @param summary global refund processing statistics
     * @return refund history response
     */
    public static AdminRefundHistoryResponseDTO from(
            Page<AdminRefundHistoryItemDTO> refundPage,
            AdminRefundHistorySummaryDTO summary
    ) {
        return new AdminRefundHistoryResponseDTO(
                refundPage.getContent(),
                refundPage.getNumber(),
                refundPage.getSize(),
                refundPage.getTotalElements(),
                refundPage.getTotalPages(),
                summary);
    }
}
