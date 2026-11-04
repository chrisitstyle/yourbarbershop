package pl.barbershopproject.barbershop.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundResponseDTO;

/**
 * Exposes refund status information to administrators.
 */
@RestController
@RequestMapping("/admin/refunds")
@RequiredArgsConstructor
public class AdminRefundController {

    private final AdminRefundQueryService adminRefundQueryService;

    /**
     * Returns the latest refund state persisted in the database.
     */
    @GetMapping("/{refundId}")
    public ResponseEntity<AdminRefundResponseDTO> getRefundDetails(
            @PathVariable Long refundId
    ) {
        AdminRefundResponseDTO refundDetails = adminRefundQueryService.getRefundDetails(refundId);

        return ResponseEntity.ok(refundDetails);
    }
}
