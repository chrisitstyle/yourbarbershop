
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.refund.event.RefundSucceededEvent;
import pl.barbershopproject.barbershop.user.User;

import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

import static pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;

/**
 * Synchronizes local refund and payment statuses with Stripe.
 * <p>
 * Prevents stale updates from overwriting terminal refund states
 * and publishes successful refund notifications only when the
 * persisted refund transitions to SUCCEEDED.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StripeRefundStatusSynchronizer {

    private final PaymentRefundRepository paymentRefundRepository;
    private final ApplicationEventPublisher eventPublisher;

    /**
     * Synchronizes a refund with the latest response received from Stripe.
     * <p>
     * Notification events are published only on actual transitions
     * to SUCCEEDED. Their listeners run after transaction commit.
     *
     * @param refundId local refund identifier
     * @param stripeRefund verified Stripe refund response
     * @return current persisted refund result
     */
    @Transactional
    public RefundResult synchronizeRefund(
            Long refundId,
            StripeRefundClient.StripeRefundResponse stripeRefund
    ) {
        PaymentRefund paymentRefund = paymentRefundRepository
                .findForUpdateByIdPaymentRefund(refundId)
                .orElseThrow(() -> new IllegalStateException("Payment refund not found: " + refundId));

        validateStripeRefund(paymentRefund, stripeRefund);

        RefundStatus currentStatus = paymentRefund.getRefundStatus();

        RefundStatus receivedStatus = resolveRefundStatus(stripeRefund.status());

        if (isTerminalStatus(currentStatus)) {
            if (isTerminalStatus(receivedStatus)
                    && currentStatus != receivedStatus) {

                paymentRefund.setRefundStatus(RefundStatus.RECONCILIATION_REQUIRED);

                paymentRefund.setFailureReason("Conflicting terminal refund statuses");

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

            if (currentStatus != RefundStatus.SUCCEEDED) {
                publishRefundSucceededNotification(paymentRefund);
            }

        } else if (
                (receivedStatus == RefundStatus.FAILED
                        || receivedStatus == RefundStatus.CANCELED)
                        && payment.getPaymentStatus()
                        == PaymentStatus.ZWROCONA
        ) {
            payment.setPaymentStatus(PaymentStatus.OPLACONA);
        }

        return toRefundResult(paymentRefund);
    }

    /**
     * Publishes a successful refund notification with an immutable
     * snapshot of the customer and reservation details.
     * <p>
     * Notification preparation must not interrupt refund processing.
     *
     * @param paymentRefund successfully completed refund
     */
    private void publishRefundSucceededNotification(
            PaymentRefund paymentRefund
    ) {
        try {
            Payment payment = paymentRefund.getPayment();

            Order registeredOrder = payment.getOrder();
            GuestOrder guestOrder = payment.getGuestOrder();

            if ((registeredOrder == null) == (guestOrder == null)) {
                log.warn("Skipping refund notification for refund {}: "
                                + "payment must reference exactly one reservation",
                        paymentRefund.getIdPaymentRefund());
                return;
            }

            Long reservationId;
            ReservationType reservationType;
            String customerEmail;
            String customerFirstname;

            if (registeredOrder != null) {
                User customer = registeredOrder.getUser();

                if (customer == null) {
                    log.warn("Skipping refund notification for refund {}: "
                                    + "registered customer not found",
                            paymentRefund.getIdPaymentRefund());
                    return;
                }

                reservationId = registeredOrder.getIdOrder();
                reservationType = ReservationType.REGISTERED;

                customerEmail = customer.getEmail();
                customerFirstname = customer.getFirstname();

            } else {
                reservationId = guestOrder.getIdGuestOrder();
                reservationType = ReservationType.GUEST;

                customerEmail = guestOrder.getEmail();
                customerFirstname = guestOrder.getFirstname();
            }

            if (customerEmail == null || customerEmail.isBlank()) {
                log.warn("Skipping refund notification for refund {}: "
                                + "customer email is missing",
                        paymentRefund.getIdPaymentRefund()
                );
                return;
            }

            RefundSucceededEvent event = new RefundSucceededEvent(
                    paymentRefund.getIdPaymentRefund(),
                    reservationId,
                    reservationType,
                    customerEmail,
                    customerFirstname,
                    paymentRefund.getAmount(),
                    paymentRefund.getCurrency());

            eventPublisher.publishEvent(event);

        } catch (RuntimeException exception) {
            log.error("Failed to publish successful refund notification "
                            + "for refund {}",
                    paymentRefund.getIdPaymentRefund(),
                    exception);
        }
    }

    /**
     * Validates the Stripe refund against the locally reserved refund.
     *
     * @param paymentRefund persisted refund
     * @param stripeRefund refund received from Stripe
     */
    private void validateStripeRefund(
            PaymentRefund paymentRefund,
            StripeRefundClient.StripeRefundResponse stripeRefund
    ) {
        if (stripeRefund == null
                || stripeRefund.id() == null
                || stripeRefund.id().isBlank()) {

            throw new IllegalStateException("Stripe returned an invalid refund");
        }

        String existingStripeRefundId =
                paymentRefund.getStripeRefundId();

        if (existingStripeRefundId != null
                && !existingStripeRefundId.equals(stripeRefund.id())) {

            throw new IllegalStateException("Stripe refund ID does not match the stored refund");
        }

        String expectedPaymentIntentId = paymentRefund
                .getPayment()
                .getStripePaymentIntentId();

        long expectedAmountInMinorUnits = paymentRefund
                .getAmount()
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

        String metadataRefundId =
                stripeRefund.metadata() == null
                        ? null
                        : stripeRefund.metadata().get("payment_refund_id");

        if (metadataRefundId != null
                && !metadataRefundId.equals(
                paymentRefund.getIdPaymentRefund().toString()
        )) {

            throw new IllegalStateException("Stripe refund metadata does not match the stored refund");
        }
    }

    /**
     * Converts a Stripe refund status into the application's status.
     *
     * @param stripeStatus status returned by Stripe
     * @return internal refund status
     */
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

    /**
     * Identifies terminal refund processing statuses.
     *
     * @param refundStatus internal refund status
     * @return whether the refund is terminal
     */
    private boolean isTerminalStatus(RefundStatus refundStatus) {
        return refundStatus == RefundStatus.SUCCEEDED
                || refundStatus == RefundStatus.FAILED
                || refundStatus == RefundStatus.CANCELED;
    }

    /**
     * Converts a persisted refund into its API result.
     *
     * @param paymentRefund persisted refund
     * @return refund result
     */
    private RefundResult toRefundResult(PaymentRefund paymentRefund) {
        return new RefundResult(
                paymentRefund.getIdPaymentRefund(),
                paymentRefund.getStripeRefundId(),
                paymentRefund.getRefundStatus());
    }
}
