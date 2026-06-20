package pl.barbershopproject.barbershop.auth.otp;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * Request body used to start passwordless login with a one-time email code.
 */
public record EmailLoginCodeRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        String email
) {
}
