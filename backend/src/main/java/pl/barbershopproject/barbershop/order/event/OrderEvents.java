package pl.barbershopproject.barbershop.order.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import static pl.barbershopproject.barbershop.utils.SecurityUtils.getActorEmailSafely;

/**
 * Publishes audit and notification events related to registered-user orders.
 *
 * <p>This component separates event publishing responsibilities from
 * order processing services, allowing them to focus on coordinating
 * order creation, update and deletion operations.</p>
 */
@Component
@RequiredArgsConstructor
public class OrderEvents {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * Publishes events associated with the creation of an order.
     *
     * <p>An audit event is always published. An order confirmation event
     * is additionally published when the selected payment method does not
     * require an online card payment.</p>
     *
     * @param order newly created order
     * @param payment payment associated with the order
     */
    public void created(Order order, Payment payment) {
        eventPublisher.publishEvent(new AuditEvent(
                order.getUser().getEmail(),
                ActionType.ORDER_CREATED,
                EntityType.ORDER,
                String.valueOf(order.getIdOrder()),
                String.format(
                        "{\"offerKind\":\"%s\", \"cost\":%s, \"visitDate\":\"%s\"}",
                        order.getOffer().getKind(),
                        order.getOffer().getCost(),
                        order.getVisitDate()
                )
        ));

        if (payment.getPaymentMethod() != PaymentMethod.KARTA_ONLINE) {
            publishConfirmation(order, payment);
        }
    }

    /**
     * Publishes an audit event associated with an order update.
     *
     * <p>The event contains the previous orderStatus, the current orderStatus
     * and the updated appointment date.</p>
     *
     * @param order updated order
     * @param oldOrderStatus orderStatus assigned to the order before the update
     */
    public void updated(Order order, OrderStatus oldOrderStatus) {
        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.ORDER_UPDATED,
                EntityType.ORDER,
                String.valueOf(order.getIdOrder()),
                String.format(
                        "{\"oldOrderStatus\":\"%s\", \"newStatus\":\"%s\", \"visitDate\":\"%s\"}",
                        oldOrderStatus,
                        order.getOrderStatus(),
                        order.getVisitDate()
                )
        ));
    }

    /**
     * Publishes an audit event associated with the deletion of an order.
     *
     * @param idOrder identifier of the deleted order
     */
    public void deleted(Long idOrder) {
        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.ORDER_DELETED,
                EntityType.ORDER,
                String.valueOf(idOrder),
                null
        ));
    }

    /**
     * Publishes an order confirmation event for the registered customer.
     *
     * <p>The event contains the customer details, appointment information,
     * selected offer and payment state.</p>
     *
     * @param order order for which the confirmation is published
     * @param payment payment associated with the order
     */
    private void publishConfirmation(Order order, Payment payment) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getUser().getEmail(),
                order.getUser().getFirstname(),
                order.getVisitDate(),
                order.getOffer().getKind(),
                order.getOffer().getCost(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus()
        ));
    }
}