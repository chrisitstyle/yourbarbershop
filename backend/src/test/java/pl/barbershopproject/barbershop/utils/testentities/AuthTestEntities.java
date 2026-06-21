package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.auth.AuthRequest;
import pl.barbershopproject.barbershop.auth.ForgotPasswordRequest;
import pl.barbershopproject.barbershop.auth.RegisterRequest;
import pl.barbershopproject.barbershop.auth.ResetPasswordRequest;

/**
 * Utility class providing factory methods for authentication-related test objects.
 * <p>
 * This class centralizes creation of authentication request DTOs used in unit and integration tests,
 * such as registration, login, forgot password and reset password requests.
 * It helps reduce duplicated setup code in test classes and keeps default authentication test data consistent.
 * </p>
 */
public final class AuthTestEntities {

    /**
     * Private constructor to prevent instantiation of this utility class.
     */
    private AuthTestEntities() {
    }

    /**
     * Creates a default {@link RegisterRequest} for tests.
     * <p>
     * Default values:
     * firstname: {@code John},
     * lastname: {@code Doe},
     * email: {@code johndoe@example.com},
     * password: {@code test_password},
     * captcha token: {@code captcha-token}.
     * </p>
     *
     * @return a {@link RegisterRequest} populated with default test data
     */
    public static RegisterRequest createRegisterRequest() {
        return new RegisterRequest(
                "John",
                "Doe",
                "johndoe@example.com",
                "test_password",
                "captcha-token"
        );
    }

    /**
     * Creates a custom {@link RegisterRequest} for tests.
     *
     * @param firstname    user's first name
     * @param lastname     user's last name
     * @param email        user's email address
     * @param password     user's raw password
     * @param captchaToken captcha verification token
     * @return a {@link RegisterRequest} populated with the provided values
     */
    public static RegisterRequest createRegisterRequest(
            String firstname,
            String lastname,
            String email,
            String password,
            String captchaToken
    ) {
        return new RegisterRequest(firstname, lastname, email, password, captchaToken);
    }

    /**
     * Creates a default {@link AuthRequest} for tests.
     * <p>
     * Default credentials:
     * email: {@code johndoe@example.com},
     * password: {@code test_password}.
     * </p>
     *
     * @return an {@link AuthRequest} populated with default test credentials
     */
    public static AuthRequest createAuthRequest() {
        return new AuthRequest("johndoe@example.com", "test_password");
    }

    /**
     * Creates a custom {@link AuthRequest} for tests.
     *
     * @param email    user's email address
     * @param password user's raw password
     * @return an {@link AuthRequest} populated with the provided credentials
     */
    public static AuthRequest createAuthRequest(String email, String password) {
        return new AuthRequest(email, password);
    }

    /**
     * Creates a default {@link ForgotPasswordRequest} for tests.
     * <p>
     * Default values:
     * email: {@code johndoe@example.com},
     * captcha token: {@code captcha-token}.
     * </p>
     *
     * @return a {@link ForgotPasswordRequest} populated with default test data
     */
    public static ForgotPasswordRequest createForgotPasswordRequest() {
        return new ForgotPasswordRequest("johndoe@example.com", "captcha-token");
    }

    /**
     * Creates a custom {@link ForgotPasswordRequest} for tests.
     *
     * @param email        user's email address
     * @param captchaToken captcha verification token
     * @return a {@link ForgotPasswordRequest} populated with the provided values
     */
    public static ForgotPasswordRequest createForgotPasswordRequest(String email, String captchaToken) {
        return new ForgotPasswordRequest(email, captchaToken);
    }

    /**
     * Creates a custom {@link ResetPasswordRequest} for tests.
     * <p>
     * This method is useful because {@link ResetPasswordRequest} is a mutable DTO with setters,
     * unlike record-based request classes such as {@link AuthRequest}, {@link RegisterRequest}
     * and {@link ForgotPasswordRequest}.
     * </p>
     *
     * @param token           raw password reset token received by the user
     * @param newPassword     new raw password
     * @param confirmPassword confirmation of the new raw password
     * @return a {@link ResetPasswordRequest} populated with the provided values
     */
    public static ResetPasswordRequest createResetPasswordRequest(
            String token,
            String newPassword,
            String confirmPassword
    ) {
        ResetPasswordRequest request = new ResetPasswordRequest();
        request.setToken(token);
        request.setNewPassword(newPassword);
        request.setConfirmPassword(confirmPassword);
        return request;
    }
}
