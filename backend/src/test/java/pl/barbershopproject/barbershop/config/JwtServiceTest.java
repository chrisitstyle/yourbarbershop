package pl.barbershopproject.barbershop.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import pl.barbershopproject.barbershop.security.UserPrincipal;
import pl.barbershopproject.barbershop.user.Role;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUserPrincipal;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.userBuilder;

class JwtServiceTest {

    private static final String SECRET_KEY =
            "AS23432423423432asdasfgfgXZXZzzzAAA12112D334XXXAAA1";

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();

        ReflectionTestUtils.setField(jwtService, "secretKey", SECRET_KEY);
        ReflectionTestUtils.setField(jwtService, "accessExpirationMinutes", 15L);
    }

    @DisplayName("generateAccessToken should contain user email as subject")
    @Test
    void generateAccessToken_ShouldContainUserEmailAsSubject() {
        // given
        UserPrincipal principal = createUserPrincipal(userBuilder()
                .idUser(7L)
                .email("john.jwt@example.com")
                .role(Role.USER)
                .build());

        // when
        String token = jwtService.generateAccessToken(principal);

        // then
        assertNotNull(token);
        assertFalse(token.isBlank());
        assertEquals(
                "john.jwt@example.com",
                jwtService.extractUserName(token)
        );
    }

    @DisplayName("generateAccessToken should contain user role and id claims")
    @Test
    void generateAccessToken_ShouldContainUserRoleAndIdClaims() {
        // given
        UserPrincipal principal = createUserPrincipal(userBuilder()
                .idUser(7L)
                .email("admin.jwt@example.com")
                .role(Role.ADMIN)
                .build());

        // when
        String token = jwtService.generateAccessToken(principal);

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

    @DisplayName("generateAccessToken with extra claims should contain custom claims")
    @Test
    void generateAccessToken_ShouldContainCustomClaims_WhenExtraClaimsAreProvided() {
        // given
        UserPrincipal principal = createUserPrincipal(userBuilder()
                .idUser(8L)
                .email("custom.jwt@example.com")
                .role(Role.USER)
                .build());

        Map<String, Object> extraClaims = Map.of(
                "customClaim", "custom-value",
                "featureEnabled", true
        );

        // when
        String token = jwtService.generateAccessToken(
                extraClaims,
                principal
        );

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
        assertEquals(
                "custom.jwt@example.com",
                jwtService.extractUserName(token)
        );
    }

    @DisplayName("isTokenValid should return true when username matches and token is not expired")
    @Test
    void isTokenValid_ShouldReturnTrue_WhenUsernameMatchesAndTokenIsNotExpired() {
        // given
        UserPrincipal principal = createUserPrincipal();

        String token = jwtService.generateAccessToken(principal);

        // when
        boolean valid = jwtService.isTokenValid(token, principal);

        // then
        assertTrue(valid);
        assertFalse(jwtService.isTokenExpired(token));
    }

    @DisplayName("isTokenValid should return false when username does not match")
    @Test
    void isTokenValid_ShouldReturnFalse_WhenUsernameDoesNotMatch() {
        // given
        UserPrincipal tokenOwner = createUserPrincipal(
                userBuilder()
                        .idUser(10L)
                        .email("owner.jwt@example.com")
                        .build());

        UserPrincipal differentUser = createUserPrincipal(
                userBuilder()
                        .idUser(11L)
                        .email("different.jwt@example.com")
                        .build());

        String token = jwtService.generateAccessToken(tokenOwner);

        // when
        boolean valid = jwtService.isTokenValid(token, differentUser);

        // then
        assertFalse(valid);
    }

    @DisplayName("isTokenValid should throw ExpiredJwtException when token is expired")
    @Test
    void isTokenValid_ShouldThrowExpiredJwtException_WhenTokenIsExpired() {
        // given
        ReflectionTestUtils.setField(
                jwtService,
                "accessExpirationMinutes",
                -1L
        );

        UserPrincipal principal = createUserPrincipal();

        String token = jwtService.generateAccessToken(principal);

        // when + then
        assertThrows(
                ExpiredJwtException.class,
                () -> jwtService.isTokenValid(token, principal)
        );
    }

    @DisplayName("extractClaim should return expiration claim")
    @Test
    void extractClaim_ShouldReturnExpirationClaim() {
        // given
        UserPrincipal principal = createUserPrincipal();

        String token = jwtService.generateAccessToken(principal);

        // when
        Object expiration = jwtService.extractClaim(
                token,
                Claims::getExpiration
        );

        // then
        assertNotNull(expiration);
    }

}