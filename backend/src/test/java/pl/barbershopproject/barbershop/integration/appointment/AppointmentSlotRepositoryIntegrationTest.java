package pl.barbershopproject.barbershop.integration.appointment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.barbershopproject.barbershop.appointment.AppointmentSlot;
import pl.barbershopproject.barbershop.appointment.AppointmentSlotRepository;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class AppointmentSlotRepositoryIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private AppointmentSlotRepository appointmentSlotRepository;

    @BeforeEach
    void cleanAppointmentSlots() {
        appointmentSlotRepository.deleteAll();
        appointmentSlotRepository.flush();
    }

    @Test
    void shouldDeleteSlotByVisitDate() {
        LocalDateTime visitDate = LocalDateTime.of(2026, 4, 20, 12, 0);

        appointmentSlotRepository.saveAndFlush(
                AppointmentSlot.builder()
                        .visitDate(visitDate)
                        .build()
        );

        appointmentSlotRepository.deleteByVisitDate(visitDate);
        appointmentSlotRepository.flush();

        assertThat(appointmentSlotRepository.findAll()).isEmpty();
    }

    @Test
    void shouldNotDeleteSlotWithDifferentVisitDate() {
        LocalDateTime existingVisitDate = LocalDateTime.of(2026, 4, 20, 12, 0);
        LocalDateTime differentVisitDate = LocalDateTime.of(2026, 4, 21, 12, 0);

        appointmentSlotRepository.saveAndFlush(
                AppointmentSlot.builder()
                        .visitDate(existingVisitDate)
                        .build()
        );

        appointmentSlotRepository.deleteByVisitDate(differentVisitDate);
        appointmentSlotRepository.flush();

        assertThat(appointmentSlotRepository.findAll())
                .singleElement()
                .extracting(AppointmentSlot::getVisitDate)
                .isEqualTo(existingVisitDate);
    }
}
