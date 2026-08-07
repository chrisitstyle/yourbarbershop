package pl.barbershopproject.barbershop.appointment;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AppointmentAvailabilityService implements AppointmentReservation {

    private final AppointmentSlotRepository appointmentSlotRepository;

    /**
     * Reserves an appointment slot for the given visit date.
     * Throws AppointmentSlotTakenException if the slot is already taken.
     */
    @Override
    @Transactional
    public void reserveSlot(LocalDateTime visitDate) {
        validateVisitDate(visitDate);

        try {
            appointmentSlotRepository.saveAndFlush(
                    AppointmentSlot.builder()
                            .visitDate(visitDate)
                            .build()
            );
        } catch (DataIntegrityViolationException _) {
            throw new AppointmentSlotTakenException(visitDate);
        }
    }

    /**
     * Updates the appointment slot reservation when the visit date or orderStatus changes.
     * Reserves a new slot, releases the old one, or does nothing if no slot change is required.
     */
    @Override
    @Transactional
    public void updateSlotReservation(
            LocalDateTime currentVisitDate,
            OrderStatus currentOrderStatus,
            LocalDateTime targetVisitDate,
            OrderStatus targetOrderStatus
    ) {
        boolean currentReservesSlot = reservesSlot(currentOrderStatus);
        boolean targetReservesSlot = reservesSlot(targetOrderStatus);

        if (!currentReservesSlot && !targetReservesSlot) {
            return;
        }

        if (!currentReservesSlot) {
            reserveSlot(targetVisitDate);
            return;
        }

        if (!targetReservesSlot) {
            release(currentVisitDate);
            return;
        }

        if (!Objects.equals(currentVisitDate, targetVisitDate)) {
            reserveSlot(targetVisitDate);
            release(currentVisitDate);
        }
    }

    /**
     * Releases the appointment slot if the given orderStatus represents an active reservation.
     */
    @Override
    @Transactional
    public void releaseIfReserved(LocalDateTime visitDate, OrderStatus orderStatus) {
        if (reservesSlot(orderStatus)) {
            release(visitDate);
        }
    }

    /**
     * Removes the appointment slot for the given visit date.
     */
    private void release(LocalDateTime visitDate) {
        if (visitDate != null) {
            appointmentSlotRepository.deleteByVisitDate(visitDate);
        }
    }

    /**
     * Checks whether the given appointment orderStatus should reserve a slot.
     */
    private boolean reservesSlot(OrderStatus orderStatus) {
        return orderStatus != OrderStatus.ANULOWANE;
    }

    /**
     * Validates that the visit date is present.
     */
    private void validateVisitDate(LocalDateTime visitDate) {
        if (visitDate == null) {
            throw new IllegalArgumentException("Termin wizyty jest wymagany");
        }
    }
}