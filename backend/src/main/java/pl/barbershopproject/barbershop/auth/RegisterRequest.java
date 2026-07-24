package pl.barbershopproject.barbershop.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for registering a new user account with CAPTCHA verification")
public record RegisterRequest(
        @Schema(description = "User's first name", example = "John")
        @NotBlank(message = "Imię jest wymagane")
        String firstname,

        @Schema(description = "User's last name", example = "Doe")
        @NotBlank(message = "Nazwisko jest wymagane")
        String lastname,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Nieprawidłowy format adresu email")
        String email,

        @Schema(description = "Account password", example = "Secret123!")
        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 8, max = 32, message = "Hasło musi mieć od {min} do {max} znaków")
        String password,

        @Schema(description = "Google reCAPTCHA token verified on the server", example = "03AFcWeA7...")
        @NotBlank(message = "CAPTCHA jest wymagana")
        String captchaToken
) {
}
