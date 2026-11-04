package pl.barbershopproject.barbershop.integration.payment.refund;

import com.github.tomakehurst.wiremock.WireMockServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
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
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

class RefundReconciliationServiceIntegrationTest extends BaseIntegrationTest {

    private static final String PAYMENT_INTENT_ID = "pi_reconciliation_test";
    private static final String STRIPE_REFUND_ID = "re_reconciliation_test";

    private static final WireMockServer stripeServer = new WireMockServer(options().dynamicPort());

    static {stripeServer.start();}

    @DynamicPropertySource
    static void configureStripeProperties(DynamicPropertyRegistry registry) {
        registry.add("stripe.api-base-url", stripeServer::baseUrl);
        registry.add("stripe.secret-key", () -> "sk_test_reconciliation");
        registry.add("application.refunds.reconciliation.enabled", () -> "false");
    }

    @Autowired
    private RefundReconciliationService refundReconciliationService;

    @Autowired
    private RefundTransactionService refundTransactionService;

    @Autowired
    private StripeRefundStatusSynchronizer refundStatusSynchronizer;

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
    void resetStripeStubs() {stripeServer.resetAll();}

    @AfterAll
    static void stopStripeServer() {stripeServer.stop();}

    @Test
    void shouldRecoverSuccessfulRefundWhenStripeRefundIdIsMissing() {
        RefundTestCase refundTestCase = createReservedRefund();

        stubRefundList(refundTestCase.refundId(), "succeeded");

        RefundResult result = refundReconciliationService
                .reconcileRefund(refundTestCase.refundId());

        assertEquals(RefundStatus.SUCCEEDED, result.refundStatus());
        assertEquals(STRIPE_REFUND_ID, result.stripeRefundId());

        PaymentRefund storedRefund = paymentRefundRepository
                .findById(refundTestCase.refundId())
                .orElseThrow();

        assertNotNull(storedRefund.getCompletedAt());

        Payment updatedPayment = paymentRepository
                .findById(refundTestCase.paymentId())
                .orElseThrow();

        assertEquals(PaymentStatus.ZWROCONA, updatedPayment.getPaymentStatus());

        verifyNoRefundCreation();
    }

    @Test
    void shouldRetrieveKnownRefundAndUpdateItsStatus() {
        RefundTestCase refundTestCase = createReservedRefund();

        refundStatusSynchronizer.synchronizeRefund(
                refundTestCase.refundId(),
                createStripeRefundResponse(
                        refundTestCase.refundId(),
                        "pending"));

        stripeServer.stubFor(
                get(urlEqualTo("/v1/refunds/" + STRIPE_REFUND_ID))
                        .willReturn(okJson(
                                createStripeRefundJson(
                                        refundTestCase.refundId(),
                                        "succeeded"))));

        RefundResult result = refundReconciliationService
                .reconcileRefund(refundTestCase.refundId());

        assertEquals(RefundStatus.SUCCEEDED, result.refundStatus());

        stripeServer.verify(1,
                getRequestedFor(urlEqualTo("/v1/refunds/" + STRIPE_REFUND_ID)));

        verifyNoRefundCreation();
    }

    @Test
    void shouldRequireReconciliationWhenRefundIsNotFound() {
        RefundTestCase refundTestCase = createReservedRefund();

        stripeServer.stubFor(
                get(urlPathEqualTo("/v1/refunds"))
                        .willReturn(okJson("""
                                {
                                  "data": [],
                                  "has_more": false
                                }
                                """)));

        RefundResult result = refundReconciliationService
                .reconcileRefund(refundTestCase.refundId());

        assertEquals(RefundStatus.RECONCILIATION_REQUIRED, result.refundStatus());

        assertNull(result.stripeRefundId());

        Payment payment = paymentRepository
                .findById(refundTestCase.paymentId())
                .orElseThrow();

        assertEquals(PaymentStatus.OPLACONA, payment.getPaymentStatus());

        verifyNoRefundCreation();
    }

    @Test
    void shouldFindMatchingRefundOnNextStripePage() {
        RefundTestCase refundTestCase = createReservedRefund();

        stripeServer.stubFor(
                get(urlPathEqualTo("/v1/refunds"))
                        .withQueryParam("starting_after", absent())
                        .willReturn(okJson("""
                                {
                                  "data": [
                                    {
                                      "id": "re_unrelated",
                                      "status": "succeeded",
                                      "amount": 8000,
                                      "currency": "pln",
                                      "payment_intent": "pi_reconciliation_test",
                                      "metadata": {}
                                    }
                                  ],
                                  "has_more": true
                                }
                                """)));

        stripeServer.stubFor(
                get(urlPathEqualTo("/v1/refunds"))
                        .withQueryParam(
                                "starting_after",
                                equalTo("re_unrelated")
                        )
                        .willReturn(okJson("""
                                {
                                  "data": [%s],
                                  "has_more": false
                                }
                                """.formatted(
                                createStripeRefundJson(
                                        refundTestCase.refundId(),
                                        "succeeded")))));

        RefundResult result = refundReconciliationService
                .reconcileRefund(refundTestCase.refundId());

        assertEquals(RefundStatus.SUCCEEDED, result.refundStatus());

        stripeServer.verify(2, getRequestedFor(urlPathEqualTo("/v1/refunds")));

        verifyNoRefundCreation();
    }

    @Test
    void shouldKeepRefundUnresolvedWhenStripeReturnsServerError() {
        RefundTestCase refundTestCase = createReservedRefund();

        stripeServer.stubFor(
                get(urlPathEqualTo("/v1/refunds"))
                        .willReturn(
                                aResponse()
                                        .withStatus(500)
                                        .withHeader(
                                                "Content-Type",
                                                "application/json"
                                        ).withBody("{}")));

        assertThrows(IllegalStateException.class,
                () -> refundReconciliationService.reconcileRefund(
                        refundTestCase.refundId()));

        PaymentRefund storedRefund = paymentRefundRepository
                .findById(refundTestCase.refundId())
                .orElseThrow();

        assertEquals(RefundStatus.RECONCILIATION_REQUIRED, storedRefund.getRefundStatus());

        verifyNoRefundCreation();
    }

    @Test
    void shouldSkipAlreadyCompletedRefund() {
        RefundTestCase refundTestCase = createReservedRefund();

        refundStatusSynchronizer.synchronizeRefund(
                refundTestCase.refundId(),
                createStripeRefundResponse(
                        refundTestCase.refundId(),
                        "succeeded"));

        RefundResult result = refundReconciliationService
                .reconcileRefund(refundTestCase.refundId());

        assertEquals(RefundStatus.SUCCEEDED, result.refundStatus());

        stripeServer.verify(0, getRequestedFor(urlMatching("/v1/refunds.*")));

        verifyNoRefundCreation();
    }

    private RefundTestCase createReservedRefund() {
        Offer selectedOffer = offerRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();

        Order order = Order.builder()
                .user(userRepository
                        .findByEmail("johndoe@example.com")
                        .orElseThrow())
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
                .amount(new BigDecimal("80.00"))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        payment.setOrder(order);
        order.setPayment(payment);

        Order savedOrder = orderRepository.saveAndFlush(order);
        Long paymentId = savedOrder.getPayment().getIdPayment();

        RefundTransactionService.RefundReservation refundReservation = refundTransactionService
                .reserveFullRefund(paymentId);

        return new RefundTestCase(
                paymentId,
                refundReservation.refundId());
    }

    private void stubRefundList(Long refundId, String refundStatus) {
        stripeServer.stubFor(
                get(urlPathEqualTo("/v1/refunds"))
                        .withQueryParam(
                                "payment_intent",
                                equalTo(PAYMENT_INTENT_ID)
                        )
                        .willReturn(okJson("""
                                {
                                  "data": [%s],
                                  "has_more": false
                                }
                                """.formatted(
                                createStripeRefundJson(
                                        refundId,
                                        refundStatus
                                )))));
    }

    private String createStripeRefundJson(
            Long refundId,
            String refundStatus
    ) {
        return """
                {
                  "id": "%s",
                  "status": "%s",
                  "amount": 8000,
                  "currency": "pln",
                  "payment_intent": "%s",
                  "metadata": {
                    "payment_refund_id": "%d"
                  }
                }
                """.formatted(
                STRIPE_REFUND_ID,
                refundStatus,
                PAYMENT_INTENT_ID,
                refundId);
    }

    private StripeRefundClient.StripeRefundResponse createStripeRefundResponse(
            Long refundId,
            String refundStatus
    ) {
        return new StripeRefundClient.StripeRefundResponse(
                STRIPE_REFUND_ID,
                refundStatus,
                8000L,
                "pln",
                PAYMENT_INTENT_ID,
                null,
                Map.of("payment_refund_id", refundId.toString()));
    }

    private void verifyNoRefundCreation() {
        stripeServer.verify(0, postRequestedFor(urlEqualTo("/v1/refunds")));
    }

    private record RefundTestCase(Long paymentId, Long refundId) { }
}
