package pl.barbershopproject.barbershop.appointment;

import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;

/**
 * Defines operations responsible for managing appointment slot reservations.
 *
 * <p>The interface hides the implementation details of appointment availability
 * management from order-related services. It supports creating, updating and
 * releasing slot reservations depending on the appointment status.</p>
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
     * @param currentStatus current appointment status
     * @param targetVisitDate target appointment date and time
     * @param targetStatus target appointment status
     */
    void updateSlotReservation(
            LocalDateTime currentVisitDate,
            Status currentStatus,
            LocalDateTime targetVisitDate,
            Status targetStatus
    );

    /**
     * Releases the appointment slot when the provided status represents
     * an active reservation.
     *
     * @param visitDate date and time of the appointment slot
     * @param status current appointment status
     */
    void releaseIfReserved(LocalDateTime visitDate, Status status);
}