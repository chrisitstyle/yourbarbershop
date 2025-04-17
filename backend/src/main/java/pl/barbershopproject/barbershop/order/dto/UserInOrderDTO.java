package pl.barbershopproject.barbershop.order.dto;

public record UserInOrderDTO(
        long idUser,
        String firstname,
        String lastname,
        String email
) {
}
