package pl.barbershopproject.barbershop.admin.dto;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Contains paginated refund history and dashboard summary statistics.
 */
public record AdminRefundHistoryResponseDTO(
        List<AdminRefundHistoryItemDTO> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        AdminRefundHistorySummaryDTO summary) {

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
