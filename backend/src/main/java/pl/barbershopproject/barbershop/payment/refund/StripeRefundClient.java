
package pl.barbershopproject.barbershop.payment.refund;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriBuilder;

import java.util.List;
import java.util.Map;

/**
 * Handles HTTP communication with Stripe for payment refunds.
 */
@Component
public class StripeRefundClient {

    private final RestClient stripeRestClient;

    public StripeRefundClient(
            RestClient.Builder restClientBuilder,
            @Value("${stripe.api-base-url}") String stripeApiBaseUrl,
            @Value("${stripe.secret-key}") String stripeSecretKey
    ) {
        this.stripeRestClient = restClientBuilder
                .baseUrl(stripeApiBaseUrl)
                .defaultHeader(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + stripeSecretKey)
                .build();
    }

    public StripePaymentIntentResponse retrievePaymentIntent(
            String paymentIntentId
    ) {
        return stripeRestClient.get()
                .uri("/v1/payment_intents/{paymentIntentId}", paymentIntentId)
                .retrieve()
                .body(StripePaymentIntentResponse.class);
    }

    public StripeRefundResponse createFullRefund(
            String paymentIntentId,
            long amountInMinorUnits,
            String idempotencyKey,
            Long refundId
    ) {
        MultiValueMap<String, String> requestBody = new LinkedMultiValueMap<>();

        requestBody.add("payment_intent", paymentIntentId);
        requestBody.add("amount", Long.toString(amountInMinorUnits));
        requestBody.add("metadata[payment_refund_id]", refundId.toString());

        return stripeRestClient.post()
                .uri("/v1/refunds")
                .header("Idempotency-Key", idempotencyKey)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(requestBody)
                .retrieve()
                .body(StripeRefundResponse.class);
    }

    public StripeRefundResponse retrieveRefund(String stripeRefundId) {
        return stripeRestClient.get()
                .uri("/v1/refunds/{refundId}", stripeRefundId)
                .retrieve()
                .body(StripeRefundResponse.class);
    }


    public StripeRefundListResponse listRefundsForPaymentIntent(
            String paymentIntentId,
            String startingAfterRefundId
    ) {
        return stripeRestClient.get()
                .uri(uriBuilder -> {
                    UriBuilder refundUriBuilder = uriBuilder
                            .path("/v1/refunds")
                            .queryParam("payment_intent", paymentIntentId)
                            .queryParam("limit", 100);

                    if (startingAfterRefundId != null) {
                        refundUriBuilder.queryParam(
                                "starting_after",
                                startingAfterRefundId);
                    }

                    return refundUriBuilder.build();
                })
                .retrieve()
                .body(StripeRefundListResponse.class);
    }

    public record StripeRefundListResponse(
            List<StripeRefundResponse> data,
            @JsonProperty("has_more") boolean hasMore
    ) {
    }


    public record StripePaymentIntentResponse(
            String id,
            String status,
            @JsonProperty("amount_received")
            long amountReceived,
            String currency) { }

    public record StripeRefundResponse(
            String id,
            String status,
            long amount,
            String currency,
            @JsonProperty("payment_intent")
            String paymentIntentId,
            @JsonProperty("failure_reason")
            String failureReason,
            Map<String, String> metadata) { }
}
