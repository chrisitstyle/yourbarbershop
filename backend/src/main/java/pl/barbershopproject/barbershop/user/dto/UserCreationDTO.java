package pl.barbershopproject.barbershop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Data transfer object for creating a new user profile by an admin")
public record UserCreationDTO(
        @Schema(description = "User's first name", example = "John")
        @NotBlank String firstname,

        @Schema(description = "User's last name", example = "Doe")
        @NotBlank String lastname,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        @NotBlank String email,

        @Schema(description = "Account password", example = "test1234")
        @NotBlank String password,

        @Schema(description = "Assigned user role", example = "USER")
        @NotBlank String role
) {
}
