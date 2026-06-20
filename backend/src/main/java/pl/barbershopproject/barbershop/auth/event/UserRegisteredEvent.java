package pl.barbershopproject.barbershop.auth.event;

public record UserRegisteredEvent(
        String email,
        String firstname
) {
}
