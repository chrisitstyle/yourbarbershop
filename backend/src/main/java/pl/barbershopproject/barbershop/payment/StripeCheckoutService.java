package pl.barbershopproject.barbershop.payment;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import pl.barbershopproject.barbershop.offer.Offer;
import tools.jackson.databind.JsonNode;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class StripeCheckoutService {

    private final RestClient restClient;
    private final String currency;
    private final String successUrl;
    private final String cancelUrl;

    public StripeCheckoutService(
            RestClient.Builder restClientBuilder,
            @Value("${stripe.api-base-url:https://api.stripe.com}") String apiBaseUrl,
            @Value("${stripe.secret-key:}") String secretKey,
            @Value("${stripe.currency:pln}") String currency,
            @Value("${stripe.success-url:http://localhost:3000?payment=success}") String successUrl,
            @Value("${stripe.cancel-url:http://localhost:3000?payment=cancel}") String cancelUrl
    ) {
        this.restClient = restClientBuilder
                .baseUrl(apiBaseUrl)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + secretKey)
                .build();
        this.currency = currency;
        this.successUrl = successUrl;
        this.cancelUrl = cancelUrl;
    }

    public StripeCheckoutSessionResponse createCheckoutSession(
            Payment payment,
            Offer offer
    ) {
        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("mode", "payment");
        body.add("payment_method_types[0]", "card");
        body.add("success_url", successUrl);
        body.add("cancel_url", cancelUrl);

        body.add("line_items[0][quantity]", "1");
        body.add("line_items[0][price_data][currency]", currency);
        body.add("line_items[0][price_data][unit_amount]", toSmallestCurrencyUnit(payment.getAmount()).toString());
        body.add("line_items[0][price_data][product_data][name]", offer.getKind());

        body.add("metadata[paymentId]", payment.getIdPayment().toString());
        body.add("payment_intent_data[metadata][paymentId]", payment.getIdPayment().toString());
        body.add("payment_method_types[0]", "card"); // only card payment

        JsonNode response = restClient.post()
                .uri("/v1/checkout/sessions")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(body)
                .retrieve()
                .body(JsonNode.class);

        if (response == null || response.path("id").isMissingNode() || response.path("url").isMissingNode()) {
            throw new IllegalStateException("Stripe nie zwrócił identyfikatora albo adresu Checkout Session");
        }

        return new StripeCheckoutSessionResponse(
                response.path("id").asText(),
                response.path("url").asText()
        );
    }

    private Long toSmallestCurrencyUnit(BigDecimal amount) {
        return amount
                .multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}