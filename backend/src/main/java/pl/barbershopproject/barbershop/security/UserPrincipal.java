package pl.barbershopproject.barbershop.security;

import lombok.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;

import java.util.Collection;
import java.util.List;
import java.util.Objects;

public record UserPrincipal(
        long userId,
        String email,
        String passwordHash,
        Role role
) implements UserDetails {

    public static UserPrincipal from(User user) {
        return new UserPrincipal(
                Objects.requireNonNull(user.getIdUser(), "User id must not be null"),
                user.getEmail(),
                user.getPassword(),
                user.getRole()
        );
    }

    @Override
    public @NonNull Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(role.name()));
    }

    @Override
    public String getPassword() {
        return passwordHash;
    }

    @Override
    public @NonNull String getUsername() {
        return email;
    }
}
