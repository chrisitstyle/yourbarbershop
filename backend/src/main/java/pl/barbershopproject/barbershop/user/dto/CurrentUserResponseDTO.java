package pl.barbershopproject.barbershop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;

@Schema(description = "Details of the currently authenticated user session")
public record CurrentUserResponseDTO(
        @Schema(description = "Unique user ID", example = "1")
        Long id,

        @Schema(description = "User's first name", example = "John")
        String firstname,

        @Schema(description = "User's last name", example = "Doe")
        String lastname,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's role in the system", example = "USER")
        Role role
) {
    public static CurrentUserResponseDTO from(User user) {
        return new CurrentUserResponseDTO(
                user.getIdUser(),
                user.getFirstname(),
                user.getLastname(),
                user.getEmail(),
                user.getRole()
        );
    }
}
