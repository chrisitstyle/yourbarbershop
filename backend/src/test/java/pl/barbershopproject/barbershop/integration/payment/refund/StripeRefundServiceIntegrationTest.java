package pl.barbershopproject.barbershop.integration.payment.refund;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentRepository;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.refund.*;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.junit.jupiter.api.Assertions.*;

class StripeRefundServiceIntegrationTest extends BaseIntegrationTest {

    private static final String PAYMENT_INTENT_ID = "pi_refund_test";
    private static final BigDecimal PAYMENT_AMOUNT = new BigDecimal("80.00");

    private static final WireMockServer stripeTestServer = new WireMockServer(
                    com.github.tomakehurst.wiremock.core.WireMockConfiguration
                            .options()
                            .dynamicPort());

    static {stripeTestServer.start();}

    @DynamicPropertySource
    static void configureStripeProperties(DynamicPropertyRegistry registry) {
        registry.add("stripe.api-base-url", stripeTestServer::baseUrl);
        registry.add("stripe.secret-key", () -> "sk_test_wiremock");
    }

    @Autowired
    private StripeRefundService stripeRefundService;

    @Autowired
    private PaymentRefundRepository paymentRefundRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void resetStripeStubs() {stripeTestServer.resetAll();}

    @AfterAll
    static void stopStripeServer() {stripeTestServer.stop();}

    @Test
    void shouldRefundFullPaymentAndPreventDuplicateRefund() {
        Payment payment = createPaidOnlinePayment();

        stubSuccessfulPaymentIntent();
        stubRefundResponse("succeeded");

        RefundResult refundResult = stripeRefundService.requestFullRefund(payment.getIdPayment());

        assertEquals(RefundStatus.SUCCEEDED, refundResult.refundStatus());
        assertEquals("re_test_123", refundResult.stripeRefundId());

        Payment updatedPayment = paymentRepository
                .findById(payment.getIdPayment())
                .orElseThrow();

        assertEquals(PaymentStatus.ZWROCONA,
                updatedPayment.getPaymentStatus());

        PaymentRefund savedRefund = getRefund(payment.getIdPayment());

        assertEquals(PAYMENT_AMOUNT, savedRefund.getAmount());
        assertNotNull(savedRefund.getCompletedAt());

        verifyRefundRequestCount(1);

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stripeRefundService.requestFullRefund(payment.getIdPayment()));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());

        verifyRefundRequestCount(1);
    }

    @Test
    void shouldPersistPendingRefundWithoutMarkingPaymentAsRefunded() {
        Payment payment = createPaidOnlinePayment();

        stubSuccessfulPaymentIntent();
        stubRefundResponse("pending");

        RefundResult refundResult = stripeRefundService.requestFullRefund(payment.getIdPayment());

        assertEquals(RefundStatus.PENDING, refundResult.refundStatus());

        Payment updatedPayment = paymentRepository
                .findById(payment.getIdPayment())
                .orElseThrow();

        assertEquals(PaymentStatus.OPLACONA, updatedPayment.getPaymentStatus());

        assertNull(getRefund(payment.getIdPayment()).getCompletedAt());
    }

    @Test
    void shouldRequireReconciliationWhenStripeReturnsServerError() {
        Payment payment = createPaidOnlinePayment();

        stubSuccessfulPaymentIntent();

        stripeTestServer.stubFor(
                post(urlEqualTo("/v1/refunds"))
                        .willReturn(
                                aResponse()
                                        .withStatus(500)
                                        .withHeader("Content-Type", "application/json")
                                        .withBody("{}")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stripeRefundService.requestFullRefund(
                        payment.getIdPayment()));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());

        PaymentRefund savedRefund = getRefund(payment.getIdPayment());

        assertEquals(RefundStatus.RECONCILIATION_REQUIRED,
                savedRefund.getRefundStatus());

        assertNotNull(savedRefund.getIdempotencyKey());

        ResponseStatusException repeatedRequest = assertThrows(
                ResponseStatusException.class,
                () -> stripeRefundService.requestFullRefund(
                        payment.getIdPayment()));

        assertEquals(HttpStatus.CONFLICT, repeatedRequest.getStatusCode());

        verifyRefundRequestCount(1);
    }

    @Test
    void shouldRejectRefundWhenStripePaymentAmountDoesNotMatch() {
        Payment payment = createPaidOnlinePayment();

        stripeTestServer.stubFor(
                get(urlEqualTo("/v1/payment_intents/" + PAYMENT_INTENT_ID))
                        .willReturn(okJson("""
                                {
                                  "id": "pi_refund_test",
                                  "status": "succeeded",
                                  "amount_received": 7000,
                                  "currency": "pln"
                                }
                                """)));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> stripeRefundService.requestFullRefund(payment.getIdPayment()));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());

        assertEquals(RefundStatus.RECONCILIATION_REQUIRED,
                getRefund(payment.getIdPayment()).getRefundStatus());

        verifyRefundRequestCount(0);
    }

    private Payment createPaidOnlinePayment() {
        Offer selectedOffer = offerRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        Order order = Order.builder()
                .user(userRepository.findByEmail("johndoe@example.com").orElseThrow())
                .offer(selectedOffer)
                .bookedOffer(BookedOffer.from(selectedOffer))
                .orderDate(LocalDateTime.now())
                .visitDate(LocalDateTime.now().plusDays(30))
                .orderStatus(OrderStatus.NOWE)
                .build();

        Payment payment = Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .stripePaymentIntentId(PAYMENT_INTENT_ID)
                .amount(PAYMENT_AMOUNT)
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        payment.setOrder(order);
        order.setPayment(payment);

        Order savedOrder = orderRepository.saveAndFlush(order);

        return savedOrder.getPayment();
    }

    private void stubSuccessfulPaymentIntent() {
        stripeTestServer.stubFor(
                get(urlEqualTo("/v1/payment_intents/" + PAYMENT_INTENT_ID))
                        .willReturn(okJson("""
                                {
                                  "id": "pi_refund_test",
                                  "status": "succeeded",
                                  "amount_received": 8000,
                                  "currency": "pln"
                                }
                                """)));
    }

    private void stubRefundResponse(String refundStatus) {
        stripeTestServer.stubFor(
                post(urlEqualTo("/v1/refunds"))
                        .willReturn(okJson("""
                                {
                                  "id": "re_test_123",
                                  "status": "%s",
                                  "amount": 8000,
                                  "currency": "pln",
                                  "payment_intent": "pi_refund_test"
                                }
                                """.formatted(refundStatus))));
    }

    private PaymentRefund getRefund(Long paymentId) {
        List<PaymentRefund> refunds = paymentRefundRepository
                .findAllByPayment_IdPaymentOrderByRequestedAtDesc(paymentId);

        assertEquals(1, refunds.size());

        return refunds.getFirst();
    }

    private void verifyRefundRequestCount(int expectedCount) {
        stripeTestServer.verify(
                expectedCount,
                postRequestedFor(urlEqualTo("/v1/refunds"))
                        .withHeader("Idempotency-Key", matching(".+"))
                        .withHeader(
                                "Authorization",
                                equalTo("Bearer sk_test_wiremock"))
                        .withRequestBody(containing("payment_intent=pi_refund_test"))
                        .withRequestBody(containing("amount=8000")));
    }
}
