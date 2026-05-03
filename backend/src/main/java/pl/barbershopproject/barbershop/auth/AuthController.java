package pl.barbershopproject.barbershop.auth;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import pl.barbershopproject.barbershop.annotation.RateLimited;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @RateLimited(limit = 3, timeWindowSeconds = 3600)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@RequestBody @Valid RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @RateLimited(timeWindowSeconds = 300)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody @Valid AuthRequest request) {
        return ResponseEntity.ok(authService.authenticate(request));
    }

    @RateLimited(limit = 3, timeWindowSeconds = 900)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Link do resetowania hasła został wysłany na podany adres email.");
    }

    @RateLimited(timeWindowSeconds = 900)
    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request.getToken(), request.getNewPassword());
        return ResponseEntity.ok("Hasło zostało pomyślnie zresetowane.");
    }

}
