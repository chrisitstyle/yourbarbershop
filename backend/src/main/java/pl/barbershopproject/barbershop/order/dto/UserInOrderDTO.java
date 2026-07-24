package pl.barbershopproject.barbershop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;

@Schema(description = "Embedded user information inside an order object")
public record UserInOrderDTO(
        @Schema(description = "User ID", example = "1")
        Long idUser,

        @Schema(description = "User first name", example = "John")
        String firstname,

        @Schema(description = "User last name", example = "Doe")
        String lastname,

        @Schema(description = "User email address", example = "john.doe@example.com")
        String email
) implements Serializable {
}
