package pl.barbershopproject.barbershop.auth;

import pl.barbershopproject.barbershop.user.Role;

public record AuthResponse(
        String accessToken,
        Long id,
        Role role
) {
}
