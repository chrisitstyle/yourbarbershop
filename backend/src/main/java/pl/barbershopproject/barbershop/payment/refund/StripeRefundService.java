
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

/**
 * Coordinates full Stripe refunds without holding a database transaction
 * during external HTTP requests.
 */
@Service
@RequiredArgsConstructor
public class StripeRefundService {

    private final StripeRefundClient stripeRefundClient;
    private final RefundTransactionService refundTransactionService;
    private final StripeRefundStatusSynchronizer refundStatusSynchronizer;


    public RefundResult requestFullRefund(Long paymentId) {
        RefundTransactionService.RefundReservation refundReservation = refundTransactionService
                .reserveFullRefund(paymentId);

        return submitReservedRefund(refundReservation.refundId());
    }

    /**
     * Submits a previously reserved refund to Stripe.
     * Prevents repeated submissions of the same refund operation.
     */
    public RefundResult submitReservedRefund(Long refundId) {
        RefundTransactionService.RefundReservation refundReservation =
                refundTransactionService.claimRefundForSubmission(refundId)
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.CONFLICT,
                                "Refund has already been submitted"));

        try {
            StripeRefundClient.StripePaymentIntentResponse paymentIntent = stripeRefundClient
                    .retrievePaymentIntent(refundReservation.paymentIntentId());

            validatePaymentIntent(paymentIntent, refundReservation);

            StripeRefundClient.StripeRefundResponse stripeRefund = stripeRefundClient.createFullRefund(
                            refundReservation.paymentIntentId(),
                            refundReservation.amountInMinorUnits(),
                            refundReservation.idempotencyKey(),
                            refundReservation.refundId());

            validateStripeRefund(stripeRefund, refundReservation);

            return refundStatusSynchronizer.synchronizeRefund(refundReservation.refundId(), stripeRefund);

        } catch (RestClientException | IllegalStateException exception) {
            refundTransactionService.markReconciliationRequired(refundReservation.refundId());

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Refund outcome requires verification with Stripe",
                    exception);
        }
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
}
