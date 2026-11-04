
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Periodically reconciles unfinished refunds without creating new refunds.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "application.refunds.reconciliation.enabled",
        havingValue = "true")
public class RefundReconciliationScheduler {

    private static final int BATCH_SIZE = 50;

    private static final Set<RefundStatus> RECONCILIABLE_STATUSES =
            EnumSet.of(
                    RefundStatus.REQUESTED,
                    RefundStatus.PENDING,
                    RefundStatus.REQUIRES_ACTION,
                    RefundStatus.RECONCILIATION_REQUIRED);

    private final PaymentRefundRepository paymentRefundRepository;
    private final RefundReconciliationService refundReconciliationService;

    @Value("${application.refunds.reconciliation.minimum-age-seconds:120}")
    private long minimumRefundAgeSeconds;

    @Scheduled(
            fixedDelayString = "${application.refunds.reconciliation.delay-ms:300000}",
            initialDelayString = "${application.refunds.reconciliation.delay-ms:300000}")
    public void reconcileOutstandingRefunds() {
        Instant requestedBefore = Instant.now()
                .minus(Duration.ofSeconds(minimumRefundAgeSeconds));

        Long lastProcessedRefundId = 0L;

        while (true) {
            List<PaymentRefund> pendingRefunds = paymentRefundRepository
                    .findByRefundStatusInAndRequestedAtBeforeAndIdPaymentRefundGreaterThanOrderByIdPaymentRefundAsc(
                            RECONCILIABLE_STATUSES,
                            requestedBefore,
                            lastProcessedRefundId,
                            PageRequest.of(0, BATCH_SIZE));

            if (pendingRefunds.isEmpty()) {
                return;
            }

            for (PaymentRefund paymentRefund : pendingRefunds) {
                Long refundId = paymentRefund.getIdPaymentRefund();

                try {
                    refundReconciliationService.reconcileRefund(refundId);
                } catch (Exception exception) {
                    log.error("Failed to reconcile payment refund {}",
                            refundId,
                            exception);
                }

                lastProcessedRefundId = refundId;
            }

            if (pendingRefunds.size() < BATCH_SIZE) {
                return;
            }
        }
    }
}
