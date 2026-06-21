package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.user.User;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

/**
 * Utility class providing factory and helper methods for password reset token tests.
 * <p>
 * This class centralizes creation of {@link PasswordResetToken} entities and
 * token hashing logic used in tests. It is especially useful when testing
 * authentication flows where raw reset tokens are sent to the user, but hashed
 * tokens are stored in the database.
 * </p>
 *
 * <p>
 * The {@link #sha256Hex(String)} method mirrors the hashing format used by
 * {@code AuthService}, allowing tests to prepare repository mocks with the
 * same token value that the service will search for internally.
 * </p>
 */
public final class PasswordResetTokenTestEntities {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private PasswordResetTokenTestEntities() {
    }

    /**
     * Creates a {@link PasswordResetToken} entity for tests.
     * <p>
     * The returned token has the provided user, token value and expiration date.
     * This method is useful when preparing mocked repository responses for
     * valid, invalid or expired password reset token scenarios.
     * </p>
     *
     * @param user       user assigned to the password reset token
     * @param token      token value stored in the entity, usually a hashed token
     * @param expiryDate expiration date of the token
     * @return a {@link PasswordResetToken} populated with the provided values
     */
    public static PasswordResetToken createPasswordResetToken(
            User user,
            String token,
            Instant expiryDate
    ) {
        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setUser(user);
        passwordResetToken.setToken(token);
        passwordResetToken.setExpiryDate(expiryDate);
        return passwordResetToken;
    }

    /**
     * Hashes a raw token using SHA-256 and returns it as a lowercase hexadecimal string.
     * <p>
     * This helper is used in tests because password reset tokens are stored as
     * hashes, while the user-facing reset URL contains the raw token. It allows
     * tests to verify or mock the same hashed token value that production code
     * expects.
     * </p>
     *
     * @param token raw token value to hash
     * @return SHA-256 hash of the provided token encoded as lowercase hexadecimal text
     * @throws IllegalStateException if the SHA-256 algorithm is not available
     */
    public static String sha256Hex(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashedBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashedBytes);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 algorithm is not available", exception);
        }
    }
}