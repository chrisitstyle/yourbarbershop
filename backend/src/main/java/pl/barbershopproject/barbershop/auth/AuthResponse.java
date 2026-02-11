package pl.barbershopproject.barbershop.auth;

import pl.barbershopproject.barbershop.user.Role;

public record AuthResponse(
        String token,
        Long id,
        Role role
) {
}
