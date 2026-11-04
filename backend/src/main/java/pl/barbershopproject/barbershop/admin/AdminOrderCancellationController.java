
package pl.barbershopproject.barbershop.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.admin.dto.AdminCancellationResponseDTO;
import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.order.OrderCancellationService;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminOrderCancellationController {

    private final OrderCancellationService orderCancellationService;

    /**
     * Cancels a registered customer's appointment as an administrator
     */
    @PostMapping("/orders/{orderId}/cancel")
    public ResponseEntity<AdminCancellationResponseDTO> cancelRegisteredOrder(@PathVariable Long orderId) {
        AdminCancellationResult cancellationResult = orderCancellationService.cancelRegisteredOrder(orderId);

        return createResponse(cancellationResult);
    }

    /**
     * Cancels a guest appointment as an administrator
     */
    @PostMapping("/guest-orders/{guestOrderId}/cancel")
    public ResponseEntity<AdminCancellationResponseDTO> cancelGuestOrder(
            @PathVariable Long guestOrderId
    ) {
        AdminCancellationResult cancellationResult =
                orderCancellationService.cancelGuestOrder(guestOrderId);

        return createResponse(cancellationResult);
    }

    private ResponseEntity<AdminCancellationResponseDTO> createResponse(
            AdminCancellationResult cancellationResult
    ) {
        HttpStatus responseStatus = resolveHttpStatus(cancellationResult.refundResult() == null
                        ? null
                        : cancellationResult.refundResult().refundStatus());

        return ResponseEntity
                .status(responseStatus)
                .body(AdminCancellationResponseDTO.from(cancellationResult));
    }

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
