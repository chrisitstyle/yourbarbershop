package pl.barbershopproject.barbershop.auth;


import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.model.Role;
import pl.barbershopproject.barbershop.model.User;
import pl.barbershopproject.barbershop.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

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

}
