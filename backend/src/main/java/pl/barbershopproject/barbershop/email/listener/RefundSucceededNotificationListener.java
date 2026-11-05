
package pl.barbershopproject.barbershop.email.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.email.template.RefundSucceededEmailTemplate;
import pl.barbershopproject.barbershop.payment.refund.event.RefundSucceededEvent;

/**
 * Sends successful refund confirmation emails after the refund
 * transaction has been committed.
 * <p>
 * Notification failures are logged but do not affect the
 * persisted refund or payment state.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RefundSucceededNotificationListener {

    private final EmailSenderService emailSenderService;

    /**
     * Sends a confirmation to the customer after a successful refund.
     * <p>
     * The event is handled asynchronously and only after commit.
     * This listener never creates or retries Stripe refunds.
     *
     * @param event successfully completed refund details
     */
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRefundSucceeded(RefundSucceededEvent event) {
        try {
            emailSenderService.sendHtmlEmail(
                    event.customerEmail(),
                    RefundSucceededEmailTemplate.subject(),
                    RefundSucceededEmailTemplate.plainText(event),
                    RefundSucceededEmailTemplate.html(event));

        } catch (RuntimeException exception) {
            log.error("Failed to send successful refund notification "
                            + "for refund {}",
                    event.refundId(),
                    exception);
        }
    }
}
