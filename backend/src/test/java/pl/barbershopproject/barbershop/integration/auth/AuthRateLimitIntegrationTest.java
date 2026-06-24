package pl.barbershopproject.barbershop.integration.auth;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.test.web.servlet.ResultMatcher;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.util.Objects;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.reset;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class AuthRateLimitIntegrationTest extends BaseIntegrationTest {

    private static final String RATE_LIMIT_MESSAGE = "Przekroczono limit zapytań.";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setup() {
        reset(captchaService);
        clearRedis();
    }

    @AfterEach
    void tearDown() {
        clearRedis();
    }

    @DisplayName("Should rate limit register endpoint after configured limit")
    @Test
    void shouldRateLimitRegisterEndpointAfterConfiguredLimit() throws Exception {
        // given
        ObjectNode requestBody = createInvalidRegisterRequest();

        // when + then
        performRequestsExpectingStatus(
                "/register",
                requestBody,
                3,
                status().isBadRequest()
        );

        performRequestExpectingRateLimit("/register", requestBody);
    }

    @DisplayName("Should rate limit login endpoint after default limit")
    @Test
    void shouldRateLimitLoginEndpointAfterDefaultLimit() throws Exception {
        // given
        ObjectNode requestBody = createLoginRequest();

        // when + then
        performRequestsExpectingStatus(
                "/login",
                requestBody,
                5,
                status().isUnauthorized()
        );

        performRequestExpectingRateLimit("/login", requestBody);
    }

    @DisplayName("Should rate limit forgot password endpoint after configured limit")
    @Test
    void shouldRateLimitForgotPasswordEndpointAfterConfiguredLimit() throws Exception {
        // given
        ObjectNode requestBody = createForgotPasswordRequest();

        // when + then
        performRequestsExpectingStatus(
                "/forgot-password",
                requestBody,
                3,
                status().isOk()
        );

        performRequestExpectingRateLimit("/forgot-password", requestBody);
    }

    @DisplayName("Should rate limit reset password endpoint after default limit")
    @Test
    void shouldRateLimitResetPasswordEndpointAfterDefaultLimit() throws Exception {
        // given
        ObjectNode requestBody = createInvalidResetPasswordRequest();

        // when + then
        performRequestsExpectingStatus(
                "/reset-password",
                requestBody,
                5,
                status().isBadRequest()
        );

        performRequestExpectingRateLimit("/reset-password", requestBody);
    }

    @DisplayName("Should rate limit email code request endpoint after configured limit")
    @Test
    void shouldRateLimitEmailCodeRequestEndpointAfterConfiguredLimit() throws Exception {
        // given
        ObjectNode requestBody = createEmailCodeRequest();

        // when + then
        performRequestsExpectingStatus(
                "/login/email-code/request",
                requestBody,
                3,
                status().isOk()
        );

        performRequestExpectingRateLimit("/login/email-code/request", requestBody);
    }

    @DisplayName("Should rate limit email code verify endpoint after configured limit")
    @Test
    void shouldRateLimitEmailCodeVerifyEndpointAfterConfiguredLimit() throws Exception {
        // given
        ObjectNode requestBody = createEmailCodeVerifyRequest();

        // when + then
        performRequestsExpectingStatus(
                "/login/email-code/verify",
                requestBody,
                5,
                status().isUnauthorized()
        );

        performRequestExpectingRateLimit("/login/email-code/verify", requestBody);
    }

    @DisplayName("Should keep separate rate limit counters for different auth endpoints")
    @Test
    void shouldKeepSeparateRateLimitCountersForDifferentAuthEndpoints() throws Exception {
        // given
        ObjectNode loginRequest = createLoginRequest();
        ObjectNode forgotPasswordRequest = createForgotPasswordRequest();

        performRequestsExpectingStatus(
                "/login",
                loginRequest,
                5,
                status().isUnauthorized()
        );

        performRequestExpectingRateLimit("/login", loginRequest);

        // when + then
        mockMvc.perform(post("/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest)))
                .andExpect(status().isOk());
    }

    private void performRequestsExpectingStatus(
            String endpoint,
            ObjectNode requestBody,
            int requestCount,
            ResultMatcher expectedStatus
    ) throws Exception {
        for (int i = 0; i < requestCount; i++) {
            mockMvc.perform(post(endpoint)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestBody)))
                    .andExpect(expectedStatus);
        }
    }

    private void performRequestExpectingRateLimit(
            String endpoint,
            ObjectNode requestBody
    ) throws Exception {
        mockMvc.perform(post(endpoint)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestBody)))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"))
                .andExpect(content().string(containsString(RATE_LIMIT_MESSAGE)));
    }

    private ObjectNode createInvalidRegisterRequest() {
        return objectMapper.createObjectNode()
                .put("firstname", "")
                .put("lastname", "")
                .put("email", "invalid-email")
                .put("password", "short")
                .put("captchaToken", "");
    }

    private ObjectNode createLoginRequest() {
        return objectMapper.createObjectNode()
                .put("email", "admin@test.com")
                .put("password", "wrong_password");
    }

    private ObjectNode createForgotPasswordRequest() {
        return objectMapper.createObjectNode()
                .put("email", "missing.ratelimit@example.com")
                .put("captchaToken", "test-captcha-token");
    }

    private ObjectNode createInvalidResetPasswordRequest() {
        return objectMapper.createObjectNode()
                .put("token", "")
                .put("newPassword", "newPassword123")
                .put("confirmPassword", "newPassword123");
    }

    private ObjectNode createEmailCodeRequest() {
        return objectMapper.createObjectNode()
                .put("email", "missing.ratelimit@example.com");
    }

    private ObjectNode createEmailCodeVerifyRequest() {
        return objectMapper.createObjectNode()
                .put("email", "johndoe@example.com")
                .put("code", "123456");
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