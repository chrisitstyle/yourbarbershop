package pl.barbershopproject.barbershop.payment.stripe.webhook;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentRepository;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StripeWebhookServiceTest {

    private static final Long PAYMENT_ID = 15L;
    private static final String PAYMENT_INTENT_ID = "pi_test_failed_123";

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private StripeWebhookService stripeWebhookService;

    @BeforeEach
    void setUp() {
        stripeWebhookService = new StripeWebhookService(
                new ObjectMapper(),
                paymentRepository,
                appointmentAvailabilityService,
                eventPublisher,
                Clock.systemUTC()
        );
    }

    @Test
    void shouldKeepPaymentPending_WhenPaymentIntentFails() {
        // given
        Payment payment = Payment.builder()
                .idPayment(PAYMENT_ID)
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .build();

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        String payload = """
                {
                  "type": "payment_intent.payment_failed",
                  "data": {
                    "object": {
                      "id": "pi_test_failed_123",
                      "metadata": {
                        "paymentId": "15"
                      }
                    }
                  }
                }
                """;

        // when
        stripeWebhookService.handleEvent(payload);

        // then
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.OCZEKUJE_NA_PLATNOSC);

        assertThat(payment.getStripePaymentIntentId())
                .isEqualTo(PAYMENT_INTENT_ID);

        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentRepository).save(payment);
        verifyNoInteractions(
                appointmentAvailabilityService,
                eventPublisher
        );
    }

    @Test
    void shouldIgnorePaymentIntentFailure_WhenPaymentIsAlreadyPaid() {
        // given
        Payment payment = Payment.builder()
                .idPayment(PAYMENT_ID)
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.OPLACONA)
                .stripePaymentIntentId("pi_test_paid")
                .build();

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        String payload = """
                {
                  "type": "payment_intent.payment_failed",
                  "data": {
                    "object": {
                      "id": "pi_test_failed_123",
                      "metadata": {
                        "paymentId": "15"
                      }
                    }
                  }
                }
                """;

        // when
        stripeWebhookService.handleEvent(payload);

        // then
        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.OPLACONA);

        assertThat(payment.getStripePaymentIntentId())
                .isEqualTo("pi_test_paid");

        verify(paymentRepository).findById(PAYMENT_ID);
        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(
                appointmentAvailabilityService,
                eventPublisher
        );
    }


    @Test
    void shouldNotRestoreRefundedPayment_WhenPaymentIntentSucceeded() {
        Payment payment = createRefundedPayment();

        when(paymentRepository.findById(PAYMENT_ID))
                .thenReturn(Optional.of(payment));

        String payload = """
            {
              "type": "payment_intent.succeeded",
              "data": {
                "object": {
                  "id": "pi_test_refunded",
                  "metadata": {
                    "paymentId": "15"
                  }
                }
              }
            }
            """;

        stripeWebhookService.handleEvent(payload);

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.ZWROCONA);

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(
                appointmentAvailabilityService,
                eventPublisher
        );
    }

    @Test
    void shouldNotRestoreRefundedPayment_WhenCheckoutCompleted() {
        Payment payment = createRefundedPayment();

        when(paymentRepository.findByStripeCheckoutSessionId("cs_test_refunded"))
                .thenReturn(Optional.of(payment));

        String payload = """
            {
              "type": "checkout.session.completed",
              "data": {
                "object": {
                  "id": "cs_test_refunded",
                  "payment_intent": "pi_test_refunded"
                }
              }
            }
            """;

        stripeWebhookService.handleEvent(payload);

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.ZWROCONA);

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(
                appointmentAvailabilityService,
                eventPublisher
        );
    }

    @Test
    void shouldNotExpirePreviouslyRefundedPayment() {
        Payment payment = createRefundedPayment();

        when(paymentRepository.findByStripeCheckoutSessionId("cs_test_refunded"))
                .thenReturn(Optional.of(payment));

        String payload = """
            {
              "type": "checkout.session.expired",
              "data": {
                "object": {
                  "id": "cs_test_refunded"
                }
              }
            }
            """;

        stripeWebhookService.handleEvent(payload);

        assertThat(payment.getPaymentStatus())
                .isEqualTo(PaymentStatus.ZWROCONA);

        verify(paymentRepository, never()).save(any());
        verifyNoInteractions(appointmentAvailabilityService, eventPublisher);
    }

    @Test
    void shouldNotModifyPayment_WhenPartialChargeRefundEventArrives() {
        String payload = """
            {
              "type": "charge.refunded",
              "data": {
                "object": {
                  "id": "ch_test_partial",
                  "payment_intent": "pi_test_refunded",
                  "amount": 8000,
                  "amount_refunded": 2000
                }
              }
            }
            """;

        stripeWebhookService.handleEvent(payload);

        verifyNoInteractions(
                paymentRepository,
                appointmentAvailabilityService,
                eventPublisher);
    }

    private Payment createRefundedPayment() {
        return Payment.builder()
                .idPayment(PAYMENT_ID)
                .paymentMethod(PaymentMethod.KARTA_ONLINE)
                .paymentStatus(PaymentStatus.ZWROCONA)
                .stripePaymentIntentId("pi_test_refunded")
                .build();
    }

}
