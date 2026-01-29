package pl.barbershopproject.barbershop.auth.oauth2;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.user.User;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtService jwtService;
    private final Map<String, OAuth2UserStrategy> strategies;

    @Value("${application.security.frontend-url-localhost:http://localhost:3000}")
    private String frontendUrl;

    // the constructor injects a list of all beans implementing the interface
    public OAuth2LoginSuccessHandler(JwtService jwtService, List<OAuth2UserStrategy> strategyList) {
        this.jwtService = jwtService;

        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(OAuth2UserStrategy::getProviderName, Function.identity()));
    }

    @Override
    public void onAuthenticationSuccess(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Authentication authentication) throws IOException {
        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        String registrationId = authToken.getAuthorizedClientRegistrationId(); // ex google or github
        OAuth2User oAuth2User = authToken.getPrincipal();

        OAuth2UserStrategy strategy = strategies.get(registrationId);

        if (strategy == null) {
            log.error("Brak strategii dla dostawcy: {}", registrationId);
            redirectWithError(request, response);
            return;
        }

        // processing user
        User user = strategy.processUser(oAuth2User);


        String token = jwtService.generateToken(user);

        String targetUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/oauth2/redirect")
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    private void redirectWithError(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String errorUrl = UriComponentsBuilder.fromUriString(frontendUrl)
                .path("/login")
                .queryParam("error", "unknown_provider")
                .build()
                .toUriString();
        getRedirectStrategy().sendRedirect(request, response, errorUrl);
    }
}
