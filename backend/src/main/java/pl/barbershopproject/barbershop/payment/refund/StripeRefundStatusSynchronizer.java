
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

/**
 * Synchronizes local refund and payment statuses with Stripe.
 * Prevents stale updates from overwriting terminal refund states.
 */
@Service
@RequiredArgsConstructor
public class StripeRefundStatusSynchronizer {

    private final PaymentRefundRepository paymentRefundRepository;


    @Transactional
    public RefundResult synchronizeRefund(
            Long refundId,
            StripeRefundClient.StripeRefundResponse stripeRefund
    ) {
        PaymentRefund paymentRefund = paymentRefundRepository
                .findForUpdateByIdPaymentRefund(refundId)
                .orElseThrow(() -> new IllegalStateException(
                        "Payment refund not found: " + refundId));

        validateStripeRefund(paymentRefund, stripeRefund);

        RefundStatus currentStatus = paymentRefund.getRefundStatus();
        RefundStatus receivedStatus = resolveRefundStatus(stripeRefund.status());

        if (isTerminalStatus(currentStatus)) {
            if (isTerminalStatus(receivedStatus)
                    && currentStatus != receivedStatus) {

                paymentRefund.setRefundStatus(
                        RefundStatus.RECONCILIATION_REQUIRED
                );
                paymentRefund.setFailureReason(
                        "Conflicting terminal refund statuses"
                );
                paymentRefund.setCompletedAt(null);

                return toRefundResult(paymentRefund);
            }

            return toRefundResult(paymentRefund);
        }

        paymentRefund.setStripeRefundId(stripeRefund.id());
        paymentRefund.setRefundStatus(receivedStatus);
        paymentRefund.setFailureReason(stripeRefund.failureReason());

        if (isTerminalStatus(receivedStatus)) {
            paymentRefund.setCompletedAt(Instant.now());
        } else {
            paymentRefund.setCompletedAt(null);
        }

        Payment payment = paymentRefund.getPayment();

        if (receivedStatus == RefundStatus.SUCCEEDED) {
            payment.setPaymentStatus(PaymentStatus.ZWROCONA);

        } else if ((receivedStatus == RefundStatus.FAILED
                || receivedStatus == RefundStatus.CANCELED)
                && payment.getPaymentStatus() == PaymentStatus.ZWROCONA) {

            payment.setPaymentStatus(PaymentStatus.OPLACONA);
        }

        return toRefundResult(paymentRefund);
    }


    private void validateStripeRefund(
            PaymentRefund paymentRefund,
            StripeRefundClient.StripeRefundResponse stripeRefund
    ) {
        if (stripeRefund == null
                || stripeRefund.id() == null
                || stripeRefund.id().isBlank()) {
            throw new IllegalStateException("Stripe returned an invalid refund");
        }

        String existingStripeRefundId = paymentRefund.getStripeRefundId();

        if (existingStripeRefundId != null
                && !existingStripeRefundId.equals(stripeRefund.id())) {
            throw new IllegalStateException("Stripe refund ID does not match the stored refund");
        }

        String expectedPaymentIntentId = paymentRefund
                .getPayment()
                .getStripePaymentIntentId();

        long expectedAmountInMinorUnits = paymentRefund.getAmount()
                .movePointRight(2)
                .longValueExact();

        if (!Objects.equals(
                expectedPaymentIntentId,
                stripeRefund.paymentIntentId()
        )
                || expectedAmountInMinorUnits != stripeRefund.amount()
                || !paymentRefund.getCurrency().equalsIgnoreCase(
                stripeRefund.currency()
        )) {

            throw new IllegalStateException("Stripe refund does not match the stored payment");
        }

        String metadataRefundId = stripeRefund.metadata() == null
                ? null
                : stripeRefund.metadata().get("payment_refund_id");

        if (metadataRefundId != null
                && !metadataRefundId.equals(
                paymentRefund.getIdPaymentRefund().toString()
        )) {

            throw new IllegalStateException("Stripe refund metadata does not match the stored refund");
        }
    }

    private RefundStatus resolveRefundStatus(String stripeStatus) {
        if (stripeStatus == null) {
            throw new IllegalStateException("Missing Stripe refund status");
        }

        return switch (stripeStatus.toLowerCase(Locale.ROOT)) {
            case "pending" -> RefundStatus.PENDING;
            case "succeeded" -> RefundStatus.SUCCEEDED;
            case "failed" -> RefundStatus.FAILED;
            case "canceled" -> RefundStatus.CANCELED;
            case "requires_action" -> RefundStatus.REQUIRES_ACTION;
            default -> throw new IllegalStateException("Unsupported Stripe refund status: " + stripeStatus);
        };
    }

    private boolean isTerminalStatus(RefundStatus refundStatus) {
        return refundStatus == RefundStatus.SUCCEEDED
                || refundStatus == RefundStatus.FAILED
                || refundStatus == RefundStatus.CANCELED;
    }

    private RefundResult toRefundResult(PaymentRefund paymentRefund) {
        return new RefundResult(
                paymentRefund.getIdPaymentRefund(),
                paymentRefund.getStripeRefundId(),
                paymentRefund.getRefundStatus());
    }
}
