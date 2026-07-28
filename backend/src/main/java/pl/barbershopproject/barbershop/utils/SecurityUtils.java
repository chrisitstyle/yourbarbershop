package pl.barbershopproject.barbershop.utils;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Utility class providing static helper methods related to Spring Security authentication context.
 * <p>
 * Instantiation is prevented via a private constructor.
 */
public class SecurityUtils {

    /**
     * Private constructor to prevent instantiation of this utility class.
     *
     * @throws IllegalStateException if constructor execution is attempted via reflection
     */
    private SecurityUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Safely retrieves the email address (username) of the currently authenticated actor
     * from the Spring Security context.
     * <p>
     * This method is null-safe and handles unauthenticated or anonymous access gracefully.
     * If the security context is empty, the user is unauthenticated, or the principal represents
     * an anonymous user, the default value {@code "SYSTEM"} is returned.
     *
     * @return the authenticated user's email address, or {@code "SYSTEM"} if no authenticated user is present
     */
    public static String getActorEmailSafely() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}

