package pl.barbershopproject.barbershop.email.listener;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.OnlinePaymentPendingEmailTemplate;
import pl.barbershopproject.barbershop.payment.PaymentLinkGenerator;
import pl.barbershopproject.barbershop.payment.event.OnlinePaymentPendingEvent;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OnlinePaymentNotificationListenerTest {

    private static final Long PAYMENT_ID = 15L;

    private static final String EMAIL = "customer@example.com";

    private static final String PAYMENT_LINK = "http://localhost:3000/payment/signed-token";

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private PaymentLinkGenerator paymentLinkGenerator;

    private OnlinePaymentNotificationListener listener;

    @BeforeEach
    void setUp() {
        listener = new OnlinePaymentNotificationListener(
                emailSenderService,
                paymentLinkGenerator);
    }

    @Test
    void shouldSendPaymentLinkEmail() {
        // given
        OnlinePaymentPendingEvent event =
                new OnlinePaymentPendingEvent(
                        PAYMENT_ID,
                        EMAIL,
                        "Jan",
                        LocalDateTime.of(
                                2030,
                                Month.JANUARY,
                                11,
                                12,
                                0
                        ),
                        "Strzyżenie",
                        BigDecimal.valueOf(80)
                );

        when(paymentLinkGenerator.createLink(PAYMENT_ID))
                .thenReturn(PAYMENT_LINK);

        ArgumentCaptor<String> plainTextCaptor = ArgumentCaptor.forClass(String.class);

        ArgumentCaptor<String> htmlCaptor = ArgumentCaptor.forClass(String.class);

        // when
        listener.handleOnlinePaymentPending(event);

        // then
        verify(paymentLinkGenerator)
                .createLink(PAYMENT_ID);

        verify(emailSenderService).sendHtmlEmail(
                org.mockito.ArgumentMatchers.eq(EMAIL),
                org.mockito.ArgumentMatchers.eq(
                        OnlinePaymentPendingEmailTemplate.subject()
                ),
                plainTextCaptor.capture(),
                htmlCaptor.capture());

        assertThat(plainTextCaptor.getValue())
                .contains("Jan")
                .contains("Strzyżenie")
                .contains("80")
                .contains(PAYMENT_LINK);

        assertThat(htmlCaptor.getValue())
                .contains("Jan")
                .contains("Strzyżenie")
                .contains("80")
                .contains(PAYMENT_LINK)
                .contains("Przejdź do płatności");
    }
}
