package pl.barbershopproject.barbershop.auth.refresh;


import pl.barbershopproject.barbershop.user.User;

public record RefreshTokenRotation(
        User user,
        String newRefreshToken
) {
}
