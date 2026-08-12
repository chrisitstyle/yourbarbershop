package pl.barbershopproject.barbershop.auth;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import pl.barbershopproject.barbershop.auth.captcha.CaptchaService;
import pl.barbershopproject.barbershop.auth.event.PasswordResetRequestedEvent;
import pl.barbershopproject.barbershop.auth.event.UserRegisteredEvent;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.exception.EmailAlreadyExistsException;
import pl.barbershopproject.barbershop.exception.InvalidPasswordTokenException;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.security.UserPrincipal;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.time.Instant;
import java.util.Optional;

import static com.github.dockerjava.zerodep.shaded.org.apache.commons.codec.digest.DigestUtils.sha256Hex;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static pl.barbershopproject.barbershop.utils.testentities.AuthTestEntities.*;
import static pl.barbershopproject.barbershop.utils.testentities.PasswordResetTokenTestEntities.createPasswordResetToken;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final String ACCESS_TOKEN = "access-token";
    private static final String ENCODED_PASSWORD = "encoded-password";
    private static final String ENCODED_NEW_PASSWORD = "encoded-new-password";
    private static final Instant VALID_PASSWORD_RESET_TOKEN_EXPIRY =
            Instant.parse("2030-01-16T12:05:00Z");
    private static final Instant EXPIRED_PASSWORD_RESET_TOKEN_EXPIRY =
            Instant.parse("2020-01-16T12:00:00Z");

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Mock
    private CaptchaService captchaService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_ReturnsAuthResultAndPublishesEvent_WhenEmailIsFree() {
        // given
        RegisterRequest request = createRegisterRequest();

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode(request.password()))
                .thenReturn(ENCODED_PASSWORD);

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setIdUser(1L);
            return savedUser;
        });

        when(jwtService.generateAccessToken(any(UserPrincipal.class)))
                .thenReturn(ACCESS_TOKEN);

        // when
        AuthResult result = authService.register(request);

        // then
        assertEquals(ACCESS_TOKEN, result.accessToken());
        assertNotNull(result.user());
        assertEquals(1L, result.user().getIdUser());
        assertEquals(request.firstname(), result.user().getFirstname());
        assertEquals(request.lastname(), result.user().getLastname());
        assertEquals(request.email(), result.user().getEmail());
        assertEquals(Role.USER, result.user().getRole());

        verify(captchaService).verify(request.captchaToken());
        verify(userRepository).findByEmail(request.email());

        ArgumentCaptor<User> userCaptor =
                ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(request.firstname(), savedUser.getFirstname());
        assertEquals(request.lastname(), savedUser.getLastname());
        assertEquals(request.email(), savedUser.getEmail());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());

        ArgumentCaptor<UserRegisteredEvent> eventCaptor =
                ArgumentCaptor.forClass(UserRegisteredEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        UserRegisteredEvent event = eventCaptor.getValue();

        assertEquals(request.email(), event.email());
        assertEquals(request.firstname(), event.firstname());

        ArgumentCaptor<UserPrincipal> principalCaptor = ArgumentCaptor.forClass(UserPrincipal.class);

        verify(jwtService).generateAccessToken(principalCaptor.capture());

        UserPrincipal principal = principalCaptor.getValue();

        assertEquals(savedUser.getIdUser(), principal.userId());
        assertEquals(savedUser.getEmail(), principal.email());
        assertEquals(savedUser.getPassword(), principal.passwordHash());
        assertEquals(savedUser.getRole(), principal.role());
    }

    @Test
    void register_ThrowsEmailAlreadyExistsException_WhenEmailAlreadyExists() {
        // given
        RegisterRequest request = createRegisterRequest();
        User existingUser = createUser();

        when(userRepository.findByEmail(request.email()))
                .thenReturn(Optional.of(existingUser));

        // when + then
        assertThrows(
                EmailAlreadyExistsException.class,
                () -> authService.register(request)
        );

        verify(captchaService).verify(request.captchaToken());
        verify(userRepository).findByEmail(request.email());

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(jwtService, never())
                .generateAccessToken(any(UserPrincipal.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void authenticate_ReturnsAuthResult_WhenCredentialsAreValid() {
        // given
        AuthRequest request = createAuthRequest();

        User user = userBuilder()
                .idUser(7L)
                .email(request.email())
                .role(Role.ADMIN)
                .build();

        UserPrincipal principal = createUserPrincipal(user);

        Authentication authentication = mock(Authentication.class);

        when(authentication.getPrincipal())
                .thenReturn(principal);

        when(authenticationManager.authenticate(any()))
                .thenReturn(authentication);

        when(userRepository.findById(principal.userId()))
                .thenReturn(Optional.of(user));

        when(jwtService.generateAccessToken(principal))
                .thenReturn(ACCESS_TOKEN);

        // when
        AuthResult result = authService.authenticate(request);

        // then
        assertEquals(ACCESS_TOKEN, result.accessToken());
        assertEquals(user, result.user());
        assertEquals(7L, result.user().getIdUser());
        assertEquals(Role.ADMIN, result.user().getRole());

        verify(authenticationManager).authenticate(
                argThat(authenticationToken ->
                        request.email().equals(authenticationToken.getPrincipal())
                                && request.password().equals(authenticationToken.getCredentials())
                )
        );

        verify(userRepository).findById(principal.userId());
        verify(jwtService).generateAccessToken(principal);
    }

    @Test
    void forgotPassword_SavesHashedPasswordResetTokenAndPublishesEvent_WhenUserExists() {
        // given
        ForgotPasswordRequest request = createForgotPasswordRequest();

        User user = userBuilder()
                .email(request.email())
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        // when
        authService.forgotPassword(request);

        // then
        verify(captchaService).verify(request.captchaToken());
        verify(userRepository).findByEmail(request.email());
        verify(passwordResetTokenRepository).deleteByUser(user);

        ArgumentCaptor<PasswordResetToken> tokenCaptor =
                ArgumentCaptor.forClass(PasswordResetToken.class);

        verify(passwordResetTokenRepository).save(tokenCaptor.capture());

        PasswordResetToken savedToken = tokenCaptor.getValue();

        assertEquals(user, savedToken.getUser());
        assertNotNull(savedToken.getToken());
        assertTrue(savedToken.getToken().matches("[0-9a-f]{64}"));

        assertNotNull(savedToken.getExpiryDate());

        ArgumentCaptor<PasswordResetRequestedEvent> eventCaptor =
                ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);

        verify(eventPublisher).publishEvent(eventCaptor.capture());

        PasswordResetRequestedEvent event = eventCaptor.getValue();

        assertEquals(user.getEmail(), event.email());
        assertEquals(user.getFirstname(), event.firstname());
        assertEquals(30, event.expirationMinutes());
        assertTrue(event.resetUrl().startsWith("http://localhost:3000/resetpassword?token="));

        String rawTokenFromUrl = event.resetUrl()
                .substring(event.resetUrl().lastIndexOf("=") + 1);

        assertNotEquals(rawTokenFromUrl, savedToken.getToken());
        assertEquals(sha256Hex(rawTokenFromUrl), savedToken.getToken());
    }

    @Test
    void forgotPassword_DoesNotRevealMissingEmail_WhenUserDoesNotExist() {
        // given
        ForgotPasswordRequest request = createForgotPasswordRequest();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());

        // when
        authService.forgotPassword(request);

        // then
        verify(captchaService).verify(request.captchaToken());
        verify(userRepository).findByEmail(request.email());

        verify(passwordResetTokenRepository, never()).deleteByUser(any(User.class));
        verify(passwordResetTokenRepository, never()).save(any(PasswordResetToken.class));
        verify(eventPublisher, never()).publishEvent(any(Object.class));
    }

    @Test
    void resetPassword_UpdatesPasswordAndDeletesToken_WhenTokenIsValid() {
        // given
        String rawToken = "valid-reset-token";
        String hashedToken = sha256Hex(rawToken);
        String newPassword = "newPassword123";

        User user = createUser();

        PasswordResetToken passwordResetToken = createPasswordResetToken(
                user,
                hashedToken,
                VALID_PASSWORD_RESET_TOKEN_EXPIRY
        );

        ResetPasswordRequest request = createResetPasswordRequest(
                rawToken,
                newPassword,
                newPassword
        );

        when(passwordResetTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.of(passwordResetToken));

        when(passwordEncoder.encode(newPassword)).thenReturn(ENCODED_NEW_PASSWORD);

        // when
        authService.resetPassword(request);

        // then
        assertEquals(ENCODED_NEW_PASSWORD, user.getPassword());

        verify(passwordResetTokenRepository).findByToken(hashedToken);
        verify(passwordEncoder).encode(newPassword);
        verify(userRepository).save(user);
        verify(passwordResetTokenRepository).delete(passwordResetToken);
    }

    @Test
    void resetPassword_ThrowsIllegalArgumentException_WhenPasswordsDoNotMatch() {
        // given
        ResetPasswordRequest request = createResetPasswordRequest(
                "raw-reset-token",
                "newPassword123",
                "differentPassword123"
        );

        // when + then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals("Hasła nie są takie same.", exception.getMessage());

        verify(passwordResetTokenRepository, never()).findByToken(anyString());
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordResetTokenRepository, never()).delete(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_ThrowsInvalidPasswordTokenException_WhenTokenDoesNotExist() {
        // given
        String rawToken = "missing-reset-token";
        String hashedToken = sha256Hex(rawToken);

        ResetPasswordRequest request = createResetPasswordRequest(
                rawToken,
                "newPassword123",
                "newPassword123"
        );

        when(passwordResetTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.empty());

        // when + then
        InvalidPasswordTokenException exception = assertThrows(
                InvalidPasswordTokenException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals("Invalid token", exception.getMessage());

        verify(passwordResetTokenRepository).findByToken(hashedToken);
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
        verify(passwordResetTokenRepository, never()).delete(any(PasswordResetToken.class));
    }

    @Test
    void resetPassword_DeletesTokenAndThrowsInvalidPasswordTokenException_WhenTokenIsExpired() {
        // given
        String rawToken = "expired-reset-token";
        String hashedToken = sha256Hex(rawToken);

        User user = createUser();

        PasswordResetToken expiredToken = createPasswordResetToken(
                user,
                hashedToken,
                EXPIRED_PASSWORD_RESET_TOKEN_EXPIRY
        );

        ResetPasswordRequest request = createResetPasswordRequest(
                rawToken,
                "newPassword123",
                "newPassword123"
        );

        when(passwordResetTokenRepository.findByToken(hashedToken))
                .thenReturn(Optional.of(expiredToken));

        // when + then
        InvalidPasswordTokenException exception = assertThrows(
                InvalidPasswordTokenException.class,
                () -> authService.resetPassword(request)
        );

        assertEquals("Token expired", exception.getMessage());

        verify(passwordResetTokenRepository).findByToken(hashedToken);
        verify(passwordResetTokenRepository).delete(expiredToken);

        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }
}