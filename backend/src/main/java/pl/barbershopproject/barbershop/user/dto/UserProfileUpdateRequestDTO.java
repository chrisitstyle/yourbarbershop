package pl.barbershopproject.barbershop.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserProfileUpdateRequestDTO(
        @NotBlank(message = "Firstname jest wymagane")
        String firstname,

        @NotBlank(message = "Lastname jest wymagane")
        String lastname,

        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Email powinien byc poprawny")
        String email
) {
}
