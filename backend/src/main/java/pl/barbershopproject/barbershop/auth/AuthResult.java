package pl.barbershopproject.barbershop.auth;

import pl.barbershopproject.barbershop.user.User;

public record AuthResult(
        String accessToken,
        User user
) {
    public AuthResponse toResponse() {
        return new AuthResponse(
                accessToken,
                user.getIdUser(),
                user.getRole()
        );
    }
}
