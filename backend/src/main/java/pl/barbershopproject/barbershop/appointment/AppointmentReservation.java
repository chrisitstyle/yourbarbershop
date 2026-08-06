package pl.barbershopproject.barbershop.appointment;

import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;

/**
 * Defines operations responsible for managing appointment slot reservations.
 *
 * <p>The interface hides the implementation details of appointment availability
 * management from order-related services. It supports creating, updating and
 * releasing slot reservations depending on the appointment orderStatus.</p>
 */
public interface AppointmentReservation {

    /**
     * Reserves an appointment slot for the specified visit date.
     *
     * @param visitDate date and time of the appointment to reserve
     * @throws IllegalArgumentException if {@code visitDate} is {@code null}
     */
    void reserveSlot(LocalDateTime visitDate);

    /**
     * Updates an existing appointment slot reservation.
     *
     * <p>The operation may reserve a new slot, release the current slot,
     * replace the current slot with another one or leave the reservation
     * unchanged, depending on the current and target appointment states.</p>
     *
     * @param currentVisitDate current appointment date and time
     * @param currentOrderStatus current appointment orderStatus
     * @param targetVisitDate target appointment date and time
     * @param targetOrderStatus target appointment orderStatus
     */
    void updateSlotReservation(
            LocalDateTime currentVisitDate,
            OrderStatus currentOrderStatus,
            LocalDateTime targetVisitDate,
            OrderStatus targetOrderStatus
    );

    /**
     * Releases the appointment slot when the provided orderStatus represents
     * an active reservation.
     *
     * @param visitDate date and time of the appointment slot
     * @param orderStatus current appointment orderStatus
     */
    void releaseIfReserved(LocalDateTime visitDate, OrderStatus orderStatus);
}