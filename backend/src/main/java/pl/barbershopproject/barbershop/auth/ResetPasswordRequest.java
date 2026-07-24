package pl.barbershopproject.barbershop.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
@Schema(description = "Request payload for setting a new password using a reset token")
public class ResetPasswordRequest {

    @Schema(description = "One-time password reset token received via email", example = "a1b2c3d4-e5f6-7890-abcd-1234567890ab")
    @NotBlank(message = "Token nie może być pusty")
    private String token;

    @Schema(description = "New password to set for the account", example = "NewSecret123!")
    @NotBlank(message = "Nowe hasło nie może być puste")
    private String newPassword;

    @Schema(description = "Confirmation of the new password", example = "NewSecret123!")
    @NotBlank(message = "Potwierdzenie hasła nie może być puste")
    private String confirmPassword;
}
