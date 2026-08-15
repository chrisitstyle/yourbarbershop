package pl.barbershopproject.barbershop.payment;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutService;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutSessionDetails;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutSessionResponse;
import pl.barbershopproject.barbershop.payment.stripe.checkout.StripeCheckoutSessionStatus;

import java.math.BigDecimal;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StripeCheckoutServiceTest {

    private static final String STRIPE_CHECKOUT_IDEMPOTENCY_KEY = "550e8400-e29b-41d4-a716-446655440000";

    private static final String IDEMPOTENCY_KEY = "checkout-session-payment-" + STRIPE_CHECKOUT_IDEMPOTENCY_KEY;
    private static final String CHECKOUT_ENDPOINT = "/v1/checkout/sessions";
    private static final String SECRET_KEY = "sk_test_123";
    private static final String SESSION_ID = "cs_test_123";
    private static final String CHECKOUT_URL = "https://checkout.stripe.com/c/pay/cs_test_123";
    private static final String SUCCESS_URL = "http://localhost:3000/payment/success";
    private static final String CANCEL_URL = "http://localhost:3000/payment/cancel";
    private static final String SESSION_ENDPOINT = "/v1/checkout/sessions/" + SESSION_ID;

    private WireMockServer stripeMock;
    private StripeCheckoutService stripeCheckoutService;

    @BeforeEach
    void setUp() {
        stripeMock = new WireMockServer(options().dynamicPort());
        stripeMock.start();

        stripeCheckoutService = new StripeCheckoutService(
                RestClient.builder(),
                stripeMock.baseUrl(),
                SECRET_KEY,
                SUCCESS_URL,
                CANCEL_URL);
    }

    @AfterEach
    void tearDown() {
        stripeMock.stop();
    }

    @Test
    void shouldCreateStripeCheckoutSession() {
        stubSuccessfulCheckoutResponse();

        PaymentCheckoutRequest request = createCheckoutRequest();

        StripeCheckoutSessionResponse response =
                stripeCheckoutService.createCheckoutSession(request);

        assertThat(response.sessionId()).isEqualTo(SESSION_ID);
        assertThat(response.checkoutUrl()).isEqualTo(CHECKOUT_URL);

        verifyCheckoutRequest();
    }

    @Test
    void shouldThrowExceptionWhenStripeReturnsServerError() {
        stubStripeResponse(
                500,
                """
                        {
                          "error": {
                            "message": "Stripe temporary error"
                          }
                        }
                        """
        );

        PaymentCheckoutRequest request = createCheckoutRequest();

        assertThatThrownBy(() -> stripeCheckoutService.createCheckoutSession(request))
                .isInstanceOf(RestClientResponseException.class);
    }

    @Test
    void shouldThrowExceptionWhenStripeDoesNotReturnCheckoutUrl() {
        stubStripeResponse(
                200,
                """
                        {
                          "id": "cs_test_123"
                        }
                        """
        );

        PaymentCheckoutRequest request = createCheckoutRequest();

        assertThatThrownBy(() -> stripeCheckoutService.createCheckoutSession(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Stripe nie zwrócił identyfikatora albo adresu Checkout Session");
    }

    @Test
    void shouldThrowExceptionWhenStripeReturnsBlankSessionId() {
        stubStripeResponse(
                200,
                """
                        {
                          "id": "",
                          "url": "https://checkout.stripe.com/c/pay/cs_test_123"
                        }
                        """
        );

        PaymentCheckoutRequest request = createCheckoutRequest();

        assertThatThrownBy(() -> stripeCheckoutService.createCheckoutSession(request))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Stripe nie zwrócił identyfikatora albo adresu Checkout Session");
    }

    @Test
    void shouldThrowExceptionWhenRequestIsNull() {
        assertThatThrownBy(() -> stripeCheckoutService.createCheckoutSession(null))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("PaymentCheckoutRequest nie może być null");
    }

    private PaymentCheckoutRequest createCheckoutRequest() {
        return new PaymentCheckoutRequest(
                15L,
                PaymentMethod.KARTA_ONLINE,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC,
                STRIPE_CHECKOUT_IDEMPOTENCY_KEY,
                new BigDecimal("80.00"),
                "PLN",
                "Strzyżenie");
    }

    private void stubSuccessfulCheckoutResponse() {
        stubStripeResponse(
                200,
                """
                        {
                          "id": "cs_test_123",
                          "url": "https://checkout.stripe.com/c/pay/cs_test_123"
                        }
                        """
        );
    }

    private void stubStripeResponse(int status, String responseBody) {
        stripeMock.stubFor(
                post(urlEqualTo(CHECKOUT_ENDPOINT))
                        .willReturn(
                                aResponse()
                                        .withStatus(status)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody(responseBody)
                        )
        );
    }

    private void verifyCheckoutRequest() {
        stripeMock.verify(
                postRequestedFor(urlEqualTo(CHECKOUT_ENDPOINT))
                        .withHeader("Authorization", equalTo("Bearer " + SECRET_KEY))
                        .withHeader("Idempotency-Key", equalTo(IDEMPOTENCY_KEY))
                        .withRequestBody(containing("mode=payment"))
                        .withRequestBody(containing("payment_method_types%5B0%5D=card"))
                        .withRequestBody(containing(
                                "success_url=http%3A%2F%2Flocalhost%3A3000%2Fpayment%2Fsuccess"
                        ))
                        .withRequestBody(containing(
                                "cancel_url=http%3A%2F%2Flocalhost%3A3000%2Fpayment%2Fcancel"))
                        .withRequestBody(containing(
                                "line_items%5B0%5D%5Bquantity%5D=1"))
                        .withRequestBody(containing(
                                "line_items%5B0%5D%5Bprice_data%5D%5Bcurrency%5D=pln"))
                        .withRequestBody(containing(
                                "line_items%5B0%5D%5Bprice_data%5D%5Bunit_amount%5D=8000"))
                        .withRequestBody(containing(
                                "line_items%5B0%5D%5Bprice_data%5D%5Bproduct_data%5D%5Bname%5D=Strzy%C5%BCenie"))
                        .withRequestBody(containing(
                                "metadata%5BpaymentId%5D=15"))
                        .withRequestBody(containing(
                                "payment_intent_data%5Bmetadata%5D%5BpaymentId%5D=15"))
        );
    }

    @Test
    void shouldRetrieveOpenStripeCheckoutSession() {
        stripeMock.stubFor(
                get(urlEqualTo(SESSION_ENDPOINT))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(
                                                """
                                                {
                                                  "id": "cs_test_123",
                                                  "status": "open",
                                                  "payment_status": "unpaid",
                                                  "expires_at": 1786125600,
                                                  "url": "https://checkout.stripe.com/c/pay/cs_test_123"
                                                }
                                                """
                                        )
                        )
        );

        StripeCheckoutSessionDetails result = stripeCheckoutService.retrieveCheckoutSession(
                        SESSION_ID
                );

        assertThat(result.sessionId())
                .isEqualTo(SESSION_ID);

        assertThat(result.status())
                .isEqualTo(StripeCheckoutSessionStatus.OPEN);

        assertThat(result.paymentStatus())
                .isEqualTo("unpaid");

        assertThat(result.checkoutUrl())
                .isEqualTo(CHECKOUT_URL);

        assertThat(result.expiresAt())
                .isEqualTo(
                        java.time.Instant.ofEpochSecond(1786125600)
                );

        assertThat(result.isOpen()).isTrue();

        stripeMock.verify(
                getRequestedFor(urlEqualTo(SESSION_ENDPOINT))
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer " + SECRET_KEY)));
    }

    @Test
    void shouldRetrieveExpiredStripeCheckoutSessionWithoutCheckoutUrl() {
        stripeMock.stubFor(
                get(urlEqualTo(SESSION_ENDPOINT))
                        .willReturn(
                                aResponse()
                                        .withStatus(200)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        )
                                        .withBody(
                                                """
                                                {
                                                  "id": "cs_test_123",
                                                  "status": "expired",
                                                  "payment_status": "unpaid",
                                                  "expires_at": 1786125600,
                                                  "url": null
                                                }
                                                """
                                        )
                        )
        );

        StripeCheckoutSessionDetails result =
                stripeCheckoutService.retrieveCheckoutSession(
                        SESSION_ID
                );

        assertThat(result.status())
                .isEqualTo(StripeCheckoutSessionStatus.EXPIRED);

        assertThat(result.paymentStatus())
                .isEqualTo("unpaid");

        assertThat(result.checkoutUrl()).isNull();

        assertThat(result.isOpen()).isFalse();
    }

    @Test
    void shouldThrowExceptionWhenCheckoutSessionIdIsBlank() {
        assertThatThrownBy(() -> stripeCheckoutService.retrieveCheckoutSession("   ")
        )
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage(
                        "Stripe Checkout session ID nie może być pusty"
                );
    }
}