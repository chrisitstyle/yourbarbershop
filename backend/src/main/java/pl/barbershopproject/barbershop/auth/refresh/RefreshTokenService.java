package pl.barbershopproject.barbershop.auth.refresh;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.user.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();
    private static final int REFRESH_TOKEN_BYTES = 64;

    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${application.security.refresh-token-days:14}")
    private long refreshTokenDays;

    @Transactional
    public String createRefreshToken(User user, HttpServletRequest request) {
        String rawToken = generateSecureToken();
        String tokenHash = hashToken(rawToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .tokenHash(tokenHash)
                .user(user)
                .expiresAt(Instant.now().plus(Duration.ofDays(refreshTokenDays)))
                .userAgent(request.getHeader("User-Agent"))
                .ipAddress(resolveClientIp(request))
                .build();

        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    @Transactional
    public RefreshTokenRotation rotateRefreshToken(String rawRefreshToken, HttpServletRequest request) {
        String oldTokenHash = hashToken(rawRefreshToken);

        RefreshToken oldToken = refreshTokenRepository.findByTokenHash(oldTokenHash)
                .orElseThrow(() -> unauthorized("Invalid refresh token"));

        if (oldToken.isRevoked() || oldToken.isExpired()) {
            throw unauthorized("Refresh token expired or revoked");
        }

        oldToken.setRevokedAt(Instant.now());

        String newRawToken = createRefreshToken(oldToken.getUser(), request);
        oldToken.setReplacedByTokenHash(hashToken(newRawToken));

        refreshTokenRepository.save(oldToken);

        return new RefreshTokenRotation(oldToken.getUser(), newRawToken);
    }

    @Transactional
    public void revokeRefreshToken(String rawRefreshToken) {
        String tokenHash = hashToken(rawRefreshToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            if (!token.isRevoked()) {
                token.setRevokedAt(Instant.now());
                refreshTokenRepository.save(token);
            }
        });
    }

    private String generateSecureToken() {
        byte[] randomBytes = new byte[REFRESH_TOKEN_BYTES];
        SECURE_RANDOM.nextBytes(randomBytes);

        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(randomBytes);
    }

    private String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");

        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }

        return request.getRemoteAddr();
    }

    private ResponseStatusException unauthorized(String message) {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED, message);
    }
}
