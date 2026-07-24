package pl.barbershopproject.barbershop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.user.Role;

import java.util.List;

@Schema(description = "Detailed user profile including historical reservations")
public record UserDTO(
        @Schema(description = "Unique user ID", example = "1")
        Long idUser,

        @Schema(description = "User's first name", example = "John")
        String firstname,

        @Schema(description = "User's last name", example = "Doe")
        String lastname,

        @Schema(description = "User's email address", example = "john.doe@example.com")
        String email,

        @Schema(description = "User's system role", example = "ADMIN")
        Role role,

        @Schema(description = "List of user's past and active orders")
        List<UserOrdersDTO> userOrders
) {}
