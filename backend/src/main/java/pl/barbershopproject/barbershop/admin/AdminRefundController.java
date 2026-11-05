
package pl.barbershopproject.barbershop.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundHistoryResponseDTO;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundResponseDTO;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

/**
 * Exposes refund history and details to administrators.
 */
@RestController
@RequestMapping("/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final AdminRefundQueryService adminRefundQueryService;

    /**
     * Returns paginated refund history and dashboard statistics.
     *
     * @param refundStatus optional refund status filter
     * @param reservationId optional registered or guest reservation ID
     * @param page zero-based page number
     * @param size number of refunds per page
     * @return refund history with pagination and summary
     */
    @GetMapping
    public ResponseEntity<AdminRefundHistoryResponseDTO> getRefundHistory(
            @RequestParam(name = "status", required = false)
            RefundStatus refundStatus,

            @RequestParam(required = false)
            Long reservationId,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size)
    {
        AdminRefundHistoryResponseDTO refundHistory = adminRefundQueryService.getRefundHistory(
                        refundStatus,
                        reservationId,
                        page,
                        size);

        return ResponseEntity.ok(refundHistory);
    }

    /**
     * Returns the latest refund state persisted in the database.
     *
     * @param refundId refund identifier
     * @return refund details
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<AdminRefundResponseDTO> getRefundDetails(@PathVariable Long refundId) {
        AdminRefundResponseDTO refundDetails = adminRefundQueryService.getRefundDetails(refundId);

        return ResponseEntity.ok(refundDetails);
    }
}
