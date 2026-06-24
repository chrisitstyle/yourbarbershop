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

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GoogleOAuth2StrategyTest {

    private static final String ENCODED_PASSWORD = "encoded-oauth-password";

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private GoogleOAuth2Strategy googleOAuth2Strategy;

    @DisplayName("Should return google as provider name")
    @Test
    void getProviderName_ShouldReturnGoogle() {
        // when
        String providerName = googleOAuth2Strategy.getProviderName();

        // then
        assertEquals("google", providerName);
    }

    @DisplayName("Should return existing user without creating duplicate")
    @Test
    void processUser_ShouldReturnExistingUserWithoutCreatingDuplicate_WhenEmailAlreadyExists() {
        // given
        String email = "john.google@example.com";

        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "sub", "google-user-id",
                        "email", email,
                        "given_name", "John",
                        "family_name", "Doe"
                ),
                "sub"
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
        User result = googleOAuth2Strategy.processUser(oAuth2User);

        // then
        assertSame(existingUser, result);

        verify(userRepository).findByEmail(email);
        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @DisplayName("Should create new user when Google email does not exist")
    @Test
    void processUser_ShouldCreateNewUser_WhenEmailDoesNotExist() {
        // given
        String email = "new.google@example.com";

        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "sub", "google-user-id",
                        "email", email,
                        "given_name", "John",
                        "family_name", "Doe"
                ),
                "sub"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User savedUser = invocation.getArgument(0);
            savedUser.setIdUser(10L);
            return savedUser;
        });

        // when
        User result = googleOAuth2Strategy.processUser(oAuth2User);

        // then
        assertEquals(10L, result.getIdUser());
        assertEquals(email, result.getEmail());
        assertEquals("John", result.getFirstname());
        assertEquals("Doe", result.getLastname());
        assertEquals(Role.USER, result.getRole());
        assertEquals(ENCODED_PASSWORD, result.getPassword());

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertEquals(email, savedUser.getEmail());
        assertEquals("John", savedUser.getFirstname());
        assertEquals("Doe", savedUser.getLastname());
        assertEquals(Role.USER, savedUser.getRole());
        assertEquals(ENCODED_PASSWORD, savedUser.getPassword());

        verify(passwordEncoder).encode(anyString());
    }

    @DisplayName("Should use empty lastname when Google does not return family name")
    @Test
    void processUser_ShouldUseEmptyLastname_WhenFamilyNameIsMissing() {
        // given
        String email = "single.name.google@example.com";

        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "sub", "google-user-id",
                        "email", email,
                        "given_name", "John"
                ),
                "sub"
        );

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn(ENCODED_PASSWORD);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        User result = googleOAuth2Strategy.processUser(oAuth2User);

        // then
        assertEquals(email, result.getEmail());
        assertEquals("John", result.getFirstname());
        assertEquals("", result.getLastname());
        assertEquals(Role.USER, result.getRole());
        assertEquals(ENCODED_PASSWORD, result.getPassword());
    }

    @DisplayName("Should throw exception when Google does not return email")
    @Test
    void processUser_ShouldThrowIllegalArgumentException_WhenEmailIsMissing() {
        // given
        OAuth2User oAuth2User = createOAuth2User(
                Map.of(
                        "sub", "google-user-id",
                        "given_name", "John",
                        "family_name", "Doe"
                ),
                "sub"
        );

        // when + then
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> googleOAuth2Strategy.processUser(oAuth2User)
        );

        assertEquals("Nie otrzymano adresu email od dostawcy Google.", exception.getMessage());

        verifyNoInteractions(userRepository);
        verifyNoInteractions(passwordEncoder);
    }

    private static OAuth2User createOAuth2User(
            Map<String, Object> attributes,
            String nameAttributeKey
    ) {
        return new DefaultOAuth2User(
                List.of(new SimpleGrantedAuthority("ROLE_USER")),
                attributes,
                nameAttributeKey
        );
    }
}
