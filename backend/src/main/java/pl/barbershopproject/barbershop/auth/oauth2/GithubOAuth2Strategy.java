package pl.barbershopproject.barbershop.auth.oauth2;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class GithubOAuth2Strategy implements OAuth2UserStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getProviderName() {
        return "github";
    }

    @Override
    public User processUser(OAuth2User oAuth2User) {
        String email = oAuth2User.getAttribute("email");
        String login = oAuth2User.getAttribute("login");
        String name = oAuth2User.getAttribute("name");

        // github mail fallback
        if (email == null) {
            String fallbackLogin = login != null ? login : UUID.randomUUID().toString();
            email = fallbackLogin + "@github.placeholder.com";
        }

        // parsing fullname
        String fullName = name != null ? name : login;
        String[] names = extractFirstAndLastName(fullName);

        final String finalEmail = email;
        return userRepository.findByEmail(finalEmail)
                .orElseGet(() -> createUser(finalEmail, names[0], names[1]));
    }

    private User createUser(String email, String firstName, String lastName) {
        return userRepository.save(User.builder()
                .email(email)
                .firstname(firstName)
                .lastname(lastName)
                .role(Role.USER)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .build());
    }

    private String[] extractFirstAndLastName(String fullName) {
        if (fullName == null || fullName.isBlank()) return new String[]{"Unknown", ""};
        String trimmed = fullName.trim();
        int splitIndex = trimmed.lastIndexOf(" ");
        if (splitIndex == -1) return new String[]{trimmed, ""};
        return new String[]{trimmed.substring(0, splitIndex), trimmed.substring(splitIndex + 1)};
    }
}
