package pl.barbershopproject.barbershop.integration.resetpassword;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.barbershopproject.barbershop.integration.AbstractRepositoryTest;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetToken;
import pl.barbershopproject.barbershop.passwordreset.PasswordResetTokenRepository;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.barbershopproject.barbershop.utils.testentities.PasswordResetTokenTestEntities.createPasswordResetToken;
import static pl.barbershopproject.barbershop.utils.testentities.PasswordResetTokenTestEntities.sha256Hex;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

class PasswordResetTokenRepositoryTest extends AbstractRepositoryTest {

    private static final Instant VALID_EXPIRY_DATE = Instant.parse("2030-01-01T10:00:00Z");

    private static final Instant LATER_VALID_EXPIRY_DATE = Instant.parse("2030-01-01T11:00:00Z");

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("save should persist password reset token and assign id")
    @Test
    void save_ShouldPersistPasswordResetTokenAndAssignId() {
        // given
        User user = saveUser("John", "Doe", "john.token.save@example.com");

        String hashedToken = sha256Hex("raw-reset-token");

        PasswordResetToken passwordResetToken = createPasswordResetToken(
                user,
                hashedToken,
                VALID_EXPIRY_DATE
        );

        // when
        PasswordResetToken savedToken = passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        // then
        assertThat(savedToken.getId()).isNotNull();
        assertThat(savedToken.getToken()).isEqualTo(hashedToken);
        assertThat(savedToken.getUser().getEmail()).isEqualTo("john.token.save@example.com");
        assertThat(savedToken.getExpiryDate()).isEqualTo(VALID_EXPIRY_DATE);
    }

    @DisplayName("findByToken should return token when token exists")
    @Test
    void findByToken_ShouldReturnToken_WhenTokenExists() {
        // given
        User user = saveUser("John", "Doe", "john.token.find@example.com");
        String hashedToken = sha256Hex("existing-reset-token");

        PasswordResetToken passwordResetToken = createPasswordResetToken(
                user,
                hashedToken,
                VALID_EXPIRY_DATE
        );

        passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        // when
        Optional<PasswordResetToken> foundToken = passwordResetTokenRepository.findByToken(hashedToken);

        // then
        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(hashedToken);
        assertThat(foundToken.get().getUser().getEmail()).isEqualTo("john.token.find@example.com");
        assertThat(foundToken.get().getExpiryDate()).isEqualTo(VALID_EXPIRY_DATE);
    }

    @DisplayName("findByToken should return empty when token does not exist")
    @Test
    void findByToken_ShouldReturnEmpty_WhenTokenDoesNotExist() {
        // when
        Optional<PasswordResetToken> foundToken =
                passwordResetTokenRepository.findByToken("missing-token");

        // then
        assertThat(foundToken).isEmpty();
    }

    @DisplayName("deleteByUser should remove all password reset tokens assigned to selected user")
    @Test
    void deleteByUser_ShouldRemoveAllTokensAssignedToSelectedUser() {
        // given
        User firstUser = saveUser("John", "Doe", "john.token.delete@example.com");
        User secondUser = saveUser("Jane", "Doe", "jane.token.delete@example.com");

        String firstUserToken = sha256Hex("first-user-token");
        String firstUserSecondToken = sha256Hex("first-user-second-token");
        String secondUserToken = sha256Hex("second-user-token");

        PasswordResetToken token1 = createPasswordResetToken(
                firstUser,
                firstUserToken,
                VALID_EXPIRY_DATE
        );

        PasswordResetToken token2 = createPasswordResetToken(
                firstUser,
                firstUserSecondToken,
                LATER_VALID_EXPIRY_DATE
        );

        PasswordResetToken token3 = createPasswordResetToken(
                secondUser,
                secondUserToken,
                VALID_EXPIRY_DATE
        );

        passwordResetTokenRepository.saveAllAndFlush(List.of(token1, token2, token3));

        // when
        passwordResetTokenRepository.deleteByUser(firstUser);
        passwordResetTokenRepository.flush();

        // then
        assertThat(passwordResetTokenRepository.findByToken(firstUserToken)).isEmpty();
        assertThat(passwordResetTokenRepository.findByToken(firstUserSecondToken)).isEmpty();
        assertThat(passwordResetTokenRepository.findByToken(secondUserToken)).isPresent();
    }

    @DisplayName("deleteByUser should do nothing when user has no password reset tokens")
    @Test
    void deleteByUser_ShouldDoNothing_WhenUserHasNoPasswordResetTokens() {
        // given
        User userWithoutTokens = saveUser(
                "John",
                "NoToken",
                "john.without.tokens@example.com"
        );

        User userWithToken = saveUser(
                "Jane",
                "WithToken",
                "jane.with.token@example.com"
        );

        String existingToken = sha256Hex("existing-token");

        PasswordResetToken passwordResetToken = createPasswordResetToken(
                userWithToken,
                existingToken,
                VALID_EXPIRY_DATE
        );

        passwordResetTokenRepository.saveAndFlush(passwordResetToken);

        // when
        passwordResetTokenRepository.deleteByUser(userWithoutTokens);
        passwordResetTokenRepository.flush();

        // then
        assertThat(passwordResetTokenRepository.findByToken(existingToken)).isPresent();
        assertThat(passwordResetTokenRepository.count()).isEqualTo(1);
    }

    private User saveUser(String firstname, String lastname, String email) {
        User user = createUser(firstname, lastname, email, Role.USER);
        user.setIdUser(null);

        return userRepository.saveAndFlush(user);
    }
}