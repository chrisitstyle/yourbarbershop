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
import pl.barbershopproject.barbershop.payment.stripe.webhook.StripeWebhookController;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.HexFormat;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static org.junit.jupiter.api.Assertions.*;

class StripeRefundWebhookIntegrationTest extends BaseIntegrationTest {

    private static final String WEBHOOK_SECRET = "whsec_test_refunds";
    private static final String STRIPE_REFUND_ID = "re_test_webhook";
    private static final String PAYMENT_INTENT_ID = "pi_refund_webhook";

    private static final WireMockServer stripeServer =
            new WireMockServer(options().dynamicPort());

    static {
        stripeServer.start();
    }

    @DynamicPropertySource
    static void configureStripe(DynamicPropertyRegistry registry) {
        registry.add("stripe.api-base-url", stripeServer::baseUrl);
        registry.add("stripe.secret-key", () -> "sk_test_webhook");
        registry.add("stripe.webhook-secret", () -> WEBHOOK_SECRET);
    }

    @Autowired
    private StripeWebhookController stripeWebhookController;

    @Autowired
    private RefundTransactionService refundTransactionService;

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

    @Autowired
    private RefundReconciliationService refundReconciliationService;

    @BeforeEach
    void resetStripeServer() {
        stripeServer.resetAll();
    }

    @AfterAll
    static void stopStripeServer() {
        stripeServer.stop();
    }

    @Test
    void shouldSynchronizeSuccessfulRefundFromWebhook() throws Exception {
        Payment payment = createPaidOnlinePayment();

        var refundReservation = refundTransactionService
                .reserveFullRefund(payment.getIdPayment());

        stubStripeRefund(refundReservation.refundId(), "succeeded", 8000);

        sendSignedWebhook("refund.updated");

        PaymentRefund savedRefund = getRefund(refundReservation.refundId());

        assertEquals(RefundStatus.SUCCEEDED, savedRefund.getRefundStatus());
        assertEquals(STRIPE_REFUND_ID, savedRefund.getStripeRefundId());
        assertNotNull(savedRefund.getCompletedAt());

        Payment updatedPayment = paymentRepository
                .findById(payment.getIdPayment())
                .orElseThrow();

        assertEquals(PaymentStatus.ZWROCONA, updatedPayment.getPaymentStatus());
    }

    @Test
    void shouldNotRevertSuccessfulRefundWhenDelayedWebhookArrives() throws Exception {

        Payment payment = createPaidOnlinePayment();

        var refundReservation = refundTransactionService
                .reserveFullRefund(payment.getIdPayment());

        stubStripeRefund(refundReservation.refundId(), "succeeded", 8000);

        sendSignedWebhook("refund.updated");

        Instant completedAt = getRefund(
                refundReservation.refundId()
        ).getCompletedAt();

        stripeServer.resetAll();

        stubStripeRefund(refundReservation.refundId(), "pending", 8000);

        sendSignedWebhook("refund.created");

        PaymentRefund savedRefund = getRefund(refundReservation.refundId());

        assertEquals(RefundStatus.SUCCEEDED, savedRefund.getRefundStatus());
        assertEquals(completedAt, savedRefund.getCompletedAt());
    }

    @Test
    void shouldKeepPaymentUnchangedWhenStripeRefundAmountDoesNotMatch() {

        Payment payment = createPaidOnlinePayment();

        var refundReservation = refundTransactionService
                .reserveFullRefund(payment.getIdPayment());

        stubStripeRefund(refundReservation.refundId(), "succeeded", 7000);

        assertThrows(
                IllegalStateException.class,
                () -> sendSignedWebhook("refund.updated"));

        PaymentRefund savedRefund = getRefund(refundReservation.refundId());

        assertEquals(RefundStatus.REQUESTED, savedRefund.getRefundStatus());

        Payment updatedPayment = paymentRepository
                .findById(payment.getIdPayment())
                .orElseThrow();

        assertEquals(PaymentStatus.OPLACONA, updatedPayment.getPaymentStatus());
    }

    @Test
    void shouldRejectWebhookWithInvalidSignature() {
        Payment payment = createPaidOnlinePayment();

        var refundReservation = refundTransactionService
                .reserveFullRefund(payment.getIdPayment());

        String payload = createWebhookPayload("refund.updated");

        assertThrows(
                IllegalArgumentException.class,
                () -> stripeWebhookController.handleStripeWebhook(
                        payload,
                        "t=123,v1=invalid"));

        PaymentRefund savedRefund = getRefund(refundReservation.refundId());

        assertEquals(RefundStatus.REQUESTED, savedRefund.getRefundStatus());

        stripeServer.verify(0, getRequestedFor(urlEqualTo("/v1/refunds/" + STRIPE_REFUND_ID)));
    }


    @Test
    void shouldRestorePaidStatusWhenInitiallySuccessfulRefundLaterFails() throws Exception {

        Payment payment = createPaidOnlinePayment();

        var refundReservation = refundTransactionService
                .reserveFullRefund(payment.getIdPayment());

        Long refundId = refundReservation.refundId();

        // Stripe initially reports a successful refund
        stubStripeRefund(refundId, "succeeded", 8000);

        sendSignedWebhook("refund.updated");

        assertEquals(RefundStatus.SUCCEEDED, getRefund(refundId).getRefundStatus());

        assertEquals(PaymentStatus.ZWROCONA,
                paymentRepository.findById(payment.getIdPayment())
                        .orElseThrow()
                        .getPaymentStatus());

        // Stripe later reports that the refund failed
        stripeServer.resetAll();
        stubStripeRefund(refundId, "failed", 8000);

        sendSignedWebhook("refund.failed");

        PaymentRefund refundRequiringReconciliation = getRefund(refundId);

        assertEquals(RefundStatus.RECONCILIATION_REQUIRED,
                refundRequiringReconciliation.getRefundStatus());

        // a fresh Stripe lookup confirms the final failure
        var reconciliationResult = refundReconciliationService.reconcileRefund(refundId);

        assertEquals(RefundStatus.FAILED, reconciliationResult.refundStatus());

        PaymentRefund failedRefund = getRefund(refundId);

        assertEquals(RefundStatus.FAILED, failedRefund.getRefundStatus());
        assertNotNull(failedRefund.getCompletedAt());

        Payment updatedPayment = paymentRepository
                .findById(payment.getIdPayment())
                .orElseThrow();

        assertEquals(PaymentStatus.OPLACONA, updatedPayment.getPaymentStatus());

        // reconciliation must never create another refund.
        stripeServer.verify(0,
                postRequestedFor(urlEqualTo("/v1/refunds")));
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
                .amount(new BigDecimal("80.00"))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();

        payment.setOrder(order);
        order.setPayment(payment);

        return orderRepository.saveAndFlush(order).getPayment();
    }

    private void stubStripeRefund(
            Long refundId,
            String refundStatus,
            long amountInMinorUnits
    ) {
        stripeServer.stubFor(
                get(urlEqualTo("/v1/refunds/" + STRIPE_REFUND_ID))
                        .willReturn(okJson("""
                                {
                                  "id": "%s",
                                  "status": "%s",
                                  "amount": %d,
                                  "currency": "pln",
                                  "payment_intent": "%s",
                                  "metadata": {
                                    "payment_refund_id": "%d"
                                  }
                                }
                                """.formatted(
                                STRIPE_REFUND_ID,
                                refundStatus,
                                amountInMinorUnits,
                                PAYMENT_INTENT_ID,
                                refundId
                        ))));
    }

    private void sendSignedWebhook(String eventType) throws Exception {
        String payload = createWebhookPayload(eventType);

        stripeWebhookController.handleStripeWebhook(payload, createStripeSignature(payload));
    }

    private String createWebhookPayload(String eventType) {
        return """
                {
                  "id": "evt_refund_webhook",
                  "type": "%s",
                  "data": {
                    "object": {
                      "id": "%s"
                    }
                  }
                }
                """.formatted(eventType, STRIPE_REFUND_ID);
    }

    private String createStripeSignature(String payload) throws Exception {
        long timestamp = Instant.now().getEpochSecond();

        Mac signatureCalculator = Mac.getInstance("HmacSHA256");

        signatureCalculator.init(new SecretKeySpec(
                WEBHOOK_SECRET.getBytes(StandardCharsets.UTF_8),
                "HmacSHA256"));

        byte[] signature = signatureCalculator.doFinal(
                (timestamp + "." + payload).getBytes(StandardCharsets.UTF_8));

        return "t=" + timestamp
                + ",v1=" + HexFormat.of().formatHex(signature);
    }

    private PaymentRefund getRefund(Long refundId) {
        return paymentRefundRepository.findById(refundId).orElseThrow();
    }
}
