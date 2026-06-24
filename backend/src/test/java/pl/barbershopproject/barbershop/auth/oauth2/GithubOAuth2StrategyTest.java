package pl.barbershopproject.barbershop.auth.oauth2;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GithubOAuth2StrategyTest {

    private static final String ENCODED_PASSWORD = "encoded-oauth-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GithubOAuth2Strategy githubOAuth2Strategy;

    @DisplayName("Should return github as provider name")
    @Test
    void getProviderName_ShouldReturnGithub() {
        // when
        String providerName = githubOAuth2Strategy.getProviderName();

        // then
        assertEquals("github", providerName);
    }

    @DisplayName("Should return existing user without creating duplicate")
    @Test
    void processUser_ShouldReturnExistingUserWithoutCreatingDuplicate_WhenEmailAlreadyExists() {
        // given
        String email = "john.github@example.com";

        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "id", "github-user-id",
                        "email", email,
                        "login", "john-dev",
                        "name", "John Doe"
                ),
                "id"
        );

        User existingUser = User.builder()
                .idUser(1L)
                .firstname("Existing")
                .lastname("User")
                .email(email)
                .password("existing-password")
                .role(Role.USER)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(existingUser));

        // when
        User result = githubOAuth2Strategy.processUser(oAuth2User);

        // then
        assertSame(existingUser, result);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @DisplayName("Should create new user and split full name by last space")
    @Test
    void processUser_ShouldCreateNewUserAndSplitFullNameByLastSpace_WhenEmailDoesNotExist() {
        // given
        String email = "new.github@example.com";

        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "id", "github-user-id",
                        "email", email,
                        "login", "john-dev",
                        "name", "John Michael Doe"
                ),
                "id"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setIdUser(10L);
            return savedUser;
        });

        // when
        User result = githubOAuth2Strategy.processUser(oAuth2User);

        // then
        assertEquals(10L, result.getIdUser());
        assertEquals(email, result.getEmail());
        assertEquals("John Michael", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        assertEquals(Role.USER, result.getRole());
        assertEquals(ENCODED_PASSWORD, result.getPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(email, savedUser.getEmail());
        assertEquals("John Michael", savedUser.getFirstname());
        assertEquals("Doe", savedUser.getLastname());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());

        verify(passwordEncoder).encode(anyString());
    }

    @DisplayName("Should use login as firstname when GitHub name is missing")
    @Test
    void processUser_ShouldUseLoginAsFirstname_WhenNameIsMissing() {
        // given
        String email = "login.only.github@example.com";

        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "id", "github-user-id",
                        "email", email,
                        "login", "octocat"
                ),
                "id"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = githubOAuth2Strategy.processUser(oAuth2User);

        // then
        assertEquals(email, result.getEmail());
        assertEquals("octocat", result.getFirstname());
        assertEquals("", result.getLastname());
        assertEquals(Role.USER, result.getRole());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
    }

    @DisplayName("Should create placeholder email when GitHub does not return email")
    @Test
    void processUser_ShouldCreatePlaceholderEmail_WhenEmailIsMissing() {
        // given
        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "id", "github-user-id",
                        "login", "octocat",
                        "name", "Octo Cat"
                ),
                "id"
        );

        String placeholderEmail = "octocat@github.placeholder.com";

        when(userRepository.findByEmail(placeholderEmail)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = githubOAuth2Strategy.processUser(oAuth2User);

        // then
        assertEquals(placeholderEmail, result.getEmail());
        assertEquals("Octo", result.getFirstname());
        assertEquals("Cat", result.getLastname());
        assertEquals(Role.USER, result.getRole());
        assertEquals(ENCODED_PASSWORD, result.getPassword());

        verify(userRepository).findByEmail(placeholderEmail);
        verify(userRepository).save(any(User.class));
    }

    @DisplayName("Should use unknown name when GitHub does not return name or login")
    @Test
    void processUser_ShouldUseUnknownName_WhenNameAndLoginAreMissing() {
        // given
        OAuth2User oAuth2User = createOAuth2User(
                Map.of("id", "github-user-id"),
                "id"
        );

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = githubOAuth2Strategy.processUser(oAuth2User);

        // then
        assertTrue(result.getEmail().endsWith("@github.placeholder.com"));
        assertEquals("Unknown", result.getFirstname());
        assertEquals("", result.getLastname());
        assertEquals(Role.USER, result.getRole());
        assertEquals(ENCODED_PASSWORD, result.getPassword());

        verify(userRepository).findByEmail(anyString());
        verify(userRepository).save(any(User.class));
    }

    private static OAuth2User createOAuth2User(
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                new HashMap<>(attributes),
                nameAttributeKey
        );
    }
}
