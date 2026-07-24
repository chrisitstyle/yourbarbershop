package pl.barbershopproject.barbershop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Request payload for updating user personal information")
public record UserProfileUpdateRequestDTO(
        @Schema(description = "Updated first name", example = "John")
        @NotBlank(message = "Firstname jest wymagane")
        String firstname,

        @Schema(description = "Updated last name", example = "Doe")
        @NotBlank(message = "Lastname jest wymagane")
        String lastname,

        @Schema(description = "Updated email address", example = "john.doe.updated@example.com")
        @NotBlank(message = "Email jest wymagany")
        @Email(message = "Email powinien byc poprawny")
        String email
) {
}
