package pl.barbershopproject.barbershop.guestorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;

public record GuestOrderUpdateRequestDTO(
        @NotBlank(message = "Imię jest wymagane")
        String firstname,

        @NotBlank(message = "Nazwisko jest wymagane")
        String lastname,

        @NotBlank(message = "Numer telefonu jest wymagany")
        String phonenumber,

        @NotBlank(message = "Adres email jest wymagany")
        @Email(message = "Adres email ma nieprawidłowy format")
        String email,

        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @NotNull(message = "Data wizyty jest wymagana")
        LocalDateTime visitDate,

        Status status
) {
}
