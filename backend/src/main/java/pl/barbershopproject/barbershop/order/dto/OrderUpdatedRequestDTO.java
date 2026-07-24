package pl.barbershopproject.barbershop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

@Schema(description = "Payload for updating an existing user order")
public record OrderUpdatedRequestDTO(
        @Schema(description = "ID of the service offer", example = "2")
        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @Schema(description = "Updated visit date and time", example = "2026-08-16T11:30:00")
        @NotNull(message = "Data wizyty jest wymagana")
        LocalDateTime visitDate,

        @Schema(description = "Updated order status", example = "ZAKONCZONE")
        Status status
) {
}
