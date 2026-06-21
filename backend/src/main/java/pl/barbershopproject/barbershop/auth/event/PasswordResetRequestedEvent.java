package pl.barbershopproject.barbershop.auth.event;

public record PasswordResetRequestedEvent(
        String email,
        String firstname,
        String resetUrl,
        int expirationMinutes
) {
}
