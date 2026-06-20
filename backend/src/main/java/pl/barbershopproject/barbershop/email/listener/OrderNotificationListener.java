package pl.barbershopproject.barbershop.email.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.OrderConfirmationEmailTemplate;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private static final DateTimeFormatter POLISH_FORMATTER =
            DateTimeFormatter.ofPattern(
                    "d MMMM yyyy, 'godz.' HH:mm",
                    Locale.of("pl", "PL")
            );

    private final EmailSenderService emailSenderService;

    @Async // mail in other thread
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        String formattedDate = event.visitDate().format(POLISH_FORMATTER);
        String offerCost = String.valueOf(event.offerCost());

        emailSenderService.sendHtmlEmail(
                event.email(),
                OrderConfirmationEmailTemplate.subject(),
                OrderConfirmationEmailTemplate.plainText(
                        event.firstname(),
                        formattedDate,
                        event.offerKind(),
                        offerCost
                ),
                OrderConfirmationEmailTemplate.html(
                        event.firstname(),
                        formattedDate,
                        event.offerKind(),
                        offerCost
                )
        );
    }
}