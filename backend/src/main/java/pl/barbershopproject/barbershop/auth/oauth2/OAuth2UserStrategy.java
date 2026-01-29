package pl.barbershopproject.barbershop.auth.oauth2;

import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.barbershopproject.barbershop.user.User;

public interface OAuth2UserStrategy {
    /**
     * Returns the provider name, e.g., "google" or "github".
     */
    String getProviderName();

    /**
     * Processes the data from the provider and returns the persisted user.
     */
    User processUser(OAuth2User oAuth2User);
}
