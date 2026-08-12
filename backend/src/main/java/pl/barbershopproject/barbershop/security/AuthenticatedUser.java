package pl.barbershopproject.barbershop.security;

import pl.barbershopproject.barbershop.user.Role;

public record AuthenticatedUser(
        Long userId,
        Role role) { }
