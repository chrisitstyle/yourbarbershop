package pl.barbershopproject.barbershop.order.dto;

public record UserInOrderDTO(
        Long idUser,
        String firstname,
        String lastname,
        String email
) {
}
