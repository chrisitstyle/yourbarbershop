package pl.barbershopproject.barbershop.appointment;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Table(name = "appointment_slot", uniqueConstraints = {@UniqueConstraint(
        name = "uq_appointment_slot_visit_date",
        columnNames = "visit_date"
)
}
)
@Entity
public class AppointmentSlot {

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Id
    private Long idAppointmentSlot;

    @Column(name = "visit_date", nullable = false, unique = true)
    private LocalDateTime visitDate;
}
