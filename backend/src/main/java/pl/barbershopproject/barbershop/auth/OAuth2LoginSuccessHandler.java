package pl.barbershopproject.barbershop.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    // Frontend URL injected from application.properties
    @Value("${application.security.frontend-url-localhost:http://localhost:3000}")
    private String frontendUrl;

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, Authentication authentication) throws IOException {
        Object principal = authentication.getPrincipal();

        // ensure principal is valid and is an OAuth2User
        if (!(principal instanceof OAuth2User oAuth2User)) {
            log.error("Authentication principal is null or not an instance of OAuth2User");
            // redirect to login page with an error
            String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                    .path("/login")
                    .queryParam("error", "oauth_authentication_error")
                    .build()
                    .toUriString();
            getRedirectStrategy().sendRedirect(request, response, errorUrl);
            return;
        }

        // fetch attributes from github
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String email = (String) attributes.get("email");
        String login = (String) attributes.get("login");
        String name = (String) attributes.get("name");

        // fallback - use login or generate UUID if email is not provided (e.g., private email on github)
        if (email == null) {
            String fallbackLogin = login != null ? login : UUID.randomUUID().toString();
            email = fallbackLogin + "@github.placeholder.com";
        }

        // prepare user data - split name on firstname and lastname
        String fullName = name != null ? name : login;
        String[] names = extractFirstAndLastName(fullName);

        String finalEmail = email;

        User user = userRepository.findByEmail(finalEmail)
                .orElseGet(() -> createUser(finalEmail, names[0], names[1]));

        String token = jwtService.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth2/redirect")
                .queryParam("token", token)
                .build()
                .toUriString();

        // redirect the user to the frontend with the token
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    /**
     * Helper method to create and save a new user in the database.
     */
    private User createUser(String email, String firstName, String lastName) {
        return userRepository.save(User.builder()
                .email(email)
                .firstname(firstName)
                .lastname(lastName)
                .role(Role.USER)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build());
    }

    /**
     * Helper to parse full name into [First Name, Last Name].
     * Handles edge cases like single-word names or nulls.
     *
     * @return String array where index 0 is firstName and index 1 is lastName.
     */
    private String[] extractFirstAndLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) {
            return new String[]{"Unknown", ""};
        }

        String trimmedName = fullName.trim();
        int splitIndex = trimmedName.lastIndexOf(" ");

        // only one word in the name (e.g., just a login)
        if (splitIndex == -1) {
            return new String[]{trimmedName, ""};
        }

        // case - standard "First Last" format
        return new String[]{
                trimmedName.substring(0, splitIndex),
                trimmedName.substring(splitIndex + 1)
        };
    }
}