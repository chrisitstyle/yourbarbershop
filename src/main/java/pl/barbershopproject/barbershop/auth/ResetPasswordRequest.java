package pl.barbershopproject.barbershop.auth;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class ResetPasswordRequest {
    @NotEmpty(message = "Token nie może być pusty")
    private String token;

    @NotEmpty(message = "Nowe hasło nie może być puste")
    private String newPassword;
}
