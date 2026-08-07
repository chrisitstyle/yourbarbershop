package pl.barbershopproject.barbershop.integration.resetpassword;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import pl.barbershopproject.barbershop.integration.BaseIntegrationTest;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
class ResetPasswordControllerIntegrationTest extends BaseIntegrationTest {

    private static final String EXISTING_EMAIL = "johndoe@example.com";
    private static final String SUCCESS_MESSAGE = "Hasło zostało pomyślnie zresetowane.";

    private static final Instant VALID_TOKEN_EXPIRY_DATE =
            Instant.parse("2030-01-01T10:00:00Z");

    private static final Instant EXPIRED_TOKEN_EXPIRY_DATE =
            Instant.parse("2000-01-01T10:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @DisplayName("Should reset password and delete token when reset token is valid")
    @Test
    void shouldResetPasswordAndDeleteToken_WhenTokenIsValid() throws Exception {
        // given
        String rawToken = "valid-reset-token";
        String hashedToken = sha256Hex(rawToken);
        String newPassword = "newPassword123";

        User user = userRepository.findByEmail(EXISTING_EMAIL)
                .orElseThrow();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(hashedToken);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(VALID_TOKEN_EXPIRY_DATE);

        passwordResetTokenRepository.save(passwordResetToken);

        ObjectNode resetPasswordData = createResetPasswordRequest(
                rawToken,
                newPassword,
                newPassword
        );

        // when + then
        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordData)))
                .andExpect(status().isOk())
                .andExpect(content().string(SUCCESS_MESSAGE));

        User updatedUser = userRepository.findByEmail(EXISTING_EMAIL)
                .orElseThrow();

        assertTrue(passwordEncoder.matches(newPassword, updatedUser.getPassword()));
        assertTrue(passwordResetTokenRepository.findByToken(hashedToken).isEmpty());
    }

    @DisplayName("Should return bad request when reset token does not exist")
    @Test
    void shouldReturnBadRequest_WhenTokenDoesNotExist() throws Exception {
        // given
        ObjectNode resetPasswordData = createResetPasswordRequest(
                "missing-reset-token",
                "newPassword123",
                "newPassword123"
        );

        // when + then
        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid token"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @DisplayName("Should return bad request when reset token is expired")
    @Test
    void shouldReturnBadRequest_WhenTokenIsExpired() throws Exception {
        // given
        String rawToken = "expired-reset-token";
        String hashedToken = sha256Hex(rawToken);

        User user = userRepository.findByEmail(EXISTING_EMAIL)
                .orElseThrow();

        PasswordResetToken expiredToken = new PasswordResetToken();
        expiredToken.setToken(hashedToken);
        expiredToken.setUser(user);
        expiredToken.setExpiryDate(EXPIRED_TOKEN_EXPIRY_DATE);

        passwordResetTokenRepository.save(expiredToken);

        ObjectNode resetPasswordData = createResetPasswordRequest(
                rawToken,
                "newPassword123",
                "newPassword123"
        );

        // when + then
        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Token expired"))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    @DisplayName("Should return bad request when passwords do not match")
    @Test
    void shouldReturnBadRequest_WhenPasswordsDoNotMatch() throws Exception {
        // given
        ObjectNode resetPasswordData = createResetPasswordRequest(
                "any-reset-token",
                "newPassword123",
                "differentPassword123"
        );

        // when + then
        mockMvc.perform(post("/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(resetPasswordData)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Hasła nie są takie same."))
                .andExpect(jsonPath("$.status").value("BAD_REQUEST"));
    }

    private ObjectNode createResetPasswordRequest(
            String token,
            String newPassword,
            String confirmPassword
    ) {
        return objectMapper.createObjectNode()
                .put("token", token)
                .put("newPassword", newPassword)
                .put("confirmPassword", confirmPassword);
    }

    private static String sha256Hex(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}