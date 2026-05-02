package pl.barbershopproject.barbershop.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ForgotPasswordRequest(
        @NotBlank(message = "Email nie może być pusty")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @NotBlank(message = "CAPTCHA jest wymagana")
        String captchaToken
) {
}
