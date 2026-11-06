
package pl.barbershopproject.barbershop.email.listener;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.RefundSucceededEmailTemplate;
import pl.barbershopproject.barbershop.payment.refund.event.RefundSucceededEvent;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static pl.barbershopproject.barbershop.order.AdminCancellationResult.ReservationType;

@ExtendWith(MockitoExtension.class)
class RefundSucceededNotificationListenerTest {

    @Mock
    private EmailSenderService emailSenderService;

    @Test
    void shouldSendSuccessfulRefundConfirmationEmail() {
        RefundSucceededNotificationListener listener = new RefundSucceededNotificationListener(emailSenderService);

        RefundSucceededEvent event = new RefundSucceededEvent(
                9L,
                18L,
                ReservationType.REGISTERED,
                "customer@example.com",
                "Jan",
                new BigDecimal("65.00"),
                "PLN");

        listener.handleRefundSucceeded(event);

        ArgumentCaptor<String> plainTextCaptor = ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailSenderService).sendHtmlEmail(
                eq("customer@example.com"),
                eq(RefundSucceededEmailTemplate.subject()),
                plainTextCaptor.capture(),
                htmlCaptor.capture());

        assertThat(plainTextCaptor.getValue())
                .contains("Jan")
                .contains("65,00")
                .contains("Rezerwacja użytkownika #18")
                .contains("Numer zwrotu: #9");

        assertThat(htmlCaptor.getValue())
                .contains("YourBarbershop")
                .contains("65,00")
                .contains("Rezerwacja użytkownika #18")
                .contains("#9");
    }
}
