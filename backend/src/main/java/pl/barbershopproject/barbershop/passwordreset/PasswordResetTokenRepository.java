package pl.barbershopproject.barbershop.passwordreset;

import org.springframework.data.jpa.repository.JpaRepository;
import pl.barbershopproject.barbershop.user.User;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
    void deleteByUser(User user);
}
