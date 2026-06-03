package pl.barbershopproject.barbershop.exception;

import java.time.LocalDateTime;

public class AppointmentSlotTakenException extends RuntimeException {

    public AppointmentSlotTakenException(LocalDateTime visitDate) {
        super("Termin " + visitDate + " jest już zajęty");
    }
}
