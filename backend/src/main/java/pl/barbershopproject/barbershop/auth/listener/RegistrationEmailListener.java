package pl.barbershopproject.barbershop.auth.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.auth.event.UserRegisteredEvent;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.RegistrationSuccessEmailTemplate;

@Component
@RequiredArgsConstructor
public class RegistrationEmailListener {

    private final EmailSenderService emailSenderService;


    /*
     * Sends the welcome email only after the registration transaction is committed.
     * This prevents sending an email for a user account that failed to persist.
     * fallbackExecution allows the listener to run even when the event is published
     * outside an active transaction.
     *
     * @Async runs the email sending in a separate thread, so registration response
     * is not delayed by SMTP communication.
     */
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handleUserRegistered(UserRegisteredEvent event) {
        emailSenderService.sendHtmlEmail(
                event.email(),
                RegistrationSuccessEmailTemplate.subject(),
                RegistrationSuccessEmailTemplate.plainText(event.firstname()),
                RegistrationSuccessEmailTemplate.html(event.firstname())
        );
    }
}
