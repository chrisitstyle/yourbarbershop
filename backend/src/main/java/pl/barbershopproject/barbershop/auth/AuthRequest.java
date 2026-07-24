package pl.barbershopproject.barbershop.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request payload for standard user email/password login")
public record AuthRequest(
        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @Schema(description = "User's account password", example = "Secret123!")
        @NotBlank(message = "Hasło jest wymagane")
        @Size(min = 8, max = 32, message = "Hasło musi mieć od {min} do {max} znaków")
        String password
) {
}