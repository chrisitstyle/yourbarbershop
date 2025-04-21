package pl.barbershopproject.barbershop.user.dto;

import jakarta.validation.constraints.NotBlank;

public record UserCreationDTO(
        @NotBlank String firstname,
        @NotBlank String lastname,
        @NotBlank String email,
        @NotBlank String password,
        @NotBlank String role){




}
