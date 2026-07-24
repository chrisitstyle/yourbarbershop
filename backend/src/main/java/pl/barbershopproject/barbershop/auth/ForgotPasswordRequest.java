package pl.barbershopproject.barbershop.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for triggering a password reset email")
public record ForgotPasswordRequest(
        @Schema(description = "Email address associated with the account", example = "john.doe@example.com")
        @NotBlank(message = "Email nie może być pusty")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @Schema(description = "Google reCAPTCHA token", example = "03AFcWeA7...")
        @NotBlank(message = "CAPTCHA jest wymagana")
        String captchaToken
) {
}
