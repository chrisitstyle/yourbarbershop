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
public class GoogleOAuth2Strategy implements OAuth2UserStrategy {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public String getProviderName() {
        return "google";
    }

    @Override
    public User processUser(OAuth2User oAuth2User) {

        String email = oAuth2User.getAttribute("email");
        String firstName = oAuth2User.getAttribute("given_name");
        String lastName = oAuth2User.getAttribute("family_name");

        // if Google doesn't return a last name, default to empty string
        if (lastName == null) {
            lastName = "";
        }

        if (email == null) {
            throw new IllegalArgumentException("Nie otrzymano adresu email od dostawcy Google.");
        }

        String finalEmail = email;
        String finalFirstName = firstName;
        String finalLastName = lastName;
        return userRepository.findByEmail(finalEmail)
                .orElseGet(() -> createUser(finalEmail, finalFirstName, finalLastName));
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
}
