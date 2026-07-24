package pl.barbershopproject.barbershop.guestorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.payment.PaymentMethod;

import java.time.LocalDateTime;

@Schema(description = "Payload for creating a reservation without an existing account (guest mode)")
public record GuestOrderCreationDTO(
        @Schema(description = "Guest's first name", example = "Jan")
        @NotBlank(message = "Imię jest wymagane")
        String firstname,

        @Schema(description = "Guest's last name", example = "Kowalski")
        @NotBlank(message = "Nazwisko jest wymagane")
        String lastname,

        @Schema(description = "Guest's phone number", example = "+48123456789")
        @NotBlank(message = "Numer telefonu jest wymagany")
        String phonenumber,

        @Schema(description = "Guest's email address", example = "jan.kowalski@example.com")
        @NotBlank(message = "Adres email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @Schema(description = "ID of the selected service offer", example = "1")
        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @Schema(description = "Scheduled appointment date and time", example = "2026-08-15T14:30:00")
        @NotNull(message = "Data wizyty jest wymagana")
        @Future(message = "Data wizyty musi być w przyszłości")
        LocalDateTime visitDate,

        @Schema(description = "Chosen payment method", example = "KARTA_ONLINE")
        @NotNull(message = "Metoda płatności jest wymagana")
        PaymentMethod paymentMethod
) {
}
