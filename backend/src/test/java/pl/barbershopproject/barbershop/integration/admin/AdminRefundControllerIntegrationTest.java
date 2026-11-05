
package pl.barbershopproject.barbershop.integration.admin;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.GuestOrderRepository;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefundRepository;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Verifies administrator-only refund lookup using persisted refund records
 */
class AdminRefundControllerIntegrationTest extends BaseIntegrationTest {

    private static final Instant REQUESTED_AT = Instant.parse("2026-11-04T12:30:00Z");

    private static final Instant COMPLETED_AT = Instant.parse("2026-11-04T12:31:00Z");

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private PaymentRefundRepository paymentRefundRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private GuestOrderRepository guestOrderRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(context)
                .apply(springSecurity())
                .build();
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnSuccessfulRefundDetails() throws Exception {
        PaymentRefund refund = createRegisteredRefund(RefundStatus.SUCCEEDED);

        Long refundId = refund.getIdPaymentRefund();
        Long paymentId = refund.getPayment().getIdPayment();

        mockMvc.perform(get("/admin/refunds/{refundId}", refundId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundId").value(refundId))
                .andExpect(jsonPath("$.paymentId").value(paymentId))
                .andExpect(jsonPath("$.stripeRefundId")
                        .value("re_admin_lookup_test"))
                .andExpect(jsonPath("$.status").value("SUCCEEDED"))
                .andExpect(jsonPath("$.amount").value(80.00))
                .andExpect(jsonPath("$.currency").value("PLN"))
                .andExpect(jsonPath("$.requestedAt")
                        .value("2026-11-04T12:30:00Z"))
                .andExpect(jsonPath("$.completedAt")
                        .value("2026-11-04T12:31:00Z"));

        PaymentRefund storedRefund = paymentRefundRepository
                .findById(refundId)
                .orElseThrow();

        assertEquals(RefundStatus.SUCCEEDED, storedRefund.getRefundStatus());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnPendingRefundWithoutCompletionDate() throws Exception {

        PaymentRefund refund = createRegisteredRefund(RefundStatus.PENDING);

        mockMvc.perform(get(
                        "/admin/refunds/{refundId}",
                        refund.getIdPaymentRefund()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.stripeRefundId").doesNotExist())
                .andExpect(jsonPath("$.completedAt").doesNotExist());
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnRefundRequiringReconciliation() throws Exception {

        PaymentRefund refund = createRegisteredRefund(RefundStatus.RECONCILIATION_REQUIRED);

        mockMvc.perform(get(
                        "/admin/refunds/{refundId}",
                        refund.getIdPaymentRefund()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status")
                        .value("RECONCILIATION_REQUIRED"))
                .andExpect(jsonPath("$.failureReason")
                        .value("Stripe refund outcome requires reconciliation"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnRefundBelongingToGuestOrder() throws Exception {
        PaymentRefund refund = createGuestRefund();

        mockMvc.perform(get("/admin/refunds/{refundId}",
                        refund.getIdPaymentRefund()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.refundId")
                        .value(refund.getIdPaymentRefund()))
                .andExpect(jsonPath("$.paymentId")
                        .value(refund.getPayment().getIdPayment()))
                .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnNotFoundWhenRefundDoesNotExist() throws Exception {
        mockMvc.perform(get("/admin/refunds/{refundId}", 999999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail")
                        .value("Zwrot o ID: 999999 nie istnieje"));
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldRejectRefundLookupByRegularUser() throws Exception {
        mockMvc.perform(get("/admin/refunds/{refundId}", 999999L))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRefundLookupByUnauthenticatedUser() throws Exception {

        mockMvc.perform(get("/admin/refunds/{refundId}", 999999L))
                .andExpect(status().isUnauthorized());
    }


    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldReturnPaginatedRefundHistoryWithSummary() throws Exception {
        createRegisteredRefund(RefundStatus.SUCCEEDED);
        createGuestRefund();

        PaymentRefund failedRefund = createRegisteredRefund(RefundStatus.FAILED);

        mockMvc.perform(get("/admin/refunds")
                        .param("page", "0")
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(2))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.summary.completed").value(1))
                .andExpect(jsonPath("$.summary.inProgress").value(1))
                .andExpect(jsonPath("$.summary.requiresAttention").value(1))
                .andExpect(jsonPath("$.content[0].refundId")
                        .value(failedRefund.getIdPaymentRefund()))
                .andExpect(jsonPath("$.content[0].reservationType")
                        .value("REGISTERED"))
                .andExpect(jsonPath("$.content[0].status")
                        .value("FAILED"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldFilterRefundHistoryByStatus() throws Exception {
        PaymentRefund successfulRefund = createRegisteredRefund(RefundStatus.SUCCEEDED);

        createGuestRefund();

        mockMvc.perform(get("/admin/refunds")
                        .param("status", "SUCCEEDED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].refundId")
                        .value(successfulRefund.getIdPaymentRefund()))
                .andExpect(jsonPath("$.content[0].status")
                        .value("SUCCEEDED"))
                .andExpect(jsonPath("$.summary.completed").value(1))
                .andExpect(jsonPath("$.summary.inProgress").value(1));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldFilterRefundHistoryByGuestReservationId() throws Exception {

        PaymentRefund guestRefund = createGuestRefund();

        Long guestOrderId = guestRefund.getPayment()
                .getGuestOrder()
                .getIdGuestOrder();

        mockMvc.perform(get("/admin/refunds")
                        .param("reservationId", guestOrderId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].refundId")
                        .value(guestRefund.getIdPaymentRefund()))
                .andExpect(jsonPath("$.content[0].reservationId")
                        .value(guestOrderId))
                .andExpect(jsonPath("$.content[0].reservationType")
                        .value("GUEST"));
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void shouldRejectInvalidRefundHistoryPagination() throws Exception {
        mockMvc.perform(get("/admin/refunds")
                        .param("page", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/admin/refunds")
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "USER")
    void shouldRejectRefundHistoryLookupByRegularUser() throws Exception {

        mockMvc.perform(get("/admin/refunds")).andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectUnauthenticatedRefundHistoryLookup() throws Exception {

        mockMvc.perform(get("/admin/refunds")).andExpect(status().isUnauthorized());
    }


    private PaymentRefund createRegisteredRefund(RefundStatus status) {
        Offer offer = getRequiredOffer();

        Order order = Order.builder()
                .user(userRepository
                        .findByEmail("johndoe@example.com")
                        .orElseThrow())
                .offer(offer)
                .bookedOffer(BookedOffer.from(offer))
                .orderDate(LocalDateTime.now())
                .visitDate(LocalDateTime.now().plusDays(30))
                .orderStatus(OrderStatus.ANULOWANE)
                .build();

        Payment payment = createPayment(status);

        payment.setOrder(order);
        order.setPayment(payment);

        Order savedOrder = orderRepository.saveAndFlush(order);

        return persistRefund(savedOrder.getPayment(), status);
    }

    private PaymentRefund createGuestRefund() {
        Offer offer = getRequiredOffer();

        GuestOrder guestOrder = GuestOrder.builder()
                .firstname("Refund")
                .lastname("Guest")
                .phonenumber("500600700")
                .email("refund-lookup@example.test")
                .offer(offer)
                .bookedOffer(BookedOffer.from(offer))
                .orderDate(LocalDateTime.now())
                .visitDate(LocalDateTime.now().plusDays(30))
                .orderStatus(OrderStatus.ANULOWANE)
                .build();

        Payment payment = createPayment(RefundStatus.REQUESTED);

        payment.setGuestOrder(guestOrder);
        guestOrder.setPayment(payment);

        GuestOrder savedGuestOrder = guestOrderRepository.saveAndFlush(guestOrder);

        return persistRefund(savedGuestOrder.getPayment(), RefundStatus.REQUESTED);
    }

    private Payment createPayment(RefundStatus refundStatus) {
        return Payment.builder()
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(
                        refundStatus == RefundStatus.SUCCEEDED
                                ? PaymentStatus.ZWROCONA
                                : PaymentStatus.OPLACONA)
                .stripePaymentIntentId("pi_admin_refund_lookup")
                .amount(new BigDecimal("80.00"))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .paidAt(LocalDateTime.now())
                .build();
    }

    private PaymentRefund persistRefund(
            Payment payment,
            RefundStatus refundStatus
    ) {
        boolean succeeded = refundStatus == RefundStatus.SUCCEEDED;

        PaymentRefund refund = PaymentRefund.builder()
                .payment(payment)
                .idempotencyKey(UUID.randomUUID().toString())
                .stripeRefundId(succeeded ? "re_admin_lookup_test" : null)
                .amount(new BigDecimal("80.00"))
                .currency("PLN")
                .refundStatus(refundStatus)
                .requestedAt(REQUESTED_AT)
                .completedAt(succeeded ? COMPLETED_AT : null)
                .failureReason(
                        refundStatus == RefundStatus.RECONCILIATION_REQUIRED
                                ? "Stripe refund outcome requires reconciliation"
                                : null)
                .build();

        return paymentRefundRepository.saveAndFlush(refund);
    }

    private Offer getRequiredOffer() {
        return offerRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();
    }
}
