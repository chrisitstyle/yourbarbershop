package pl.barbershopproject.barbershop.integration.appointment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.appointment.AppointmentSlot;
import pl.barbershopproject.barbershop.appointment.AppointmentSlotRepository;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AppointmentAvailabilityServiceIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @BeforeEach
    void cleanAppointmentSlots() {
        appointmentSlotRepository.deleteAll();
        appointmentSlotRepository.flush();
    }

    @Test
    void shouldReserveSlotInDatabase() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.reserveSlot(visitDate);

        assertThat(appointmentSlotRepository.findAll())
                .singleElement()
                .extracting(AppointmentSlot::getVisitDate)
                .isEqualTo(visitDate);
    }

    @Test
    void shouldRejectDuplicateSlotUsingDatabaseUniqueConstraint() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.reserveSlot(visitDate);

        assertThatThrownBy(() -> appointmentAvailabilityService.reserveSlot(visitDate))
                .isInstanceOf(AppointmentSlotTakenException.class)
                .hasMessageContaining(visitDate.toString());

        assertThat(appointmentSlotRepository.count()).isEqualTo(1);
    }

    @Test
    void shouldUpdateSlotReservationWhenActiveAppointmentChangesVisitDate() {
        LocalDateTime currentVisitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);
        LocalDateTime targetVisitDate = LocalDateTime.of(2026, Month.APRIL, 21, 12, 0);

        appointmentAvailabilityService.reserveSlot(currentVisitDate);

        appointmentAvailabilityService.updateSlotReservation(
                currentVisitDate,
                Status.NOWE,
                targetVisitDate,
                Status.NOWE
        );

        assertThat(appointmentSlotRepository.findAll())
                .singleElement()
                .extracting(AppointmentSlot::getVisitDate)
                .isEqualTo(targetVisitDate);
    }

    @Test
    void shouldReleaseSlotWhenAppointmentBecomesCancelled() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.reserveSlot(visitDate);

        appointmentAvailabilityService.updateSlotReservation(
                visitDate,
                Status.NOWE,
                visitDate,
                Status.ANULOWANE
        );

        assertThat(appointmentSlotRepository.findAll()).isEmpty();
    }

    @Test
    void shouldReserveSlotWhenCancelledAppointmentBecomesActive() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                visitDate,
                Status.ANULOWANE,
                visitDate,
                Status.NOWE
        );

        assertThat(appointmentSlotRepository.findAll())
                .singleElement()
                .extracting(AppointmentSlot::getVisitDate)
                .isEqualTo(visitDate);
    }

    @Test
    void shouldDoNothingWhenCancelledAppointmentStaysCancelled() {
        LocalDateTime currentVisitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);
        LocalDateTime targetVisitDate = LocalDateTime.of(2026, Month.APRIL, 21, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                currentVisitDate,
                Status.ANULOWANE,
                targetVisitDate,
                Status.ANULOWANE
        );

        assertThat(appointmentSlotRepository.findAll()).isEmpty();
    }

    @Test
    void shouldDoNothingWhenActiveAppointmentKeepsSameVisitDate() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.reserveSlot(visitDate);

        appointmentAvailabilityService.updateSlotReservation(
                visitDate,
                Status.NOWE,
                visitDate,
                Status.NOWE
        );

        assertThat(appointmentSlotRepository.findAll())
                .singleElement()
                .extracting(AppointmentSlot::getVisitDate)
                .isEqualTo(visitDate);
    }

    @Test
    void shouldReleaseSlotIfReserved() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.reserveSlot(visitDate);

        appointmentAvailabilityService.releaseIfReserved(visitDate, Status.NOWE);

        assertThat(appointmentSlotRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotReleaseSlotIfStatusDoesNotReserveSlot() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.releaseIfReserved(visitDate, Status.ANULOWANE);

        assertThat(appointmentSlotRepository.findAll()).isEmpty();
    }
}