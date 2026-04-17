package pl.barbershopproject.barbershop.email.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;

import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class OrderNotificationListener {

    private final EmailSenderService emailSenderService;

    private static final DateTimeFormatter POLISH_FORMATTER =
            DateTimeFormatter.ofPattern("d MMMM yyyy, 'godz.' HH:mm",
                    Locale.of("pl", "PL"));

    @Async // mail in other thread
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {

        String formattedDate = event.visitDate().format(POLISH_FORMATTER);

        String subject = "Potwierdzenie rezerwacji wizyty";

        String message = String.format(
                "Cześć %s!%n%n" +
                        "Dziękujemy za umówienie wizyty w naszym salonie YourBarbershop.%n%n" +
                        "Data wizyty: %s.%n" +
                        "Wybrana oferta: %s%n" +
                        "Koszt usługi: %s zł.%n%n" +
                        "Zapraszamy w uzgodnionym terminie do nas!%n%n" +
                        "Z poważaniem,%n" +
                        "Zespół YourBarbershop",
                event.firstname(),
                formattedDate,
                event.offerKind(),
                event.offerCost()
        );

        emailSenderService.sendEmail(event.email(), subject, message);
    }
}
