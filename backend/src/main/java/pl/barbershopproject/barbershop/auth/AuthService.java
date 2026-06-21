package pl.barbershopproject.barbershop.auth;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;
import pl.barbershopproject.barbershop.auth.event.PasswordResetRequestedEvent;
import pl.barbershopproject.barbershop.auth.event.UserRegisteredEvent;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.InvalidPasswordTokenException;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class AuthService {

    private static final int PASSWORD_RESET_EXPIRATION_MINUTES = 30;
    private static final int PASSWORD_RESET_TOKEN_BYTES = 32;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final CaptchaService captchaService;
    private final ApplicationEventPublisher eventPublisher;

    public AuthResponse register(RegisterRequest request) {
        captchaService.verify(request.captchaToken());

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyExistsException("Użytkownik o podanym adresie e-mail już istnieje");
        }

        var user = User.builder()
                .firstname(request.firstname())
                .lastname(request.lastname())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .build();

        userRepository.save(user);

        eventPublisher.publishEvent(
                new UserRegisteredEvent(user.getEmail(), user.getFirstname())
        );

        var token = jwtService.generateToken(user);

        // return id, role and user token
        return new AuthResponse(token, user.getIdUser(), user.getRole());
    }

    public AuthResponse authenticate(AuthRequest request) {
        return authenticate(request.email(), request.password());
    }

    public AuthResponse authenticate(@NotNull String email, @NotNull String password) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password)
        );

        var user = userRepository.findByEmail(email).orElseThrow();
        var token = jwtService.generateToken(user);

        // return id, role and user token
        return new AuthResponse(token, user.getIdUser(), user.getRole());
    }

    @Transactional
    public void forgotPassword(ForgotPasswordRequest request) {
        captchaService.verify(request.captchaToken());

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String rawToken = generateSecureToken();
            String hashedToken = hashToken(rawToken);

            // only the newest reset link should stay valid
            passwordResetTokenRepository.deleteByUser(user);

            PasswordResetToken passwordResetToken = new PasswordResetToken();

            passwordResetToken.setToken(hashedToken);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(
                    Instant.now().plusSeconds(PASSWORD_RESET_EXPIRATION_MINUTES * 60L)
            );

            passwordResetTokenRepository.save(passwordResetToken);

            String resetLink = "http://localhost:3000/resetpassword?token=" + rawToken;

            eventPublisher.publishEvent(
                    new PasswordResetRequestedEvent(
                            user.getEmail(),
                            user.getFirstname(),
                            resetLink,
                            PASSWORD_RESET_EXPIRATION_MINUTES
                    )
            );
        });
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        if (!Objects.equals(request.getNewPassword(), request.getConfirmPassword())) {
            throw new IllegalArgumentException("Hasła nie są takie same.");
        }

        String hashedToken = hashToken(request.getToken());

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(hashedToken)
                .orElseThrow(() -> new InvalidPasswordTokenException("Invalid token"));

        if (passwordResetToken.getExpiryDate().isBefore(Instant.now())) {
            passwordResetTokenRepository.delete(passwordResetToken);
            throw new InvalidPasswordTokenException("Token expired");
        }

        User user = passwordResetToken.getUser();

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);

        // delete the token after use
        passwordResetTokenRepository.delete(passwordResetToken);
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[PASSWORD_RESET_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}