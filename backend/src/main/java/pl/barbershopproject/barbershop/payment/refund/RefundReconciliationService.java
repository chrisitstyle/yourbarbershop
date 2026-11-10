
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Reconciles unfinished payment refunds with their current Stripe state.
 * Never creates new refunds or retries refund creation requests.
 */
@Service
@RequiredArgsConstructor
public class RefundReconciliationService {

    private static final int MAX_REFUND_PAGES = 50;

    private static final Set<RefundStatus> RECONCILIABLE_STATUSES = EnumSet.of(
            RefundStatus.REQUESTED,
            RefundStatus.SUBMITTING,
            RefundStatus.PENDING,
            RefundStatus.REQUIRES_ACTION,
            RefundStatus.RECONCILIATION_REQUIRED);

    private final StripeRefundClient stripeRefundClient;
    private final RefundTransactionService refundTransactionService;
    private final StripeRefundStatusSynchronizer refundStatusSynchronizer;

    public RefundResult reconcileRefund(Long refundId) {
        RefundTransactionService.RefundLookupTarget refundTarget = refundTransactionService
                .loadRefundForReconciliation(refundId);

        if (!RECONCILIABLE_STATUSES.contains(refundTarget.refundStatus())) {
            return new RefundResult(
                    refundTarget.refundId(),
                    refundTarget.stripeRefundId(),
                    refundTarget.refundStatus());
        }

        try {
            StripeRefundClient.StripeRefundResponse stripeRefund = findStripeRefund(refundTarget);

            if (stripeRefund == null) {
                refundTransactionService.markReconciliationRequired(refundId);

                return getCurrentRefundResult(refundId);
            }

            return refundStatusSynchronizer.synchronizeRefund(refundId, stripeRefund);

        } catch (RestClientException | IllegalStateException exception) {
            refundTransactionService.markReconciliationRequired(refundId);

            throw new IllegalStateException("Failed to reconcile refund: " + refundId, exception);
        }
    }

    private StripeRefundClient.StripeRefundResponse findStripeRefund(
            RefundTransactionService.RefundLookupTarget refundTarget
    ) {
        String stripeRefundId = refundTarget.stripeRefundId();

        if (stripeRefundId != null && !stripeRefundId.isBlank()) {
            return stripeRefundClient.retrieveRefund(stripeRefundId);
        }

        return findRefundByPaymentIntent(refundTarget);
    }

    private StripeRefundClient.StripeRefundResponse findRefundByPaymentIntent(
            RefundTransactionService.RefundLookupTarget refundTarget
    ) {
        String paymentIntentId = refundTarget.paymentIntentId();

        if (paymentIntentId == null || paymentIntentId.isBlank()) {
            throw new IllegalStateException("PaymentIntent is missing for refund: " + refundTarget.refundId());
        }

        String startingAfterRefundId = null;
        StripeRefundClient.StripeRefundResponse matchingRefund = null;

        for (int pageNumber = 0; pageNumber < MAX_REFUND_PAGES; pageNumber++) {
            StripeRefundClient.StripeRefundListResponse refundPage = stripeRefundClient
                    .listRefundsForPaymentIntent(paymentIntentId, startingAfterRefundId);

            if (refundPage == null || refundPage.data() == null) {
                throw new IllegalStateException("Stripe returned an invalid refund list");
            }

            List<StripeRefundClient.StripeRefundResponse> refunds = refundPage.data();

            matchingRefund = findMatchingRefund(
                    refunds,
                    refundTarget.refundId(),
                    matchingRefund);

            if (!refundPage.hasMore()) {
                return matchingRefund;
            }

            startingAfterRefundId = getNextRefundCursor(refunds);
        }

        throw new IllegalStateException("Stripe refund search exceeded the page limit");
    }

    private StripeRefundClient.StripeRefundResponse findMatchingRefund(
            List<StripeRefundClient.StripeRefundResponse> refunds,
            Long refundId,
            StripeRefundClient.StripeRefundResponse matchingRefund
    ) {
        for (StripeRefundClient.StripeRefundResponse stripeRefund : refunds) {
            if (!belongsToRefund(stripeRefund, refundId)) {
                continue;
            }

            if (matchingRefund != null) {
                throw new IllegalStateException("Multiple Stripe refunds match local refund: " + refundId);
            }

            matchingRefund = stripeRefund;
        }

        return matchingRefund;
    }

    private String getNextRefundCursor(
            List<StripeRefundClient.StripeRefundResponse> refunds
    ) {
        if (refunds.isEmpty()) {
            throw new IllegalStateException("Stripe refund pagination returned an empty page");
        }

        String cursor = refunds.getLast().id();

        if (cursor == null || cursor.isBlank()) {
            throw new IllegalStateException("Stripe refund pagination cursor is missing");
        }

        return cursor;
    }

    private boolean belongsToRefund(
            StripeRefundClient.StripeRefundResponse stripeRefund,
            Long refundId) {
        Map<String, String> refundMetadata = stripeRefund.metadata();

        return refundMetadata != null
                && refundId.toString().equals(
                refundMetadata.get("payment_refund_id"));
    }

    private RefundResult getCurrentRefundResult(Long refundId) {
        RefundTransactionService.RefundLookupTarget currentRefund = refundTransactionService
                .loadRefundForReconciliation(refundId);

        return new RefundResult(
                currentRefund.refundId(),
                currentRefund.stripeRefundId(),
                currentRefund.refundStatus());
    }
}
