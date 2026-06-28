package pl.barbershopproject.barbershop.payment;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pl.barbershopproject.barbershop.offer.Offer;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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
    void shouldCreateStripeCheckoutSessionForPayment() {
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
                "http://localhost:3000/payment/success",
                "http://localhost:3000/payment/cancel"
        );

        Offer offer = Offer.builder()
                .idOffer(7L)
                .kind("Strzyżenie")
                .cost(BigDecimal.valueOf(80))
                .build();

        Payment payment = Payment.builder()
                .idPayment(15L)
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .amount(BigDecimal.valueOf(80))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        StripeCheckoutSessionResponse response = service.createCheckoutSession(
                payment,
                offer
        );

        assertThat(response.sessionId()).isEqualTo("cs_test_123");
        assertThat(response.checkoutUrl()).isEqualTo("https://checkout.stripe.com/c/pay/cs_test_123");

        stripeMock.verify(postRequestedFor(urlEqualTo("/v1/checkout/sessions"))
                .withHeader("Authorization", equalTo("Bearer sk_test_123"))
                .withRequestBody(containing("mode=payment"))
                .withRequestBody(containing("payment_method_types%5B0%5D=card"))
                .withRequestBody(containing("success_url=http%3A%2F%2Flocalhost%3A3000%2Fpayment%2Fsuccess"))
                .withRequestBody(containing("cancel_url=http%3A%2F%2Flocalhost%3A3000%2Fpayment%2Fcancel"))
                .withRequestBody(containing("line_items%5B0%5D%5Bquantity%5D=1"))
                .withRequestBody(containing("line_items%5B0%5D%5Bprice_data%5D%5Bcurrency%5D=pln"))
                .withRequestBody(containing("line_items%5B0%5D%5Bprice_data%5D%5Bunit_amount%5D=8000"))
                .withRequestBody(containing("line_items%5B0%5D%5Bprice_data%5D%5Bproduct_data%5D%5Bname%5D=Strzy%C5%BCenie"))
                .withRequestBody(containing("metadata%5BpaymentId%5D=15"))
                .withRequestBody(containing("payment_intent_data%5Bmetadata%5D%5BpaymentId%5D=15")));
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
                "http://localhost:3000/payment/success",
                "http://localhost:3000/payment/cancel"
        );

        Offer offer = Offer.builder()
                .idOffer(7L)
                .kind("Strzyżenie")
                .cost(BigDecimal.valueOf(80))
                .build();

        Payment payment = Payment.builder()
                .idPayment(15L)
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .amount(BigDecimal.valueOf(80))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> service.createCheckoutSession(payment, offer))
                .isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void shouldThrowExceptionWhenStripeDoesNotReturnSessionIdOrUrl() {
        stripeMock.stubFor(post(urlEqualTo("/v1/checkout/sessions"))
                .willReturn(aResponse()
                        .withHeader("Content-Type", "application/json")
                        .withBody("""
                                {
                                  "id": "cs_test_123"
                                }
                                """)));

        StripeCheckoutService service = new StripeCheckoutService(
                RestClient.builder(),
                stripeMock.baseUrl(),
                "sk_test_123",
                "pln",
                "http://localhost:3000/payment/success",
                "http://localhost:3000/payment/cancel"
        );

        Offer offer = Offer.builder()
                .idOffer(7L)
                .kind("Strzyżenie")
                .cost(BigDecimal.valueOf(80))
                .build();

        Payment payment = Payment.builder()
                .idPayment(15L)
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .amount(BigDecimal.valueOf(80))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .build();

        assertThatThrownBy(() -> service.createCheckoutSession(payment, offer))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Stripe nie zwrócił identyfikatora albo adresu Checkout Session");
    }
}