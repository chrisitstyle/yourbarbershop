package pl.barbershopproject.barbershop.email.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.OnlinePaymentPendingEmailTemplate;
import pl.barbershopproject.barbershop.payment.PaymentLinkGenerator;
import pl.barbershopproject.barbershop.payment.event.OnlinePaymentPendingEvent;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Sends payment links for online payments after the reservation
 * transaction has been successfully committed.
 */
@Component
@RequiredArgsConstructor
public class OnlinePaymentNotificationListener {

    private static final DateTimeFormatter POLISH_FORMATTER = DateTimeFormatter.ofPattern(
                    "d MMMM yyyy, 'godz.' HH:mm",
                    Locale.of("pl", "PL"));

    private final EmailSenderService emailSenderService;
    private final PaymentLinkGenerator paymentLinkGenerator;

    /**
     * Generates a signed payment link and sends it to the customer.
     *
     * @param event online payment awaiting customer completion
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOnlinePaymentPending(
            OnlinePaymentPendingEvent event) {
        String paymentLink = paymentLinkGenerator.createLink(
                        event.paymentId());

        String formattedDate = event.visitDate().format(POLISH_FORMATTER);

        String offerCost =
                String.valueOf(event.offerCost());

        emailSenderService.sendHtmlEmail(
                event.email(),
                OnlinePaymentPendingEmailTemplate.subject(),
                OnlinePaymentPendingEmailTemplate.plainText(
                        event.firstname(),
                        formattedDate,
                        event.offerName(),
                        offerCost,
                        paymentLink
                ),
                OnlinePaymentPendingEmailTemplate.html(
                        event.firstname(),
                        formattedDate,
                        event.offerName(),
                        offerCost,
                        paymentLink
                )
        );
    }
}
