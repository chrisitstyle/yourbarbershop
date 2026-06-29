package pl.barbershopproject.barbershop.order.dto;

import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public record OrderUpdatedRequestDTO(
        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @NotNull(message = "Data wizyty jest wymagana")
        LocalDateTime visitDate,

        Status status
) {
}
