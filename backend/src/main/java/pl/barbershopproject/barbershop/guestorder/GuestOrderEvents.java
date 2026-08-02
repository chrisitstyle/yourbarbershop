package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.utils.Status;

import static pl.barbershopproject.barbershop.utils.SecurityUtils.getActorEmailSafely;

/**
 * Publishes audit and notification events related to guest orders.
 *
 * <p>This component separates event publishing responsibilities from
 * {@link GuestOrderService}, allowing the service to focus on coordinating
 * the guest order processing flow.</p>
 */
@Component
@RequiredArgsConstructor
class GuestOrderEvents {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes events associated with the creation of a guest order.
     *
     * <p>An audit event is always published. An order confirmation event is
     * additionally published when the selected payment method does not require
     * an online card payment.</p>
     *
     * @param guestOrder newly created guest order
     * @param payment payment associated with the guest order
     */
    void created(GuestOrder guestOrder, Payment payment) {
        eventPublisher.publishEvent(new AuditEvent(
                guestOrder.getEmail(),
                ActionType.GUEST_ORDER_CREATED,
                EntityType.GUEST_ORDER,
                String.valueOf(guestOrder.getIdGuestOrder()),
                String.format(
                        "{\"offerKind\":\"%s\", \"cost\":%s, \"visitDate\":\"%s\"}",
                        guestOrder.getOffer().getKind(),
                        guestOrder.getOffer().getCost(),
                        guestOrder.getVisitDate()
                )
        ));

        if (payment.getPaymentMethod() != PaymentMethod.KARTA_ONLINE) {
            publishConfirmation(guestOrder, payment);
        }
    }

    /**
     * Publishes an audit event associated with a guest order update.
     *
     * <p>The event contains the previous status, the current status and
     * the updated appointment date.</p>
     *
     * @param guestOrder updated guest order
     * @param oldStatus status assigned to the guest order before the update
     */
    void updated(GuestOrder guestOrder, Status oldStatus) {
        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.GUEST_ORDER_UPDATED,
                EntityType.GUEST_ORDER,
                String.valueOf(guestOrder.getIdGuestOrder()),
                String.format(
                        "{\"oldStatus\":\"%s\", \"newStatus\":\"%s\", \"visitDate\":\"%s\"}",
                        oldStatus,
                        guestOrder.getStatus(),
                        guestOrder.getVisitDate()
                )
        ));
    }

    /**
     * Publishes an audit event associated with the deletion of a guest order.
     *
     * @param idGuestOrder identifier of the deleted guest order
     */
    void deleted(Long idGuestOrder) {
        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.GUEST_ORDER_DELETED,
                EntityType.GUEST_ORDER,
                String.valueOf(idGuestOrder),
                null
        ));
    }

    /**
     * Publishes an order confirmation event for a guest customer.
     *
     * <p>The event contains the customer details, appointment information,
     * selected offer and payment state.</p>
     *
     * @param guestOrder guest order for which the confirmation is published
     * @param payment payment associated with the guest order
     */
    private void publishConfirmation(
            GuestOrder guestOrder,
            Payment payment
    ) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                guestOrder.getEmail(),
                guestOrder.getFirstname(),
                guestOrder.getVisitDate(),
                guestOrder.getOffer().getKind(),
                guestOrder.getOffer().getCost(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus()
        ));
    }
}