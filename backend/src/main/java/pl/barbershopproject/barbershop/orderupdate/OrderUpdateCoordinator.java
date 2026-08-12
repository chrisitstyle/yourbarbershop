package pl.barbershopproject.barbershop.orderupdate;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferChangeHandler;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;

/**
 * Coordinates the common preparation steps required before updating an order-like entity.
 * It validates status transitions, resolves and applies offer changes, and updates
 * appointment slot reservations.
 */
@Component
@RequiredArgsConstructor
public class OrderUpdateCoordinator {

    private final OrderModificationPolicy orderModificationPolicy;
    private final OfferQuery offerQuery;
    private final OfferChangeHandler offerChangeHandler;
    private final AppointmentReservation appointmentReservation;

    /**
     * Prepares an order update by validating the requested status, applying an offer change
     * when needed, and updating the appointment slot reservation.
     *
     * @param order           order-like entity being updated
     * @param payment         payment associated with the order
     * @param targetOfferId   identifier of the requested offer
     * @param targetVisitDate requested visit date
     * @param requestedStatus requested order status, or {@code null} to keep the current status
     * @return current and target statuses required by the caller to complete the update
     */
    public OrderUpdateResult prepareUpdate(
            OrderUpdateTarget order,
            Payment payment,
            Long targetOfferId,
            LocalDateTime targetVisitDate,
            OrderStatus requestedStatus
    ) {
        OrderStatus currentStatus = order.getOrderStatus();

        OrderStatus targetStatus = requestedStatus != null
                ? requestedStatus
                : currentStatus;

        orderModificationPolicy.validateUpdate(
                currentStatus,
                targetStatus,
                payment
        );

        Offer targetOffer = offerQuery.getRequiredOffer(
                targetOfferId);

        offerChangeHandler.updateIfChanged(
                order.getOffer(),
                targetOffer,
                payment,
                order::setOffer,
                order::setBookedOffer);

        appointmentReservation.updateSlotReservation(
                order.getVisitDate(),
                currentStatus,
                targetVisitDate,
                targetStatus);

        return new OrderUpdateResult(
                currentStatus,
                targetStatus);
    }
}
