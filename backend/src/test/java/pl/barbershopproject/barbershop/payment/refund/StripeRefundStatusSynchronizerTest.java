
package pl.barbershopproject.barbershop.payment.refund;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.payment.refund.event.RefundSucceededEvent;
import pl.barbershopproject.barbershop.user.User;

import java.math.BigDecimal;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;

@ExtendWith(MockitoExtension.class)
class StripeRefundStatusSynchronizerTest {

    private static final Long REFUND_ID = 9L;
    private static final String PAYMENT_INTENT_ID = "pi_refund_notification_test";

    @Mock
    private PaymentRefundRepository paymentRefundRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StripeRefundStatusSynchronizer synchronizer;

    @BeforeEach
    void setUp() {
        synchronizer = new StripeRefundStatusSynchronizer(paymentRefundRepository, eventPublisher);
    }

    @Test
    void shouldPublishSuccessfulRefundEventForRegisteredCustomer() {
        PaymentRefund refund = createRegisteredRefund(RefundStatus.PENDING);

        mockRefundLookup(refund);

        RefundResult result = synchronizer.synchronizeRefund(REFUND_ID, stripeRefundResponse("succeeded"));

        ArgumentCaptor<RefundSucceededEvent> eventCaptor = ArgumentCaptor.forClass(RefundSucceededEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        RefundSucceededEvent event = eventCaptor.getValue();

        assertThat(result.refundStatus()).isEqualTo(RefundStatus.SUCCEEDED);

        assertThat(refund.getPayment().getPaymentStatus()).isEqualTo(PaymentStatus.ZWROCONA);

        assertThat(event.refundId()).isEqualTo(REFUND_ID);
        assertThat(event.reservationId()).isEqualTo(18L);
        assertThat(event.reservationType())
                .isEqualTo(ReservationType.REGISTERED);

        assertThat(event.customerEmail())
                .isEqualTo("customer@example.com");

        assertThat(event.customerFirstname()).isEqualTo("Jan");

        assertThat(event.amount()).isEqualByComparingTo("65.00");

        assertThat(event.currency()).isEqualTo("PLN");
    }

    @Test
    void shouldPublishSuccessfulRefundEventForGuestCustomer() {
        PaymentRefund refund = createGuestRefund(RefundStatus.RECONCILIATION_REQUIRED);

        mockRefundLookup(refund);

        synchronizer.synchronizeRefund(REFUND_ID,
                stripeRefundResponse("succeeded"));

        ArgumentCaptor<RefundSucceededEvent> eventCaptor = ArgumentCaptor.forClass(RefundSucceededEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        RefundSucceededEvent event = eventCaptor.getValue();

        assertThat(event.reservationId()).isEqualTo(42L);

        assertThat(event.reservationType())
                .isEqualTo(ReservationType.GUEST);

        assertThat(event.customerEmail())
                .isEqualTo("guest@example.com");

        assertThat(event.customerFirstname())
                .isEqualTo("Anna");
    }

    @Test
    void shouldNotPublishDuplicateEventForRepeatedSuccessStatus() {
        PaymentRefund refund = createRegisteredRefund(RefundStatus.PENDING);
        mockRefundLookup(refund);

        synchronizer.synchronizeRefund(
                REFUND_ID,
                stripeRefundResponse("succeeded"));

        synchronizer.synchronizeRefund(
                REFUND_ID,
                stripeRefundResponse("succeeded"));

        verify(eventPublisher, times(1)).publishEvent(
                any(RefundSucceededEvent.class));
    }

    @Test
    void shouldNotPublishEventForPendingRefund() {
        PaymentRefund refund = createRegisteredRefund(RefundStatus.REQUESTED);

        mockRefundLookup(refund);

        synchronizer.synchronizeRefund(
                REFUND_ID,
                stripeRefundResponse("pending"));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.PENDING);

        verify(eventPublisher, never()).publishEvent(any(RefundSucceededEvent.class));
    }

    @Test
    void shouldNotPublishEventForFailedRefund() {
        PaymentRefund refund = createRegisteredRefund(RefundStatus.PENDING);

        mockRefundLookup(refund);

        synchronizer.synchronizeRefund(REFUND_ID, stripeRefundResponse("failed"));

        assertThat(refund.getRefundStatus()).isEqualTo(RefundStatus.FAILED);

        verify(eventPublisher, never()).publishEvent(any(RefundSucceededEvent.class));
    }

    @Test
    void shouldNotPublishEventWhenCustomerEmailIsMissing() {
        PaymentRefund refund = createGuestRefund(RefundStatus.PENDING);

        refund.getPayment()
                .getGuestOrder()
                .setEmail(null);

        mockRefundLookup(refund);

        synchronizer.synchronizeRefund(REFUND_ID, stripeRefundResponse("succeeded"));

        assertThat(refund.getRefundStatus())
                .isEqualTo(RefundStatus.SUCCEEDED);

        assertThat(refund.getPayment().getPaymentStatus())
                .isEqualTo(PaymentStatus.ZWROCONA);

        verify(eventPublisher, never()).publishEvent(any(RefundSucceededEvent.class));
    }

    @Test
    void shouldKeepSuccessfulRefundWhenEventPublishingFails() {
        PaymentRefund refund = createRegisteredRefund(RefundStatus.PENDING);

        mockRefundLookup(refund);

        doThrow(new IllegalStateException("Event publishing failed"))
                .when(eventPublisher)
                .publishEvent(any(RefundSucceededEvent.class));

        RefundResult result = assertDoesNotThrow(
                () -> synchronizer.synchronizeRefund(
                        REFUND_ID,
                        stripeRefundResponse("succeeded")));

        assertThat(result.refundStatus())
                .isEqualTo(RefundStatus.SUCCEEDED);

        assertThat(refund.getPayment().getPaymentStatus())
                .isEqualTo(PaymentStatus.ZWROCONA);
    }

    /**
     * Creates a registered customer refund for synchronization tests.
     */
    private PaymentRefund createRegisteredRefund(
            RefundStatus initialStatus
    ) {
        User customer = User.builder()
                .idUser(5L)
                .firstname("Jan")
                .email("customer@example.com")
                .build();

        Order order = Order.builder()
                .idOrder(18L)
                .user(customer)
                .build();

        Payment payment = Payment.builder()
                .order(order)
                .stripePaymentIntentId(PAYMENT_INTENT_ID)
                .paymentStatus(PaymentStatus.OPLACONA)
                .build();

        return createRefund(payment, initialStatus);
    }

    /**
     * Creates a guest customer refund for synchronization tests.
     */
    private PaymentRefund createGuestRefund(
            RefundStatus initialStatus
    ) {
        GuestOrder guestOrder = GuestOrder.builder()
                .idGuestOrder(42L)
                .firstname("Anna")
                .email("guest@example.com")
                .build();

        Payment payment = Payment.builder()
                .guestOrder(guestOrder)
                .stripePaymentIntentId(PAYMENT_INTENT_ID)
                .paymentStatus(PaymentStatus.OPLACONA)
                .build();

        return createRefund(payment, initialStatus);
    }

    /**
     * Creates a refund using the supplied payment and status.
     */
    private PaymentRefund createRefund(
            Payment payment,
            RefundStatus initialStatus
    ) {
        return PaymentRefund.builder()
                .idPaymentRefund(REFUND_ID)
                .payment(payment)
                .stripeRefundId("re_refund_notification_test")
                .amount(new BigDecimal("65.00"))
                .currency("PLN")
                .refundStatus(initialStatus)
                .build();
    }

    /**
     * Configures the repository to return a selected refund.
     */
    private void mockRefundLookup(PaymentRefund refund) {
        when(paymentRefundRepository.findForUpdateByIdPaymentRefund(REFUND_ID))
                .thenReturn(Optional.of(refund));
    }

    /**
     * Creates a Stripe refund response with matching financial data.
     */
    private StripeRefundClient.StripeRefundResponse stripeRefundResponse(
            String status
    ) {
        return new StripeRefundClient.StripeRefundResponse(
                "re_refund_notification_test",
                status,
                6500L,
                "pln",
                PAYMENT_INTENT_ID,
                null,
                Map.of("payment_refund_id", REFUND_ID.toString()));
    }
}
