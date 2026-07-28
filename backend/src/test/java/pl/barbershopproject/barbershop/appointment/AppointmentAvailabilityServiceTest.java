package pl.barbershopproject.barbershop.appointment;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import pl.barbershopproject.barbershop.exception.AppointmentSlotTakenException;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentAvailabilityServiceTest {

    @Mock
    private AppointmentSlotRepository appointmentSlotRepository;

    @InjectMocks
    private AppointmentAvailabilityService appointmentAvailabilityService;

    @Test
    void shouldReserveSlot() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.reserveSlot(visitDate);

        ArgumentCaptor<AppointmentSlot> slotCaptor = ArgumentCaptor.forClass(AppointmentSlot.class);

        verify(appointmentSlotRepository).saveAndFlush(slotCaptor.capture());

        AppointmentSlot savedSlot = slotCaptor.getValue();

        assertThat(savedSlot.getVisitDate()).isEqualTo(visitDate);
    }

    @Test
    void shouldThrowAppointmentSlotTakenExceptionWhenSlotIsAlreadyReserved() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        when(appointmentSlotRepository.saveAndFlush(any(AppointmentSlot.class)))
                .thenThrow(DataIntegrityViolationException.class);

        assertThatThrownBy(() -> appointmentAvailabilityService.reserveSlot(visitDate))
                .isInstanceOf(AppointmentSlotTakenException.class);

        verify(appointmentSlotRepository).saveAndFlush(any(AppointmentSlot.class));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenVisitDateIsNull() {
        assertThatThrownBy(() -> appointmentAvailabilityService.reserveSlot(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Termin wizyty jest wymagany");

        verifyNoInteractions(appointmentSlotRepository);
    }

    @Test
    void shouldDoNothingWhenCurrentAndTargetStatusesDoNotReserveSlot() {
        LocalDateTime currentVisitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);
        LocalDateTime targetVisitDate = LocalDateTime.of(2026, Month.APRIL, 21, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                currentVisitDate,
                Status.ANULOWANE,
                targetVisitDate,
                Status.ANULOWANE
        );

        verifyNoInteractions(appointmentSlotRepository);
    }

    @Test
    void shouldReserveSlotWhenAppointmentChangesFromCancelledToActive() {
        LocalDateTime targetVisitDate = LocalDateTime.of(2026, Month.APRIL, 21, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                LocalDateTime.of(2026, Month.APRIL, 20, 12, 0),
                Status.ANULOWANE,
                targetVisitDate,
                Status.NOWE
        );

        ArgumentCaptor<AppointmentSlot> slotCaptor = ArgumentCaptor.forClass(AppointmentSlot.class);

        verify(appointmentSlotRepository).saveAndFlush(slotCaptor.capture());
        verify(appointmentSlotRepository, never()).deleteByVisitDate(any());

        assertThat(slotCaptor.getValue().getVisitDate()).isEqualTo(targetVisitDate);
    }

    @Test
    void shouldReleaseSlotWhenAppointmentChangesFromActiveToCancelled() {
        LocalDateTime currentVisitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                currentVisitDate,
                Status.NOWE,
                currentVisitDate,
                Status.ANULOWANE
        );

        verify(appointmentSlotRepository).deleteByVisitDate(currentVisitDate);
        verify(appointmentSlotRepository, never()).saveAndFlush(any());
    }

    @Test
    void shouldUpdateSlotWhenActiveAppointmentChangesVisitDate() {
        LocalDateTime currentVisitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);
        LocalDateTime targetVisitDate = LocalDateTime.of(2026, Month.APRIL, 21, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                currentVisitDate,
                Status.NOWE,
                targetVisitDate,
                Status.NOWE
        );

        ArgumentCaptor<AppointmentSlot> slotCaptor = ArgumentCaptor.forClass(AppointmentSlot.class);

        verify(appointmentSlotRepository).saveAndFlush(slotCaptor.capture());
        verify(appointmentSlotRepository).deleteByVisitDate(currentVisitDate);

        assertThat(slotCaptor.getValue().getVisitDate()).isEqualTo(targetVisitDate);
    }

    @Test
    void shouldNotUpdateSlotWhenActiveAppointmentKeepsSameVisitDate() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.updateSlotReservation(
                visitDate,
                Status.NOWE,
                visitDate,
                Status.NOWE
        );

        verifyNoInteractions(appointmentSlotRepository);
    }

    @Test
    void shouldReleaseSlotIfStatusReservesSlot() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.releaseIfReserved(visitDate, Status.NOWE);

        verify(appointmentSlotRepository).deleteByVisitDate(visitDate);
    }

    @Test
    void shouldNotReleaseSlotIfStatusDoesNotReserveSlot() {
        LocalDateTime visitDate = LocalDateTime.of(2026, Month.APRIL, 20, 12, 0);

        appointmentAvailabilityService.releaseIfReserved(visitDate, Status.ANULOWANE);

        verifyNoInteractions(appointmentSlotRepository);
    }
}