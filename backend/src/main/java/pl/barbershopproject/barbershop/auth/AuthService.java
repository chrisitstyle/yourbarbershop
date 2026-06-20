package pl.barbershopproject.barbershop.auth;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;
import pl.barbershopproject.barbershop.auth.event.UserRegisteredEvent;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.InvalidPasswordTokenException;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

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
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
        var user = userRepository.findByEmail(email).orElseThrow();
        var token = jwtService.generateToken(user);

        // return id, role and user token
        return new AuthResponse(token, user.getIdUser(), user.getRole());
    }

    public void forgotPassword(ForgotPasswordRequest request) {
        captchaService.verify(request.captchaToken());

        userRepository.findByEmail(request.email()).ifPresent(user -> {
            String token = UUID.randomUUID().toString();

            PasswordResetToken passwordResetToken = new PasswordResetToken();

            passwordResetToken.setToken(token);
            passwordResetToken.setUser(user);
            passwordResetToken.setExpiryDate(Instant.now().plusSeconds(1800)); // 30 minutes expiry

            passwordResetTokenRepository.save(passwordResetToken);

            String resetLink = "http://localhost:3000/resetpassword?token=" + token;

            emailSenderService.sendEmail(
                    user.getEmail(),
                    "Reset your password link",
                    resetLink + " \n\n Link expire after 30 minutes"
            );
        });
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
