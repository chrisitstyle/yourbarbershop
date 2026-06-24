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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
@Transactional
class EmailCodeLoginControllerIntegrationTest extends BaseIntegrationTest {

    private static final String EXISTING_EMAIL = "johndoe@example.com";
    private static final String MISSING_EMAIL = "missing@example.com";
    private static final String VALID_CODE = "123456";
    private static final String REQUEST_SUCCESS_MESSAGE =
            "Jeśli konto istnieje, kod zostanie wysłany na podany adres e-mail.";

    private static final String CODE_KEY_PREFIX = "auth:email-login-code:";
    private static final String ATTEMPTS_KEY_PREFIX = "auth:email-login-attempts:";
    private static final String REQUEST_COOLDOWN_KEY_PREFIX = "auth:email-login-request-cooldown:";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setup() {
        reset(captchaService);
        clearRedis();
    }

    @DisplayName("Should request email login code and store code hash when user exists")
    @Test
    void shouldRequestEmailLoginCodeAndStoreCodeHash_WhenUserExists() throws Exception {
        // given
        ObjectNode requestData = createEmailCodeRequest(EXISTING_EMAIL);

        String codeKey = buildCodeKey(EXISTING_EMAIL);
        String cooldownKey = buildCooldownKey(EXISTING_EMAIL);

        // when + then
        mockMvc.perform(post("/login/email-code/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(content().string(REQUEST_SUCCESS_MESSAGE));

        String storedCodeHash = stringRedisTemplate.opsForValue().get(codeKey);

        assertNotNull(storedCodeHash);
        assertFalse(storedCodeHash.isBlank());
        assertNotEquals(VALID_CODE, storedCodeHash);
        assertEquals("1", stringRedisTemplate.opsForValue().get(cooldownKey));
    }

    @DisplayName("Should return generic message and not store code when user does not exist")
    @Test
    void shouldReturnGenericMessageAndNotStoreCode_WhenUserDoesNotExist() throws Exception {
        // given
        ObjectNode requestData = createEmailCodeRequest(MISSING_EMAIL);

        String codeKey = buildCodeKey(MISSING_EMAIL);
        String cooldownKey = buildCooldownKey(MISSING_EMAIL);

        // when + then
        mockMvc.perform(post("/login/email-code/request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestData)))
                .andExpect(status().isOk())
                .andExpect(content().string(REQUEST_SUCCESS_MESSAGE));

        assertNull(stringRedisTemplate.opsForValue().get(codeKey));
        assertEquals("1", stringRedisTemplate.opsForValue().get(cooldownKey));
    }

    @DisplayName("Should verify email login code and return auth response when code is valid")
    @Test
    void shouldVerifyEmailLoginCodeAndReturnAuthResponse_WhenCodeIsValid() throws Exception {
        // given
        User user = userRepository.findByEmail(EXISTING_EMAIL)
                .orElseThrow();

        String codeKey = buildCodeKey(EXISTING_EMAIL);
        String attemptsKey = buildAttemptsKey(EXISTING_EMAIL);

        stringRedisTemplate.opsForValue().set(
                codeKey,
                Objects.requireNonNull(passwordEncoder.encode(VALID_CODE)),
                Duration.ofMinutes(10)
        );

        ObjectNode verifyData = createEmailCodeVerifyRequest(EXISTING_EMAIL.toUpperCase(Locale.ROOT), VALID_CODE);

        // when + then
        mockMvc.perform(post("/login/email-code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyData)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.id").value(user.getIdUser()))
                .andExpect(jsonPath("$.role").value("USER"));

        assertNull(stringRedisTemplate.opsForValue().get(codeKey));
        assertNull(stringRedisTemplate.opsForValue().get(attemptsKey));
    }

    @DisplayName("Should return unauthorized when email login code is invalid")
    @Test
    void shouldReturnUnauthorized_WhenEmailLoginCodeIsInvalid() throws Exception {
        // given
        String codeKey = buildCodeKey(EXISTING_EMAIL);

        stringRedisTemplate.opsForValue().set(
                codeKey,
                Objects.requireNonNull(passwordEncoder.encode(VALID_CODE)),
                Duration.ofMinutes(10)
        );

        ObjectNode verifyData = createEmailCodeVerifyRequest(EXISTING_EMAIL, "000000");

        // when + then
        mockMvc.perform(post("/login/email-code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyData)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired login code"))
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"));
    }

    @DisplayName("Should return unauthorized when email login code does not exist")
    @Test
    void shouldReturnUnauthorized_WhenEmailLoginCodeDoesNotExist() throws Exception {
        // given
        ObjectNode verifyData = createEmailCodeVerifyRequest(EXISTING_EMAIL, VALID_CODE);

        // when + then
        mockMvc.perform(post("/login/email-code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyData)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Invalid or expired login code"))
                .andExpect(jsonPath("$.status").value("UNAUTHORIZED"));
    }

    @DisplayName("Should return bad request when email login code has invalid format")
    @Test
    void shouldReturnBadRequest_WhenEmailLoginCodeHasInvalidFormat() throws Exception {
        // given
        ObjectNode verifyData = createEmailCodeVerifyRequest(EXISTING_EMAIL, "123");

        // when + then
        mockMvc.perform(post("/login/email-code/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(verifyData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("Code must contain 6 digits"));
    }

    private ObjectNode createEmailCodeRequest(String email) {
        return objectMapper.createObjectNode()
                .put("email", email);
    }

    private ObjectNode createEmailCodeVerifyRequest(String email, String code) {
        return objectMapper.createObjectNode()
                .put("email", email)
                .put("code", code);
    }

    private String buildCodeKey(String email) {
        return CODE_KEY_PREFIX + sha256Hex(normalizeEmail(email));
    }

    private String buildAttemptsKey(String email) {
        return ATTEMPTS_KEY_PREFIX + sha256Hex(normalizeEmail(email));
    }

    private String buildCooldownKey(String email) {
        return REQUEST_COOLDOWN_KEY_PREFIX + sha256Hex(normalizeEmail(email));
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private static String sha256Hex(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
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
