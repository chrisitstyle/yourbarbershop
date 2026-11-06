package pl.barbershopproject.barbershop.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ProblemDetail;
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
 * <p>
 * These operations are read-only and never initiate
 * new Stripe refund requests.
 */
@RestController
@RequestMapping("/admin/refunds")
@RequiredArgsConstructor
@Tag(
        name = "Admin Refunds",
        description = "Administrative access to refund history, "
                + "processing statistics, and persisted Stripe refund details")
@SecurityRequirement(name = "bearerAuth")
public class AdminRefundController {

    private final AdminRefundQueryService adminRefundQueryService;

    /**
     * Returns paginated refund history with optional filters
     * and global summary statistics.
     *
     * @param refundStatus optional refund status
     * @param reservationId optional registered or guest reservation ID
     * @param page zero-based page index
     * @param size number of refunds per page
     * @return paginated refund history and statistics
     */
    @Operation(
            summary = "Get paginated refund history",
            description = """
                    Retrieves persisted Stripe refund records for
                    administrators.

                    Results can be filtered by refund status and
                    reservation identifier.

                    Reservation IDs are matched against both
                    registered customer reservations and guest
                    reservations. These reservation types use
                    separate identifier sequences.

                    Refund records are sorted by request date
                    and refund ID, newest first.

                    The response includes global summary statistics
                    for completed refunds, refunds in progress,
                    and refunds requiring administrative attention.

                    Summary statistics are not affected by the
                    selected table filters.

                    This endpoint is read-only and does not
                    initiate or retry Stripe refunds.
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "Refund history retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminRefundHistoryResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid pagination parameters, "
                    + "reservation ID, or refund status",
            content = @Content)
    @ApiResponse(
            responseCode = "401",
            description = "Authentication is required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Access restricted to administrators",
            content = @Content)
    @GetMapping
    public ResponseEntity<AdminRefundHistoryResponseDTO> getRefundHistory(
            @Parameter(
                    description = "Optional exact refund status. "
                            + "Use one of the RefundStatus enum values.",
                    example = "FAILED")
            @RequestParam(name = "status", required = false) RefundStatus refundStatus,

            @Parameter(
                    description = "Optional reservation ID. "
                            + "Matches registered and guest reservations.",
                    example = "18")
            @RequestParam(required = false) Long reservationId,

            @Parameter(description = "Zero-based page index", example = "0")
            @RequestParam(defaultValue = "0") int page,

            @Parameter(
                    description = "Number of refunds per page, "
                            + "between 1 and 100",
                    example = "20")
            @RequestParam(defaultValue = "20") int size) {
        AdminRefundHistoryResponseDTO refundHistory =
                adminRefundQueryService.getRefundHistory(refundStatus, reservationId, page, size);

        return ResponseEntity.ok(refundHistory);
    }

    /**
     * Returns the latest persisted details of a single refund.
     *
     * @param refundId local refund identifier
     * @return persisted refund details
     */
    @Operation(
            summary = "Get refund details",
            description = """
                    Retrieves the current persisted state of a
                    specific Stripe refund.

                    The response includes the associated payment,
                    Stripe refund identifier, amount, currency,
                    processing timestamps, and optional failure reason.

                    The endpoint reads the latest refund state from
                    the application database.

                    It does not initiate a refund or force a direct
                    Stripe status synchronization.
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "Refund details retrieved successfully",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminRefundResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid refund identifier",
            content = @Content)
    @ApiResponse(
            responseCode = "401",
            description = "Authentication is required",
            content = @Content)
    @ApiResponse(
            responseCode = "403",
            description = "Access restricted to administrators",
            content = @Content)
    @ApiResponse(
            responseCode = "404",
            description = "Refund not found",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @GetMapping("/{refundId}")
    public ResponseEntity<AdminRefundResponseDTO> getRefundDetails(
            @Parameter(description = "Local refund identifier", required = true, example = "9")
            @PathVariable Long refundId) {
        AdminRefundResponseDTO refundDetails = adminRefundQueryService.getRefundDetails(refundId);

        return ResponseEntity.ok(refundDetails);
    }
}
