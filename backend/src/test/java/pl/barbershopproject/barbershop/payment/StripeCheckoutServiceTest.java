package pl.barbershopproject.barbershop.payment;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pl.barbershopproject.barbershop.offer.Offer;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.postRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripeCheckoutServiceTest {

    private WireMockServer stripeMock;

    @BeforeEach
    void startWireMock() {
        stripeMock = new WireMockServer(options().dynamicPort());
        stripeMock.start();
    }

    @AfterEach
    void stopWireMock() {
        stripeMock.stop();
    }

    @Test
    void shouldCreateStripeCheckoutSessionForOrder() {
        stripeMock.stubFor(post(urlEqualTo("/v1/checkout/sessions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "cs_test_123",
                                  "url": "https://checkout.stripe.com/c/pay/cs_test_123"
                                }
                                """)));

        StripeCheckoutService service = new StripeCheckoutService(
                RestClient.builder(),
                stripeMock.baseUrl(),
                "sk_test_123",
                "pln",
                "http://localhost:3000?payment=success",
                "http://localhost:3000?payment=cancel"
        );

        Offer offer = Offer.builder()
                .idOffer(7L)
                .kind("Strzyżenie")
                .cost(BigDecimal.valueOf(80))
                .build();

        StripeCheckoutSessionResponse response = service.createCheckoutSession(
                PaymentTargetType.ORDER,
                15L,
                offer
        );

        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/c/pay/cs_test_123");

        stripeMock.verify(postRequestedFor(urlEqualTo("/v1/checkout/sessions"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withRequestBody(containing("mode=payment"))
                .withRequestBody(containing("unit_amount=8000"))
                .withRequestBody(containing("metadata"))
                .withRequestBody(containing("ORDER"))
                .withRequestBody(containing("15")));
    }

    @Test
    void shouldThrowExceptionWhenStripeReturnsServerError() {
        stripeMock.stubFor(post(urlEqualTo("/v1/checkout/sessions"))
                .willReturn(aResponse()
                        .withStatus(500)
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "error": {
                                    "message": "Stripe temporary error"
                                  }
                                }
                                """)));

        StripeCheckoutService service = new StripeCheckoutService(
                RestClient.builder(),
                stripeMock.baseUrl(),
                "sk_test_123",
                "pln",
                "http://localhost:3000?payment=success",
                "http://localhost:3000?payment=cancel"
        );

        Offer offer = Offer.builder()
                .idOffer(7L)
                .kind("Strzyżenie")
                .cost(BigDecimal.valueOf(80))
                .build();

        assertThatThrownBy(() -> service.createCheckoutSession(PaymentTargetType.ORDER, 15L, offer))
                .isInstanceOf(RestClientResponseException.class);
    }
}
