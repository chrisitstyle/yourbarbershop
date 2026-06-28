package pl.barbershopproject.barbershop.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.annotation.RateLimited;
import pl.barbershopproject.barbershop.auth.otp.EmailLoginCodeRequest;
import pl.barbershopproject.barbershop.auth.otp.EmailLoginCodeService;
import pl.barbershopproject.barbershop.auth.otp.EmailLoginCodeVerifyRequest;
import pl.barbershopproject.barbershop.auth.refresh.RefreshCookieService;
import pl.barbershopproject.barbershop.auth.refresh.RefreshTokenRotation;
import pl.barbershopproject.barbershop.auth.refresh.RefreshTokenService;
import pl.barbershopproject.barbershop.config.JwtService;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailLoginCodeService emailLoginCodeService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshCookieService refreshCookieService;
    private final JwtService jwtService;

    @RateLimited(limit = 3, timeWindowSeconds = 3600)
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @RequestBody @Valid RegisterRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthResult result = authService.register(request);
        return issueLoginResponse(result, servletRequest, servletResponse);
    }

    @RateLimited(timeWindowSeconds = 300)
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @RequestBody @Valid AuthRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthResult result = authService.authenticate(request);
        return issueLoginResponse(result, servletRequest, servletResponse);
    }

    @RateLimited(limit = 3, timeWindowSeconds = 900)
    @PostMapping("/login/email-code/request")
    public ResponseEntity<String> requestEmailLoginCode(
            @RequestBody @Valid EmailLoginCodeRequest request
    ) {
        emailLoginCodeService.requestCode(request);
        return ResponseEntity.ok(
                "Jeśli konto istnieje, kod zostanie wysłany na podany adres e-mail."
        );
    }

    @RateLimited(timeWindowSeconds = 900)
    @PostMapping("/login/email-code/verify")
    public ResponseEntity<AuthResponse> verifyEmailLoginCode(
            @RequestBody @Valid EmailLoginCodeVerifyRequest request,
            HttpServletRequest servletRequest,
            HttpServletResponse servletResponse
    ) {
        AuthResult result = emailLoginCodeService.verifyCode(request);
        return issueLoginResponse(result, servletRequest, servletResponse);
    }

    @PostMapping("/auth/refresh")
    public ResponseEntity<AuthResponse> refresh(
            HttpServletRequest request,
            HttpServletResponse response,
            @CookieValue(name = RefreshCookieService.REFRESH_COOKIE_NAME, required = false)
            String refreshToken
    ) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing refresh token");
        }

        RefreshTokenRotation rotation = refreshTokenService.rotateRefreshToken(refreshToken, request);

        refreshCookieService.addRefreshCookie(response, rotation.newRefreshToken());

        String newAccessToken = jwtService.generateAccessToken(rotation.user());

        return ResponseEntity.ok(
                new AuthResponse(
                        newAccessToken,
                        rotation.user().getIdUser(),
                        rotation.user().getRole()
                )
        );
    }

    @PostMapping("/auth/logout")
    public ResponseEntity<Void> logout(
            HttpServletResponse response,
            @CookieValue(name = RefreshCookieService.REFRESH_COOKIE_NAME, required = false)
            String refreshToken
    ) {
        if (refreshToken != null && !refreshToken.isBlank()) {
            refreshTokenService.revokeRefreshToken(refreshToken);
        }

        refreshCookieService.clearRefreshCookie(response);

        return ResponseEntity.noContent().build();
    }

    @RateLimited(limit = 3, timeWindowSeconds = 900)
    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request);
        return ResponseEntity.ok("Link do resetowania hasła został wysłany na podany adres email.");
    }

    @RateLimited(timeWindowSeconds = 900)
    @PostMapping("/reset-password")
    public String resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return "Hasło zostało pomyślnie zresetowane.";
    }

    private ResponseEntity<AuthResponse> issueLoginResponse(
            AuthResult result,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String refreshToken = refreshTokenService.createRefreshToken(result.user(), request);
        refreshCookieService.addRefreshCookie(response, refreshToken);

        return ResponseEntity.ok(result.toResponse());
    }
}