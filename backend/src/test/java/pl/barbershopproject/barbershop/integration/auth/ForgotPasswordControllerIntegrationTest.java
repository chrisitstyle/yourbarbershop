package pl.barbershopproject.barbershop.integration.auth;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.time.Instant;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class ForgotPasswordControllerIntegrationTest extends BaseIntegrationTest {

    private static final String EXISTING_EMAIL = "johndoe@example.com";
    private static final String MISSING_EMAIL = "missing@example.com";
    private static final String CAPTCHA_TOKEN = "test-captcha-token";
    private static final String SUCCESS_MESSAGE = "Link do resetowania hasła został wysłany na podany adres email.";

    private static final Instant OLD_TOKEN_EXPIRY_DATE =
            Instant.parse("2030-01-01T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setup() {
        reset(captchaService);
        clearRedis();
        passwordResetTokenRepository.deleteAll();
    }

    @DisplayName("Should create password reset token and return generic message when user exists")
    @Test
    void shouldCreatePasswordResetTokenAndReturnGenericMessage_WhenUserExists() throws Exception {
        // given
        User user = userRepository.findByEmail(EXISTING_EMAIL)
                .orElseThrow();

        PasswordResetToken oldToken = new PasswordResetToken();
        oldToken.setToken("old-token");
        oldToken.setUser(user);
        oldToken.setExpiryDate(OLD_TOKEN_EXPIRY_DATE);

        passwordResetTokenRepository.save(oldToken);

        ObjectNode forgotPasswordData = createForgotPasswordRequest(EXISTING_EMAIL, CAPTCHA_TOKEN);

        // when + then
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordData)))
                .andExpect(status().isOk())
                .andExpect(content().string(SUCCESS_MESSAGE));

        verify(captchaService).verify(CAPTCHA_TOKEN);

        List<PasswordResetToken> tokens = passwordResetTokenRepository.findAll();

        assertEquals(1, tokens.size());

        PasswordResetToken savedToken = tokens.getFirst();

        assertEquals(user.getIdUser(), savedToken.getUser().getIdUser());
        assertNotEquals("old-token", savedToken.getToken());
        assertTrue(savedToken.getToken().matches("[0-9a-f]{64}"));
        assertNotNull(savedToken.getExpiryDate());
        assertNotEquals(OLD_TOKEN_EXPIRY_DATE, savedToken.getExpiryDate());
    }

    @DisplayName("Should return generic message and not create token when user does not exist")
    @Test
    void shouldReturnGenericMessageAndNotCreateToken_WhenUserDoesNotExist() throws Exception {
        // given
        ObjectNode forgotPasswordData = createForgotPasswordRequest(MISSING_EMAIL, CAPTCHA_TOKEN);

        // when + then
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordData)))
                .andExpect(status().isOk())
                .andExpect(content().string(SUCCESS_MESSAGE));

        verify(captchaService).verify(CAPTCHA_TOKEN);

        assertEquals(0, passwordResetTokenRepository.count());
    }

    @DisplayName("Should return bad request when email is invalid")
    @Test
    void shouldReturnBadRequest_WhenEmailIsInvalid() throws Exception {
        // given
        ObjectNode forgotPasswordData = createForgotPasswordRequest("invalid-email", CAPTCHA_TOKEN);

        // when + then
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").value("Niepoprawny format adresu email"));

        assertEquals(0, passwordResetTokenRepository.count());
    }

    @DisplayName("Should return bad request when captcha token is blank")
    @Test
    void shouldReturnBadRequest_WhenCaptchaTokenIsBlank() throws Exception {
        // given
        ObjectNode forgotPasswordData = createForgotPasswordRequest(EXISTING_EMAIL, "");

        // when + then
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.captchaToken").value("CAPTCHA jest wymagana"));

        assertEquals(0, passwordResetTokenRepository.count());
    }

    private ObjectNode createForgotPasswordRequest(String email, String captchaToken) {
        return objectMapper.createObjectNode()
                .put("email", email)
                .put("captchaToken", captchaToken);
    }

    private void clearRedis() {
        RedisConnectionFactory connectionFactory = Objects.requireNonNull(
                stringRedisTemplate.getConnectionFactory()
        );

        try (RedisConnection connection = connectionFactory.getConnection()) {
            connection.serverCommands().flushDb();
        }
    }
}