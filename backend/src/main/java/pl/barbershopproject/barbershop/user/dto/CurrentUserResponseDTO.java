package pl.barbershopproject.barbershop.user.dto;

import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;

public record CurrentUserResponseDTO(
        Long id,
        String firstname,
        String lastname,
        String email,
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
