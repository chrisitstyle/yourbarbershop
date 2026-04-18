package pl.barbershopproject.barbershop.order.dto;

import java.io.Serializable;

public record UserInOrderDTO(
        Long idUser,
        String firstname,
        String lastname,
        String email
) implements Serializable {
}
