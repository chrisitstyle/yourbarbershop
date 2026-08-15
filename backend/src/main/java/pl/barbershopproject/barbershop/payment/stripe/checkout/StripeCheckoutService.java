package pl.barbershopproject.barbershop.payment.stripe.checkout;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.Locale;
import java.util.Objects;

@Service
public class StripeCheckoutService {

    private static final String IDEMPOTENCY_KEY_HEADER = "Idempotency-Key";
    private static final String IDEMPOTENCY_KEY_PREFIX = "checkout-session-payment-";

    private static final String CHECKOUT_SESSION_ERROR =
            "Stripe nie zwrócił identyfikatora albo adresu Checkout Session";

    private final RestClient restClient;
    private final String successUrl;
    private final String cancelUrl;

    public StripeCheckoutService(
            RestClient.Builder restClientBuilder,
            @Value("${stripe.api-base-url:https://api.stripe.com}")
            String apiBaseUrl,
            @Value("${stripe.secret-key:}")
            String secretKey,
            @Value("${stripe.success-url:http://localhost:3000?payment=success}")
            String successUrl,
            @Value("${stripe.cancel-url:http://localhost:3000?payment=cancel}")
            String cancelUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .build();

        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public StripeCheckoutSessionResponse createCheckoutSession(
            PaymentCheckoutRequest request) {
        Objects.requireNonNull(
                request,
                "PaymentCheckoutRequest nie może być null");

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();

        body.add("mode", "payment");
        body.add("payment_method_types[0]", "card");
        body.add("success_url", successUrl);
        body.add("cancel_url", cancelUrl);

        body.add("line_items[0][quantity]", "1");

        body.add(
                "line_items[0][price_data][currency]",
                request.currency().toLowerCase(Locale.ROOT));

        body.add(
                "line_items[0][price_data][unit_amount]",
                toSmallestCurrencyUnit(request.amount()).toString());

        body.add(
                "line_items[0][price_data][product_data][name]",
                request.productName());

        String paymentId = request.paymentId().toString();

        body.add(
                "metadata[paymentId]",
                paymentId);

        body.add(
                "payment_intent_data[metadata][paymentId]",
                paymentId);

        String idempotencyKey = IDEMPOTENCY_KEY_PREFIX
                + request.stripeCheckoutIdempotencyKey();

        JsonNode response = restClient.post()
                .uri("/v1/checkout/sessions")
                .header(
                        IDEMPOTENCY_KEY_HEADER,
                        idempotencyKey)
                .contentType(
                        MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        return mapResponse(response);
    }

    public StripeCheckoutSessionDetails retrieveCheckoutSession(
            String sessionId
    ) {
        String requiredSessionId = Objects.requireNonNull(
                sessionId,
                "Stripe Checkout session ID nie może być null");

        if (requiredSessionId.isBlank()) {
            throw new IllegalArgumentException(
                    "Stripe Checkout session ID nie może być pusty");
        }

        JsonNode response = restClient.get()
                .uri(
                        "/v1/checkout/sessions/{sessionId}",
                        requiredSessionId
                )
                .retrieve()
                .body(JsonNode.class);

        return mapSessionDetails(response);
    }

    private StripeCheckoutSessionResponse mapResponse(
            JsonNode response
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    CHECKOUT_SESSION_ERROR
            );
        }

        String sessionId = response.path("id").asString(null);
        String checkoutUrl = response.path("url").asString(null);

        if (sessionId == null
                || sessionId.isBlank()
                || checkoutUrl == null
                || checkoutUrl.isBlank()) {
            throw new IllegalStateException(CHECKOUT_SESSION_ERROR);
        }

        return new StripeCheckoutSessionResponse(
                sessionId,
                checkoutUrl
        );
    }

    private StripeCheckoutSessionDetails mapSessionDetails(
            JsonNode response
    ) {
        if (response == null) {
            throw new IllegalStateException(
                    "Stripe nie zwrócił Checkout Session");
        }

        String sessionId = response.path("id").asString(null);
        String status = response.path("status").asString(null);
        String paymentStatus = response
                .path("payment_status")
                .asString(null);

        long expiresAt = response
                .path("expires_at")
                .asLong(0);

        if (sessionId == null
                || sessionId.isBlank()
                || status == null
                || status.isBlank()
                || paymentStatus == null
                || paymentStatus.isBlank()
                || expiresAt <= 0) {
            throw new IllegalStateException(
                    "Stripe zwrócił niekompletną Checkout Session");
        }

        String checkoutUrl = response
                .path("url")
                .asString(null);

        return new StripeCheckoutSessionDetails(
                sessionId,
                StripeCheckoutSessionStatus.from(status),
                paymentStatus,
                checkoutUrl,
                Instant.ofEpochSecond(expiresAt)
        );
    }

    private Long toSmallestCurrencyUnit(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}