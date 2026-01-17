package pl.barbershopproject.barbershop.user.dto;

import pl.barbershopproject.barbershop.user.Role;

public record UserResponseDTO(
        Long idUser,
        String firstname,
        String lastname,
        String email,
        Role role
) {}
