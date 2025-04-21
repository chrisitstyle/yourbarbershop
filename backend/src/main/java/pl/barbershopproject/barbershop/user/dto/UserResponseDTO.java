package pl.barbershopproject.barbershop.user.dto;

import pl.barbershopproject.barbershop.user.Role;

public record UserResponseDTO(
        Long idUser,
        String email,
        String firstname,
        String lastname,
        Role role
) {}
