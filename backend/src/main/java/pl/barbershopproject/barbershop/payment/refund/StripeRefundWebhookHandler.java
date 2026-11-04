package pl.barbershopproject.barbershop.payment.refund;

import tools.jackson.core.JacksonException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.Set;

/**
 * Processes verified Stripe refund webhook events.
 * Retrieves the current refund state before updating local records.
 */
@Service
@RequiredArgsConstructor
public class StripeRefundWebhookHandler {

    private static final Set<String> REFUND_EVENT_TYPES = Set.of(
            "refund.created",
            "refund.updated",
            "refund.failed");

    private final ObjectMapper objectMapper;
    private final StripeRefundClient stripeRefundClient;
    private final PaymentRefundRepository paymentRefundRepository;
    private final StripeRefundStatusSynchronizer refundStatusSynchronizer;

    public boolean handleVerifiedRefundEvent(String verifiedPayload) {
        JsonNode stripeEvent = parseStripeEvent(verifiedPayload);
        String eventType = stripeEvent.path("type").asText();

        if (!REFUND_EVENT_TYPES.contains(eventType)) {
            return false;
        }

        String stripeRefundId = stripeEvent
                .path("data")
                .path("object")
                .path("id")
                .asText();

        if (!stripeRefundId.startsWith("re_")) {
            throw new IllegalArgumentException(
                    "Stripe webhook contains an invalid refund ID");
        }

        StripeRefundClient.StripeRefundResponse stripeRefund = stripeRefundClient.retrieveRefund(stripeRefundId);

        Long paymentRefundId = findPaymentRefundId(stripeRefund);

        if (paymentRefundId != null) {
            refundStatusSynchronizer.synchronizeRefund(paymentRefundId, stripeRefund);
        }

        return true;
    }

    private Long findPaymentRefundId(
            StripeRefundClient.StripeRefundResponse stripeRefund
    ) {
        String stripeRefundId = stripeRefund.id();

        var existingRefund = paymentRefundRepository
                .findByStripeRefundId(stripeRefundId);

        if (existingRefund.isPresent()) {
            return existingRefund.get().getIdPaymentRefund();
        }

        String metadataRefundId = stripeRefund.metadata() == null
                ? null
                : stripeRefund.metadata().get("payment_refund_id");

        if (metadataRefundId == null || metadataRefundId.isBlank()) {
            // refund created outside the application's refund flow.
            return null;
        }

        try {
            Long paymentRefundId = Long.valueOf(metadataRefundId);

            if (!paymentRefundRepository.existsById(paymentRefundId)) {
                throw new IllegalStateException("Local refund referenced by Stripe was not found");
            }

            return paymentRefundId;

        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Invalid payment_refund_id in Stripe metadata",
                    exception);
        }
    }

    private JsonNode parseStripeEvent(String verifiedPayload) {
        try {
            return objectMapper.readTree(verifiedPayload);
        } catch (JacksonException exception) {
            throw new IllegalArgumentException(
                    "Invalid Stripe webhook payload",
                    exception);
        }
    }
}
