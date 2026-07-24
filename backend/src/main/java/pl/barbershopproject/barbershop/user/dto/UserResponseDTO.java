package pl.barbershopproject.barbershop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.user.Role;

@Schema(description = "Response payload after creating or updating a user")
public record UserResponseDTO(
        @Schema(description = "Unique identifier of the newly created user", example = "2")
        Long idUser,

        @Schema(description = "User's first name", example = "Jan")
        String firstname,

        @Schema(description = "User's last name", example = "Kowalski")
        String lastname,

        @Schema(description = "User's email address", example = "jan.kowalski@example.com")
        String email,

        @Schema(description = "Role assigned to the user", example = "USER")
        Role role
) {}
