package pl.barbershopproject.barbershop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.userBuilder;

class JwtServiceTest {

    private static final String SECRET_KEY =
            "AS23432423423432asdasfgfgXZXZzzzAAA12112D334XXXAAA1";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "expirationHours", 8L);
    }

    @DisplayName("generateToken should contain user email as subject")
    @Test
    void generateToken_ShouldContainUserEmailAsSubject() {
        // given
        User user = userBuilder()
                .idUser(7L)
                .email("john.jwt@example.com")
                .role(Role.USER)
                .build();

        // when
        String token = jwtService.generateToken(user);

        // then
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals("john.jwt@example.com", jwtService.extractUserName(token));
    }

    @DisplayName("generateToken should contain user role and id claims")
    @Test
    void generateToken_ShouldContainUserRoleAndIdClaims() {
        // given
        User user = userBuilder()
                .idUser(7L)
                .email("admin.jwt@example.com")
                .role(Role.ADMIN)
                .build();

        // when
        String token = jwtService.generateToken(user);

        // then
        String role = jwtService.extractClaim(
                token,
                claims -> claims.get("role", String.class)
        );

        Number id = jwtService.extractClaim(
                token,
                claims -> claims.get("id", Number.class)
        );

        assertEquals("ADMIN", role);
        assertEquals(7L, id.longValue());
    }

    @DisplayName("generateToken with extra claims should contain custom claims")
    @Test
    void generateToken_ShouldContainCustomClaims_WhenExtraClaimsAreProvided() {
        // given
        User user = userBuilder()
                .idUser(8L)
                .email("custom.jwt@example.com")
                .role(Role.USER)
                .build();

        Map<String, Object> extraClaims = Map.of(
                "customClaim", "custom-value",
                "featureEnabled", true
        );

        // when
        String token = jwtService.generateToken(extraClaims, user);

        // then
        String customClaim = jwtService.extractClaim(
                token,
                claims -> claims.get("customClaim", String.class)
        );

        Boolean featureEnabled = jwtService.extractClaim(
                token,
                claims -> claims.get("featureEnabled", Boolean.class)
        );

        assertEquals("custom-value", customClaim);
        assertTrue(featureEnabled);
        assertEquals("custom.jwt@example.com", jwtService.extractUserName(token));
    }

    @DisplayName("isTokenValid should return true when username matches and token is not expired")
    @Test
    void isTokenValid_ShouldReturnTrue_WhenUsernameMatchesAndTokenIsNotExpired() {
        // given
        User user = userBuilder()
                .idUser(9L)
                .email("valid.jwt@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        // when
        boolean valid = jwtService.isTokenValid(token, user);

        // then
        assertTrue(valid);
        assertFalse(jwtService.isTokenExpired(token));
    }

    @DisplayName("isTokenValid should return false when username does not match")
    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // given
        User tokenOwner = userBuilder()
                .idUser(10L)
                .email("owner.jwt@example.com")
                .role(Role.USER)
                .build();

        User differentUser = userBuilder()
                .idUser(11L)
                .email("different.jwt@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(tokenOwner);

        // when
        boolean valid = jwtService.isTokenValid(token, differentUser);

        // then
        assertFalse(valid);
    }

    @DisplayName("isTokenValid should throw ExpiredJwtException when token is expired")
    @Test
    void isTokenValid_ShouldThrowExpiredJwtException_WhenTokenIsExpired() {
        // given
        ReflectionTestUtils.setField(jwtService, "expirationHours", -1L);

        User user = userBuilder()
                .idUser(12L)
                .email("expired.jwt@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        // when + then
        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(token, user)
        );
    }

    @DisplayName("extractClaim should return expiration claim")
    @Test
    void extractClaim_ShouldReturnExpirationClaim() {
        // given
        User user = userBuilder()
                .idUser(13L)
                .email("expiration.jwt@example.com")
                .role(Role.USER)
                .build();

        String token = jwtService.generateToken(user);

        // when
        Object expiration = jwtService.extractClaim(
                token,
                Claims::getExpiration
        );

        // then
        assertNotNull(expiration);
    }
}
