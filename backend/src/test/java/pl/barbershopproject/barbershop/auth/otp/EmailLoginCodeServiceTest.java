package pl.barbershopproject.barbershop.auth.otp;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.barbershopproject.barbershop.auth.AuthResult;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.email.EmailSenderService;
import pl.barbershopproject.barbershop.exception.InvalidEmailLoginCodeException;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailLoginCodeServiceTest {

    private static final Duration CODE_TTL = Duration.ofMinutes(10);
    private static final Duration REQUEST_COOLDOWN = Duration.ofSeconds(60);
    private static final Duration ATTEMPTS_TTL = Duration.ofMinutes(10);

    private static final String EMAIL = "johndoe@example.com";
    private static final String RAW_EMAIL = " JohnDoe@Example.com ";
    private static final String CODE = "123456";
    private static final String CODE_HASH = "hashed-code";
    private static final String ACCESS_TOKEN = "access-token";

    @Mock
    private UserRepository userRepository;

    @Mock
    private StringRedisTemplate stringRedisTemplate;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailSenderService emailSenderService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private EmailLoginCodeService emailLoginCodeService;

    @BeforeEach
    void setUp() {
        when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    void shouldSendLoginCodeWhenUserExistsAndCooldownIsNotActive() {
        User user = createUserForEmailSending();

        when(valueOperations.setIfAbsent(cooldownKey(), "1", REQUEST_COOLDOWN))
                .thenReturn(true);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(user));
        when(passwordEncoder.encode(anyString()))
                .thenReturn(CODE_HASH);

        emailLoginCodeService.requestCode(new EmailLoginCodeRequest(RAW_EMAIL));

        verify(valueOperations).set(codeKey(), CODE_HASH, CODE_TTL);
        verify(stringRedisTemplate).delete(attemptsKey());

        ArgumentCaptor<String> plainTextBodyCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> htmlBodyCaptor = ArgumentCaptor.forClass(String.class);

        verify(emailSenderService).sendHtmlEmail(
                eq(EMAIL),
                eq("YourBarbershop login code"),
                plainTextBodyCaptor.capture(),
                htmlBodyCaptor.capture()
        );

        String plainTextBody = plainTextBodyCaptor.getValue();
        String htmlBody = htmlBodyCaptor.getValue();

        verify(passwordEncoder).encode(anyString());

        assertTrue(plainTextBody.contains("YourBarbershop login code is:"));
        assertTrue(plainTextBody.contains("The code expires in 10 minutes."));
        assertTrue(plainTextBody.contains("If you did not request this code"));

        assertTrue(htmlBody.contains("YourBarbershop"));
        assertTrue(htmlBody.contains("Your login code"));
        assertTrue(htmlBody.contains("10 minutes"));
        assertTrue(htmlBody.contains("<!doctype html>"));
    }

    @Test
    void shouldNotSendLoginCodeWhenCooldownIsActive() {
        when(valueOperations.setIfAbsent(cooldownKey(), "1", REQUEST_COOLDOWN))
                .thenReturn(false);

        emailLoginCodeService.requestCode(new EmailLoginCodeRequest(RAW_EMAIL));

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(emailSenderService);
    }

    @Test
    void shouldNotSendLoginCodeWhenUserDoesNotExist() {
        when(valueOperations.setIfAbsent(cooldownKey(), "1", REQUEST_COOLDOWN))
                .thenReturn(true);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.empty());

        emailLoginCodeService.requestCode(new EmailLoginCodeRequest(RAW_EMAIL));

        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(emailSenderService);
        verify(valueOperations, never()).set(eq(codeKey()), anyString(), eq(CODE_TTL));
    }

    @Test
    void shouldVerifyCodeAndReturnAuthResult() {
        User user = createUserForSuccessfulLogin();

        when(valueOperations.get(codeKey()))
                .thenReturn(CODE_HASH);
        when(valueOperations.increment(attemptsKey()))
                .thenReturn(1L);
        when(passwordEncoder.matches(CODE, CODE_HASH))
                .thenReturn(true);
        when(stringRedisTemplate.delete(codeKey()))
                .thenReturn(true);
        when(userRepository.findByEmailIgnoreCase(EMAIL))
                .thenReturn(Optional.of(user));
        when(jwtService.generateAccessToken(user))
                .thenReturn(ACCESS_TOKEN);

        AuthResult result = emailLoginCodeService.verifyCode(
                new EmailLoginCodeVerifyRequest(RAW_EMAIL, CODE)
        );

        assertEquals(ACCESS_TOKEN, result.accessToken());
        assertEquals(user, result.user());
        assertEquals(1L, result.user().getIdUser());
        assertEquals(Role.USER, result.user().getRole());

        verify(stringRedisTemplate).delete(codeKey());
        verify(stringRedisTemplate).delete(attemptsKey());
        verify(jwtService).generateAccessToken(user);
    }

    @Test
    void shouldThrowExceptionWhenCodeDoesNotExist() {
        when(valueOperations.get(codeKey()))
                .thenReturn(null);

        EmailLoginCodeVerifyRequest request =
                new EmailLoginCodeVerifyRequest(RAW_EMAIL, CODE);

        assertThrows(
                InvalidEmailLoginCodeException.class,
                () -> emailLoginCodeService.verifyCode(request)
        );

        verify(valueOperations, never()).increment(anyString());
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldThrowExceptionWhenCodeIsInvalid() {
        when(valueOperations.get(codeKey()))
                .thenReturn(CODE_HASH);
        when(valueOperations.increment(attemptsKey()))
                .thenReturn(1L);
        when(passwordEncoder.matches(CODE, CODE_HASH))
                .thenReturn(false);

        EmailLoginCodeVerifyRequest request =
                new EmailLoginCodeVerifyRequest(RAW_EMAIL, CODE);

        assertThrows(
                InvalidEmailLoginCodeException.class,
                () -> emailLoginCodeService.verifyCode(request)
        );

        verify(stringRedisTemplate, never()).delete(codeKey());
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldDeleteCodeAndAttemptsWhenMaxAttemptsExceeded() {
        when(valueOperations.get(codeKey()))
                .thenReturn(CODE_HASH);
        when(valueOperations.increment(attemptsKey()))
                .thenReturn(6L);

        EmailLoginCodeVerifyRequest request =
                new EmailLoginCodeVerifyRequest(RAW_EMAIL, CODE);

        assertThrows(
                InvalidEmailLoginCodeException.class,
                () -> emailLoginCodeService.verifyCode(request)
        );

        verify(stringRedisTemplate).delete(List.of(codeKey(), attemptsKey()));
        verifyNoInteractions(passwordEncoder);
        verifyNoInteractions(jwtService);
    }

    @Test
    void shouldSetAttemptsTtlWhenFirstAttemptIsCreated() {
        when(valueOperations.increment(attemptsKey()))
                .thenReturn(1L);

        long attempts = invokeIncrementAttempts();

        assertEquals(1L, attempts);
        verify(stringRedisTemplate).expire(attemptsKey(), ATTEMPTS_TTL);
    }

    @Test
    void shouldReturnExceededAttemptsWhenRedisIncrementReturnsNull() {
        when(valueOperations.increment(attemptsKey()))
                .thenReturn(null);

        long attempts = invokeIncrementAttempts();

        assertEquals(6L, attempts);
    }

    private long invokeIncrementAttempts() {
        try {
            var method = EmailLoginCodeService.class.getDeclaredMethod(
                    "incrementAttempts",
                    String.class
            );
            method.setAccessible(true);

            return (long) method.invoke(emailLoginCodeService, attemptsKey());
        } catch (Exception ex) {
            throw new IllegalStateException("Could not invoke incrementAttempts", ex);
        }
    }

    private User createUserForEmailSending() {
        User user = org.mockito.Mockito.mock(User.class);

        when(user.getEmail()).thenReturn(EMAIL);

        return user;
    }

    private User createUserForSuccessfulLogin() {
        return User.builder()
                .idUser(1L)
                .firstname("John")
                .lastname("Doe")
                .email(EMAIL)
                .password("password")
                .role(Role.USER)
                .build();
    }

    private static String codeKey() {
        return "auth:email-login-code:" + hashedEmail();
    }

    private static String attemptsKey() {
        return "auth:email-login-attempts:" + hashedEmail();
    }

    private static String cooldownKey() {
        return "auth:email-login-request-cooldown:" + hashedEmail();
    }

    private static String hashedEmail() {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(EMAIL.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 algorithm is not available", ex);
        }
    }
}