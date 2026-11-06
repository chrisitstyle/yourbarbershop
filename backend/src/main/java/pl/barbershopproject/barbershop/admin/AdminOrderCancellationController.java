package pl.barbershopproject.barbershop.admin;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.admin.dto.AdminCancellationResponseDTO;
import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.order.OrderCancellationService;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

/**
 * Exposes administrative reservation cancellation operations.
 *
 * Eligible paid online reservations trigger a full Stripe refund.
 * Reservations with on-site payment methods are cancelled without
 * creating a Stripe refund.
 */
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@Tag(
        name = "Admin Order Cancellation",
        description = "Administrative cancellation of registered and guest "
                + "reservations, including full Stripe refunds when required")
@SecurityRequirement(name = "bearerAuth")
public class AdminOrderCancellationController {

    private final OrderCancellationService orderCancellationService;

    /**
     * Cancels a registered customer's reservation as an administrator.
     *
     * @param orderId registered reservation identifier
     * @return cancellation result and optional refund information
     */
    @Operation(
            summary = "Cancel a registered customer reservation",
            description = """
                    Cancels an eligible registered customer reservation.

                    For paid online reservations, the backend initiates
                    a full Stripe refund and returns its current status.

                    Reservations paid on site are cancelled without
                    creating a refund.

                    The cancellation endpoint is restricted to
                    administrators. Standard order update endpoints
                    cannot be used to cancel reservations.

                    A successfully processed refund triggers a
                    best-effort email confirmation after the
                    refund transaction is committed.
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "Reservation cancelled. No refund was "
                    + "required, or the refund reached a terminal "
                    + "status.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminCancellationResponseDTO.class)))
    @ApiResponse(
            responseCode = "202",
            description = "Reservation cancelled. The refund is "
                    + "still being processed or requires "
                    + "reconciliation.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminCancellationResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid reservation identifier",
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
            description = "Reservation not found",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Reservation cannot be cancelled "
                    + "in its current state, or cancellation "
                    + "has already been performed",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "502",
            description = "Stripe refund submission could not "
                    + "be completed or reconciled",
            content = @Content)
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<AdminCancellationResponseDTO> cancelRegisteredOrder(
            @Parameter(description = "Registered reservation identifier", required = true, example = "18")
            @PathVariable Long orderId) {
        AdminCancellationResult cancellationResult = orderCancellationService.cancelRegisteredOrder(orderId);

        return createResponse(cancellationResult);
    }

    /**
     * Cancels a guest reservation as an administrator.
     *
     * @param guestOrderId guest reservation identifier
     * @return cancellation result and optional refund information
     */
    @Operation(
            summary = "Cancel a guest reservation",
            description = """
                    Cancels an eligible guest reservation.

                    If the reservation was paid online, the backend
                    initiates a full Stripe refund and returns its
                    current processing status.

                    On-site payment methods do not require a refund.

                    The operation requires administrator privileges
                    and cannot be replaced by a standard guest order
                    update request.

                    After a successful refund, the guest receives
                    a best-effort confirmation email.
                    """)
    @ApiResponse(
            responseCode = "200",
            description = "Guest reservation cancelled. "
                    + "No refund was required, or the refund "
                    + "reached a terminal status.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminCancellationResponseDTO.class)))
    @ApiResponse(
            responseCode = "202",
            description = "Guest reservation cancelled. "
                    + "Refund processing or reconciliation "
                    + "is still in progress.",
            content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = AdminCancellationResponseDTO.class)))
    @ApiResponse(
            responseCode = "400",
            description = "Invalid guest reservation identifier",
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
            description = "Guest reservation not found",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "409",
            description = "Guest reservation cannot be cancelled "
                    + "in its current state, or cancellation "
                    + "has already been performed",
            content = @Content(
                    mediaType = "application/problem+json",
                    schema = @Schema(implementation = ProblemDetail.class)))
    @ApiResponse(
            responseCode = "502",
            description = "Stripe refund submission could not "
                    + "be completed or reconciled",
            content = @Content)
    @PostMapping("/guest-orders/{guestOrderId}/cancel")
    public ResponseEntity<AdminCancellationResponseDTO> cancelGuestOrder(
            @Parameter(description = "Guest reservation identifier", required = true, example = "42")
            @PathVariable Long guestOrderId) {
        AdminCancellationResult cancellationResult = orderCancellationService.cancelGuestOrder(guestOrderId);

        return createResponse(cancellationResult);
    }

    /**
     * Converts the cancellation result into an HTTP response.
     *
     * @param cancellationResult completed cancellation operation
     * @return HTTP response with cancellation and refund information
     */
    private ResponseEntity<AdminCancellationResponseDTO> createResponse(AdminCancellationResult cancellationResult) {
        HttpStatus responseStatus = resolveHttpStatus(
                cancellationResult.refundResult() == null
                        ? null
                        : cancellationResult.refundResult().refundStatus());

        return ResponseEntity.status(responseStatus)
                .body(AdminCancellationResponseDTO.from(cancellationResult));
    }

    /**
     * Resolves the response status based on the refund lifecycle.
     *
     * @param refundStatus current refund status, or null if not required
     * @return OK for absent or terminal refunds, ACCEPTED otherwise
     */
    private HttpStatus resolveHttpStatus(RefundStatus refundStatus) {
        if (refundStatus == null
                || refundStatus == RefundStatus.SUCCEEDED
                || refundStatus == RefundStatus.FAILED
                || refundStatus == RefundStatus.CANCELED) {
            return HttpStatus.OK;
        }

        return HttpStatus.ACCEPTED;
    }
}
