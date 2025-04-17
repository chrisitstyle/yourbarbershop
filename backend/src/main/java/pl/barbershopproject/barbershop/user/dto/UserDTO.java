package pl.barbershopproject.barbershop.user.dto;

import pl.barbershopproject.barbershop.user.Role;

import java.util.List;

public record UserDTO(
        long idUser,
        String firstname,
        String lastname,
        String email,
        Role role,
        List<UserOrdersDTO> userOrders
) {}
