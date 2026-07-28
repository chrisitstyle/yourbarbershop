package pl.barbershopproject.barbershop.guestorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;

@Schema(description = "Payload for updating guest reservation details by an admin")
public record GuestOrderUpdateRequestDTO(
        @Schema(description = "Updated guest first name", example = "Jan")
        @NotBlank(message = "Imię jest wymagane")
        String firstname,

        @Schema(description = "Updated guest last name", example = "Kowalski")
        @NotBlank(message = "Nazwisko jest wymagane")
        String lastname,

        @Schema(description = "Updated phone number", example = "+48987654321")
        @NotBlank(message = "Numer telefonu jest wymagany")
        String phonenumber,

        @Schema(description = "Updated email address", example = "jan.kowalski@example.com")
        @NotBlank(message = "Adres email jest wymagany")
        @Email(message = "Adres email ma nieprawidłowy format")
        String email,

        @Schema(description = "ID of the selected offer", example = "1")
        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @Schema(description = "Updated visit date and time", example = "2026-08-15T15:00:00")
        @NotNull(message = "Data wizyty jest wymagana")
        LocalDateTime visitDate,

        @Schema(description = "Updated order status", example = "ZAKONCZONE")
        Status status
) {
}
