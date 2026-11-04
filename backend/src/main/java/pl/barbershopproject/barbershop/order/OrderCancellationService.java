
package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.guestorder.GuestOrderService;
import pl.barbershopproject.barbershop.payment.refund.RefundResult;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.payment.refund.RefundTransactionService;
import pl.barbershopproject.barbershop.payment.refund.StripeRefundService;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import static pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;

/**
 * Coordinates administrative appointment cancellation and full Stripe refunds.
 * Database changes are committed before the external refund request.
 */
@Service
@RequiredArgsConstructor
public class OrderCancellationService {

    private final OrderService orderService;
    private final GuestOrderService guestOrderService;
    private final StripeRefundService stripeRefundService;
    private final RefundTransactionService refundTransactionService;

    public AdminCancellationResult cancelRegisteredOrder(Long orderId) {
        Long refundId = orderService.prepareAdminCancellation(orderId);

        RefundResult refundResult = submitRefundIfRequired(refundId);

        return new AdminCancellationResult(
                orderId,
                ReservationType.REGISTERED,
                OrderStatus.ANULOWANE,
                refundResult);
    }

    public AdminCancellationResult cancelGuestOrder(Long guestOrderId) {
        Long refundId = guestOrderService.prepareAdminCancellation(guestOrderId);

        RefundResult refundResult = submitRefundIfRequired(refundId);

        return new AdminCancellationResult(
                guestOrderId,
                ReservationType.GUEST,
                OrderStatus.ANULOWANE,
                refundResult);
    }


    private RefundResult submitRefundIfRequired(Long refundId) {
        if (refundId == null) {
            return null;
        }

        try {
            return stripeRefundService.submitReservedRefund(refundId);

        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode().value() != 502) {
                throw exception;
            }

            RefundTransactionService.RefundLookupTarget persistedRefund = refundTransactionService
                    .loadRefundForReconciliation(refundId);

            RefundStatus persistedStatus = persistedRefund.refundStatus();

            if (persistedStatus != RefundStatus.RECONCILIATION_REQUIRED
                    && persistedStatus != RefundStatus.PENDING
                    && persistedStatus != RefundStatus.SUCCEEDED
                    && persistedStatus != RefundStatus.FAILED
                    && persistedStatus != RefundStatus.CANCELED) {
                throw exception;
            }

            return new RefundResult(
                    persistedRefund.refundId(),
                    persistedRefund.stripeRefundId(),
                    persistedStatus);
        }
    }

}
