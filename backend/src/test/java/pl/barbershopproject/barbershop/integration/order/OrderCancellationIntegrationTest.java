package pl.barbershopproject.barbershop.integration.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.exception.OrderStatusChangeNotAllowedException;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.GuestOrderRepository;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.AdminCancellationResult;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderCancellationService;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefundRepository;
import pl.barbershopproject.barbershop.payment.refund.RefundResult;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;
import pl.barbershopproject.barbershop.payment.refund.StripeRefundService;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Stream;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

class OrderCancellationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private OrderCancellationService orderCancellationService;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private GuestOrderRepository guestOrderRepository;

    @Autowired
    private PaymentRefundRepository paymentRefundRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private UserRepository userRepository;

    @MockitoBean
    private StripeRefundService stripeRefundService;

    @Test
    void shouldCancelUnpaidCashOrderWithoutStripeRefund() {
        Order order = createRegisteredOrder(PaymentMethod.GOTOWKA, PaymentStatus.NIE_WYMAGANA);

        AdminCancellationResult result = orderCancellationService.cancelRegisteredOrder(
                        order.getIdOrder());

        Order cancelledOrder = orderRepository
                .findById(order.getIdOrder())
                .orElseThrow();

        assertEquals(OrderStatus.ANULOWANE, result.orderStatus());
        assertEquals(OrderStatus.ANULOWANE, cancelledOrder.getOrderStatus());

        assertNull(result.refundResult());
        assertTrue(paymentRefundRepository.findAll().isEmpty());

        assertNotNull(cancelledOrder.getPayment());
        verifyNoInteractions(stripeRefundService);
    }

    @Test
    void shouldCancelPaidOrderAndReserveFullStripeRefund() {
        Order order = createRegisteredOrder(PaymentMethod.KARTA_ONLINE, PaymentStatus.OPLACONA);

        stubPendingRefund();

        AdminCancellationResult result = orderCancellationService.cancelRegisteredOrder(
                        order.getIdOrder());

        Order cancelledOrder = orderRepository
                .findById(order.getIdOrder())
                .orElseThrow();

        List<PaymentRefund> refunds = getRefundsForPayment(order.getPayment().getIdPayment());

        assertEquals(OrderStatus.ANULOWANE, cancelledOrder.getOrderStatus());
        assertEquals(RefundStatus.PENDING, result.refundResult().refundStatus());

        assertEquals(1, refunds.size());

        PaymentRefund savedRefund = refunds.getFirst();

        assertEquals(
                0,
                order.getPayment().getAmount()
                        .compareTo(savedRefund.getAmount()));

        assertEquals(PaymentStatus.OPLACONA,
                cancelledOrder.getPayment().getPaymentStatus());

        verify(stripeRefundService).submitReservedRefund(savedRefund.getIdPaymentRefund());
    }

    @Test
    void shouldCancelPaidGuestOrderAndReserveFullStripeRefund() {
        GuestOrder guestOrder = createGuestOrder(PaymentMethod.KARTA_ONLINE, PaymentStatus.OPLACONA);

        stubPendingRefund();

        AdminCancellationResult result = orderCancellationService.cancelGuestOrder(
                        guestOrder.getIdGuestOrder());

        GuestOrder cancelledGuestOrder = guestOrderRepository
                .findById(guestOrder.getIdGuestOrder())
                .orElseThrow();

        List<PaymentRefund> refunds = getRefundsForPayment(guestOrder.getPayment().getIdPayment());

        assertEquals(OrderStatus.ANULOWANE, cancelledGuestOrder.getOrderStatus());

        assertEquals(1, refunds.size());
        assertEquals(RefundStatus.PENDING, result.refundResult().refundStatus());

        verify(stripeRefundService).submitReservedRefund(refunds.getFirst().getIdPaymentRefund());
    }

    @Test
    void shouldRejectCancellationWhenOnlinePaymentIsPending() {
        Order order = createRegisteredOrder(PaymentMethod.KARTA_ONLINE, PaymentStatus.OCZEKUJE_NA_PLATNOSC);

        long orderId = order.getIdOrder();

        assertThrows(OrderStatusChangeNotAllowedException.class,
                () -> orderCancellationService.cancelRegisteredOrder(orderId));

        Order unchangedOrder = orderRepository
                .findById(order.getIdOrder())
                .orElseThrow();

        assertEquals(OrderStatus.NOWE, unchangedOrder.getOrderStatus());
        assertTrue(paymentRefundRepository.findAll().isEmpty());

        verifyNoInteractions(stripeRefundService);
    }

    @Test
    void shouldPreventDuplicateCancellationAndRefund() {
        Order order = createRegisteredOrder(PaymentMethod.KARTA_ONLINE, PaymentStatus.OPLACONA);

        stubPendingRefund();

        orderCancellationService.cancelRegisteredOrder(order.getIdOrder());

        long orderId = order.getIdOrder();
        assertThrows(OrderStatusChangeNotAllowedException.class,
                () -> orderCancellationService.cancelRegisteredOrder(orderId));

        List<PaymentRefund> refunds = getRefundsForPayment(order.getPayment().getIdPayment());

        assertEquals(1, refunds.size());

        verify(stripeRefundService, times(1))
                .submitReservedRefund(anyLong());
    }

    @Test
    void shouldPreserveCancellationWhenStripeSubmissionFails() {
        Order order = createRegisteredOrder(PaymentMethod.KARTA_ONLINE, PaymentStatus.OPLACONA);

        when(stripeRefundService.submitReservedRefund(anyLong()))
                .thenThrow(new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Stripe is unavailable"));

        long orderId = order.getIdOrder();

        assertThrows(ResponseStatusException.class,
                () -> orderCancellationService.cancelRegisteredOrder(
                        orderId));

        Order cancelledOrder = orderRepository
                .findById(orderId)
                .orElseThrow();

        List<PaymentRefund> refunds = getRefundsForPayment(order.getPayment().getIdPayment());

        assertEquals(OrderStatus.ANULOWANE, cancelledOrder.getOrderStatus());
        assertEquals(1, refunds.size());

        // StripeRefundService is mocked, so submission status is unchanged.
        assertEquals(RefundStatus.REQUESTED, refunds.getFirst().getRefundStatus());
    }

    @Test
    @Timeout(30)
    void shouldAllowOnlyOneCancellationWhenRequestsAreConcurrent() throws Exception {

        Order order = createRegisteredOrder(PaymentMethod.KARTA_ONLINE, PaymentStatus.OPLACONA);

        stubPendingRefund();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        CountDownLatch workersReady = new CountDownLatch(2);
        CountDownLatch startCancellation = new CountDownLatch(1);

        Callable<Object> cancellationTask = () -> {
            workersReady.countDown();

            if (!startCancellation.await(10, SECONDS)) {
                throw new IllegalStateException("Timed out waiting to start cancellation");
            }

            try {
                return orderCancellationService.cancelRegisteredOrder(order.getIdOrder());
            } catch (OrderStatusChangeNotAllowedException exception) {
                return exception;
            }
        };

        try {
            Future<Object> firstRequest = executor.submit(cancellationTask);
            Future<Object> secondRequest = executor.submit(cancellationTask);

            assertTrue(workersReady.await(10, SECONDS));

            startCancellation.countDown();

            Object firstResult = firstRequest.get(20, SECONDS);
            Object secondResult = secondRequest.get(20, SECONDS);

            long successfulCancellations = Stream.of(firstResult, secondResult)
                            .filter(AdminCancellationResult.class::isInstance)
                            .count();

            long rejectedCancellations = Stream.of(firstResult, secondResult)
                            .filter(OrderStatusChangeNotAllowedException.class::isInstance)
                            .count();

            assertEquals(1L, successfulCancellations);
            assertEquals(1L, rejectedCancellations);

            assertEquals(
                    1,
                    getRefundsForPayment(order.getPayment().getIdPayment()).size());

            verify(stripeRefundService, times(1))
                    .submitReservedRefund(anyLong());

        } finally {
            startCancellation.countDown();
            executor.shutdownNow();
            executor.awaitTermination(5, SECONDS);
        }
    }

    private void stubPendingRefund() {
        when(stripeRefundService.submitReservedRefund(anyLong()))
                .thenAnswer(invocation -> new RefundResult(
                        invocation.getArgument(0, Long.class),
                        "re_test_cancellation",
                        RefundStatus.PENDING));
    }

    private Order createRegisteredOrder(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    ) {
        Offer selectedOffer = getAvailableOffer();

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

        Payment payment = createPayment(paymentMethod, paymentStatus);

        payment.setOrder(order);
        order.setPayment(payment);

        return orderRepository.saveAndFlush(order);
    }

    private GuestOrder createGuestOrder(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    ) {
        Offer selectedOffer = getAvailableOffer();

        GuestOrder guestOrder = GuestOrder.builder()
                .firstname("Refund")
                .lastname("Integration")
                .phonenumber("500600700")
                .email("refund-guest@example.test")
                .offer(selectedOffer)
                .bookedOffer(BookedOffer.from(selectedOffer))
                .orderDate(LocalDateTime.now())
                .visitDate(LocalDateTime.now().plusDays(30))
                .orderStatus(OrderStatus.NOWE)
                .build();

        Payment payment = createPayment(paymentMethod, paymentStatus);

        payment.setGuestOrder(guestOrder);
        guestOrder.setPayment(payment);

        return guestOrderRepository.saveAndFlush(guestOrder);
    }

    private Payment createPayment(
            PaymentMethod paymentMethod,
            PaymentStatus paymentStatus
    ) {
        return Payment.builder()
                .paymentMethod(paymentMethod)
                .paymentStatus(paymentStatus)
                .stripePaymentIntentId(paymentMethod == PaymentMethod.KARTA_ONLINE
                                ? "pi_cancellation_test"
                                : null)
                .amount(new BigDecimal("80.00"))
                .currency("PLN")
                .createdAt(LocalDateTime.now())
                .paidAt(
                        paymentStatus == PaymentStatus.OPLACONA
                                ? LocalDateTime.now()
                                : null)
                .build();
    }

    private Offer getAvailableOffer() {
        return offerRepository.findAll()
                .stream()
                .findFirst()
                .orElseThrow();
    }

    private List<PaymentRefund> getRefundsForPayment(Long paymentId) {
        return paymentRefundRepository
                .findAllByPayment_IdPaymentOrderByRequestedAtDesc(paymentId);
    }
}
