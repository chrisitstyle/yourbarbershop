package pl.barbershopproject.barbershop.payment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
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
}
