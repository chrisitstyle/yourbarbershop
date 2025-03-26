package pl.barbershopproject.barbershop.auth;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class ForgotPasswordRequest {
    @Email(message = "Niepoprawny format adresu email")
    private String email;
}
