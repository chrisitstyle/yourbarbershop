package pl.barbershopproject.barbershop.auth;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.InvalidPasswordTokenException;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.email.EmailSenderService;

import java.time.Instant;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailSenderService emailSenderService;
    private final PasswordResetTokenRepository passwordResetTokenRepository;

    public AuthResponse register(User user) {

        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new EmailAlreadyExistsException("Użytkownik o podanym adresie e-mail już istnieje");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole(Role.USER);
        }
        userRepository.save(user);
        var token = jwtService.generateToken(user);

        // Zwracanie id, roli i tokena użytkownika
        return AuthResponse.builder()
                .token(token)
                .id(user.getIdUser())
                .role(user.getRole())
                .build();
    }

    public AuthResponse authenticate(AuthRequest request) {
        return authenticate(request.getEmail(), request.getPassword());
    }

    public AuthResponse authenticate(@NotNull String email, @NotNull String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        var user = userRepository.findByEmail(email).orElseThrow();
        var token = jwtService.generateToken(user);

        // Zwracanie id, roli i tokena użytkownika
        return AuthResponse.builder()
                .token(token)
                .id(user.getIdUser())
                .role(user.getRole())
                .build();
    }
    public void forgotPassword(String email) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
        String token = UUID.randomUUID().toString();
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(Instant.now().plusSeconds(1800)); // 30 minutes expiry
        passwordResetTokenRepository.save(passwordResetToken);

        String resetLink = "http://localhost:3000/resetpassword?token=" + token;
        emailSenderService.sendEmail(email, "Reset your password link", resetLink + " \n\n Link expire after 30 minutes");
    }

    public void resetPassword(String token, String newPassword) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidPasswordTokenException("Invalid token"));

        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            throw new InvalidPasswordTokenException("Token expired");
        }

        User user = passwordResetToken.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // delete the token after use
        passwordResetTokenRepository.delete(passwordResetToken);
    }

}
