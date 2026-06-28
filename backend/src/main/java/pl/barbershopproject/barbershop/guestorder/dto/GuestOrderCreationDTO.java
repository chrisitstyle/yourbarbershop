package pl.barbershopproject.barbershop.guestorder.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.payment.PaymentMethod;

import java.time.LocalDateTime;

public record GuestOrderCreationDTO(
        @NotBlank(message = "Imię jest wymagane")
        String firstname,

        @NotBlank(message = "Nazwisko jest wymagane")
        String lastname,

        @NotBlank(message = "Numer telefonu jest wymagany")
        String phonenumber,

        @NotBlank(message = "Adres email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @NotNull(message = "Data wizyty jest wymagana")
        @Future(message = "Data wizyty musi być w przyszłości")
        LocalDateTime visitDate,

        @NotNull(message = "Metoda płatności jest wymagana")
        PaymentMethod paymentMethod
) {
}
