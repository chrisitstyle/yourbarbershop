package pl.barbershopproject.barbershop.auth.listener;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.auth.event.PasswordResetRequestedEvent;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.PasswordResetEmailTemplate;

@Component
@RequiredArgsConstructor
public class PasswordResetEmailListener {

    private final EmailSenderService emailSenderService;

    /*
     * Sends the password reset email only after the reset token transaction is committed.
     * This prevents sending a reset link that points to a token that was not persisted.
     * fallbackExecution allows the listener to run even when the event is published
     * outside an active transaction.
     *
     * @Async runs the email sending in a separate thread, so the password reset request
     * response is not delayed by SMTP communication.
     */
    @Async
    @TransactionalEventListener(
            phase = TransactionPhase.AFTER_COMMIT,
            fallbackExecution = true
    )
    public void handlePasswordResetRequested(PasswordResetRequestedEvent event) {
        emailSenderService.sendHtmlEmail(
                event.email(),
                PasswordResetEmailTemplate.subject(),
                PasswordResetEmailTemplate.plainText(
                        event.firstname(),
                        event.resetUrl(),
                        event.expirationMinutes()
                ),
                PasswordResetEmailTemplate.html(
                        event.firstname(),
                        event.resetUrl(),
                        event.expirationMinutes()
                )
        );
    }
}
