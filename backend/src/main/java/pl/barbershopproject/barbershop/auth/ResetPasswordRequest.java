package pl.barbershopproject.barbershop.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordRequest {

    @NotBlank(message = "Token nie może być pusty")
    private String token;

    @NotBlank(message = "Nowe hasło nie może być puste")
    private String newPassword;

    @NotBlank(message = "Potwierdzenie hasła nie może być puste")
    private String confirmPassword;
}
