package pl.barbershopproject.barbershop.auth.otp;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

@Schema(description = "Request payload for verifying a received email OTP login code")
public record EmailLoginCodeVerifyRequest(
        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Niepoprawny format adresu email")
        String email,

        @Schema(description = "One-time 6-digit login code", example = "123456")
        @NotBlank(message = "Kod jest wymagany")
        @Pattern(regexp = "\\d{6}", message = "Code must contain 6 digits")
        String code
) {
}
