package pl.barbershopproject.barbershop.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.user.Role;

@Schema(description = "Response payload containing authentication tokens and basic user info")
public record AuthResponse(
        @Schema(description = "JWT Access Token used for bearer authentication", example = "eyJhbGciOiJIUzI1NiJ9...")
        String accessToken,

        @Schema(description = "Unique identifier of the authenticated user", example = "1")
        Long id,

        @Schema(description = "Role assigned to the user", example = "USER")
        Role role
) {
}
