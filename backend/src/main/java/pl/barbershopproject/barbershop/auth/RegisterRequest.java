package pl.barbershopproject.barbershop.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank(message = "Imię jest wymagane")
        String firstname,

        @NotBlank(message = "Nazwisko jest wymagane")
        String lastname,

        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format adresu email")
        String email,

        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 8, max = 32, message = "Hasło musi mieć od {min} do {max} znaków")
        String password
) {
}
