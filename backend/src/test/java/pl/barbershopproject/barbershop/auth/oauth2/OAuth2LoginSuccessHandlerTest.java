package pl.barbershopproject.barbershop.auth.oauth2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.test.util.ReflectionTestUtils;
import pl.barbershopproject.barbershop.config.JwtService;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class OAuth2LoginSuccessHandlerTest {

    private static final String FRONTEND_URL = "http://localhost:3000";
    private static final String JWT_TOKEN = "jwt-token";

    @DisplayName("Should process OAuth2 user, generate JWT and redirect to frontend")
    @Test
    void onAuthenticationSuccess_ShouldRedirectWithJwtToken_WhenProviderIsSupported() throws Exception {
        // given
        JwtService jwtService = mock(JwtService.class);
        OAuth2UserStrategy googleStrategy = mock(OAuth2UserStrategy.class);

        when(googleStrategy.getProviderName()).thenReturn("google");

        OAuth2LoginSuccessHandler successHandler =
                new OAuth2LoginSuccessHandler(jwtService, List.of(googleStrategy));

        ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);

        OAuth2User oAuth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createAuthentication("google", oAuth2User);

        User user = User.builder()
                .idUser(1L)
                .firstname("John")
                .lastname("Doe")
                .email("john.google@example.com")
                .password("password")
                .role(Role.USER)
                .build();

        when(googleStrategy.processUser(oAuth2User)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(JWT_TOKEN);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus());
        assertEquals(
                FRONTEND_URL + "/oauth2/redirect?token=" + JWT_TOKEN,
                response.getRedirectedUrl()
        );

        verify(googleStrategy).processUser(oAuth2User);
        verify(jwtService).generateToken(user);
    }

    @DisplayName("Should redirect with error when provider is not supported")
    @Test
    void onAuthenticationSuccess_ShouldRedirectWithError_WhenProviderIsNotSupported() throws Exception {
        // given
        JwtService jwtService = mock(JwtService.class);
        OAuth2UserStrategy googleStrategy = mock(OAuth2UserStrategy.class);

        when(googleStrategy.getProviderName()).thenReturn("google");

        OAuth2LoginSuccessHandler successHandler =
                new OAuth2LoginSuccessHandler(jwtService, List.of(googleStrategy));

        ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);

        OAuth2User oAuth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createAuthentication("facebook", oAuth2User);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus());
        assertEquals(
                FRONTEND_URL + "/login?error=unknown_provider",
                response.getRedirectedUrl()
        );

        verify(googleStrategy, never()).processUser(any(OAuth2User.class));
        verify(jwtService, never()).generateToken(any(User.class));
    }

    @DisplayName("Should use matching strategy based on OAuth2 registration id")
    @Test
    void onAuthenticationSuccess_ShouldUseMatchingStrategy_BasedOnRegistrationId() throws Exception {
        // given
        JwtService jwtService = mock(JwtService.class);

        OAuth2UserStrategy googleStrategy = mock(OAuth2UserStrategy.class);
        OAuth2UserStrategy githubStrategy = mock(OAuth2UserStrategy.class);

        when(googleStrategy.getProviderName()).thenReturn("google");
        when(githubStrategy.getProviderName()).thenReturn("github");

        OAuth2LoginSuccessHandler successHandler =
                new OAuth2LoginSuccessHandler(jwtService, List.of(googleStrategy, githubStrategy));

        ReflectionTestUtils.setField(successHandler, "frontendUrl", FRONTEND_URL);

        OAuth2User oAuth2User = createOAuth2User();
        OAuth2AuthenticationToken authentication = createAuthentication("github", oAuth2User);

        User user = User.builder()
                .idUser(2L)
                .firstname("Octo")
                .lastname("Cat")
                .email("octocat@github.placeholder.com")
                .password("password")
                .role(Role.USER)
                .build();

        when(githubStrategy.processUser(oAuth2User)).thenReturn(user);
        when(jwtService.generateToken(user)).thenReturn(JWT_TOKEN);

        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();

        // when
        successHandler.onAuthenticationSuccess(request, response, authentication);

        // then
        assertEquals(302, response.getStatus());
        assertEquals(
                FRONTEND_URL + "/oauth2/redirect?token=" + JWT_TOKEN,
                response.getRedirectedUrl()
        );

        verify(githubStrategy).processUser(oAuth2User);
        verify(googleStrategy, never()).processUser(any(OAuth2User.class));
        verify(jwtService).generateToken(user);
    }

    private static OAuth2User createOAuth2User() {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                Map.of(
                        "sub", "oauth-user-id",
                        "email", "john.oauth@example.com"
                ),
                "sub"
        );
    }

    private static OAuth2AuthenticationToken createAuthentication(
            String registrationId,
            OAuth2User oAuth2User
    ) {
        return new OAuth2AuthenticationToken(
                oAuth2User,
                oAuth2User.getAuthorities(),
                registrationId
        );
    }
}
