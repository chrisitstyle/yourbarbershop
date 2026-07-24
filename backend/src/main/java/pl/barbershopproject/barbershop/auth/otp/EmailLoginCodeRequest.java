package pl.barbershopproject.barbershop.auth.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to start passwordless login with a one-time email code.
 */
@Schema(description = "Request payload for requesting a one-time login code via email")
public record EmailLoginCodeRequest(
        @Schema(description = "Email address associated with the account", example = "john.doe@example.com")
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email
) {
}
