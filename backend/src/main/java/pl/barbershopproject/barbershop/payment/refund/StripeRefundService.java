
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * Coordinates full Stripe refunds without holding a database transaction
 * during external HTTP requests.
 */
@Service
@RequiredArgsConstructor
public class StripeRefundService {

    private final StripeRefundClient stripeRefundClient;
    private final RefundTransactionService refundTransactionService;

    public RefundResult requestFullRefund(Long paymentId) {
        RefundTransactionService.RefundReservation refundReservation =
                refundTransactionService.reserveFullRefund(paymentId);

        StripeRefundClient.StripeRefundResponse stripeResponse;
        RefundStatus refundStatus;

        try {
            StripeRefundClient.StripePaymentIntentResponse paymentIntent = stripeRefundClient.retrievePaymentIntent(
                            refundReservation.paymentIntentId());

            validatePaymentIntent(paymentIntent, refundReservation);

            stripeResponse = stripeRefundClient.createFullRefund(
                    refundReservation.paymentIntentId(),
                    refundReservation.amountInMinorUnits(),
                    refundReservation.idempotencyKey(),
                    refundReservation.refundId());

            validateStripeRefund(stripeResponse, refundReservation);

            refundStatus = resolveRefundStatus(stripeResponse.status());

        } catch (RestClientException | IllegalStateException exception) {
            refundTransactionService.markReconciliationRequired(refundReservation.refundId());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Refund outcome requires verification with Stripe",
                    exception);
        }

        return refundTransactionService.recordStripeRefund(
                refundReservation.refundId(),
                stripeResponse,
                refundStatus);
    }

    private void validatePaymentIntent(
            StripeRefundClient.StripePaymentIntentResponse paymentIntent,
            RefundTransactionService.RefundReservation refundReservation
    ) {
        if (paymentIntent == null
                || !refundReservation.paymentIntentId()
                .equals(paymentIntent.id())
                || !"succeeded".equals(paymentIntent.status())
                || paymentIntent.amountReceived()
                != refundReservation.amountInMinorUnits()
                || !refundReservation.currency()
                .equalsIgnoreCase(paymentIntent.currency())) {

            throw new IllegalStateException("Stripe PaymentIntent does not match the stored payment");
        }
    }

    private void validateStripeRefund(
            StripeRefundClient.StripeRefundResponse stripeResponse,
            RefundTransactionService.RefundReservation refundReservation
    ) {
        if (stripeResponse == null
                || stripeResponse.id() == null
                || stripeResponse.id().isBlank()
                || !refundReservation.paymentIntentId()
                .equals(stripeResponse.paymentIntentId())
                || stripeResponse.amount()
                != refundReservation.amountInMinorUnits()
                || !refundReservation.currency()
                .equalsIgnoreCase(stripeResponse.currency())) {

            throw new IllegalStateException("Stripe refund response does not match the requested refund");
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
            default -> throw new IllegalStateException(
                    "Unsupported Stripe refund status");
        };
    }
}
