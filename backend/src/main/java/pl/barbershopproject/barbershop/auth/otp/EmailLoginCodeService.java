package pl.barbershopproject.barbershop.auth.otp;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.auth.AuthResult;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.exception.InvalidEmailLoginCodeException;
import pl.barbershopproject.barbershop.security.UserPrincipal;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class EmailLoginCodeService {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(10);

    private static final int MAX_VERIFY_ATTEMPTS = 5;

    private static final String CODE_KEY_PREFIX = "auth:email-login-code:";
    private static final String ATTEMPTS_KEY_PREFIX = "auth:email-login-attempts:";
    private static final String REQUEST_COOLDOWN_KEY_PREFIX = "auth:email-login-request-cooldown:";

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final UserRepository userRepository;
    private final StringRedisTemplate stringRedisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final EmailSenderService emailSenderService;
    private final JwtService jwtService;

    public void requestCode(EmailLoginCodeRequest request) {
        String email = normalizeEmail(request.email());

        /*
         * The cooldown is always set, even for non-existing accounts.
         * This prevents the endpoint from revealing whether an email exists in the database.
         */
        boolean canSendCode = Boolean.TRUE.equals(
                stringRedisTemplate.opsForValue().setIfAbsent(
                        buildCooldownKey(email),
                        "1",
                        REQUEST_COOLDOWN
                )
        );

        if (!canSendCode) {
            return;
        }

        User user = userRepository.findByEmailIgnoreCase(email).orElse(null);

        if (user == null) {
            return;
        }

        String code = generateCode();
        String codeHash = Objects.requireNonNull(
                passwordEncoder.encode(code),
                "Encoded login code must not be null"
        );

        stringRedisTemplate.opsForValue().set(
                buildCodeKey(email),
                codeHash,
                CODE_TTL
        );

        stringRedisTemplate.delete(buildAttemptsKey(email));

        int expirationMinutes = (int) CODE_TTL.toMinutes();

        emailSenderService.sendHtmlEmail(
                user.getEmail(),
                LoginCodeEmailTemplate.subject(),
                LoginCodeEmailTemplate.plainText(code, expirationMinutes),
                LoginCodeEmailTemplate.html(code, expirationMinutes)
        );
    }

    public AuthResult verifyCode(EmailLoginCodeVerifyRequest request) {
        String email = normalizeEmail(request.email());

        String codeKey = buildCodeKey(email);
        String attemptsKey = buildAttemptsKey(email);

        String storedCodeHash = stringRedisTemplate.opsForValue().get(codeKey);

        if (storedCodeHash == null) {
            throw invalidCodeException();
        }

        long attempts = incrementAttempts(attemptsKey);

        if (attempts > MAX_VERIFY_ATTEMPTS) {
            stringRedisTemplate.delete(List.of(codeKey, attemptsKey));
            throw invalidCodeException();
        }

        if (!passwordEncoder.matches(request.code(), storedCodeHash)) {
            throw invalidCodeException();
        }

        /*
         * The code is removed after successful verification.
         * delete(...) returns true only if the key actually existed.
         * This ensures that if two valid requests are sent at the same time,
         * only the first one can consume the code.
         */
        boolean codeConsumed = Boolean.TRUE.equals(stringRedisTemplate.delete(codeKey));

        if (!codeConsumed) {
            throw invalidCodeException();
        }

        stringRedisTemplate.delete(attemptsKey);

        User user = userRepository.findByEmailIgnoreCase(email)
                .orElseThrow(this::invalidCodeException);

        String accessToken = jwtService.generateAccessToken(UserPrincipal.from(user));

        return new AuthResult(accessToken, user);
    }

    private long incrementAttempts(String attemptsKey) {
        Long attempts = stringRedisTemplate.opsForValue().increment(attemptsKey);

        if (attempts == null) {
            return MAX_VERIFY_ATTEMPTS + 1L;
        }

        if (attempts == 1L) {
            stringRedisTemplate.expire(attemptsKey, ATTEMPTS_TTL);
        }

        return attempts;
    }

    private String generateCode() {
        return String.format("%06d", SECURE_RANDOM.nextInt(1_000_000));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private String buildCodeKey(String email) {
        return CODE_KEY_PREFIX + hashEmail(email);
    }

    private String buildAttemptsKey(String email) {
        return ATTEMPTS_KEY_PREFIX + hashEmail(email);
    }

    private String buildCooldownKey(String email) {
        return REQUEST_COOLDOWN_KEY_PREFIX + hashEmail(email);
    }

    private String hashEmail(String email) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(email.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }

    private InvalidEmailLoginCodeException invalidCodeException() {
        return new InvalidEmailLoginCodeException("Invalid or expired login code");
    }
}