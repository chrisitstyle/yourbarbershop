package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.security.AuthenticatedUser;
import pl.barbershopproject.barbershop.security.UserPrincipal;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.dto.*;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;

import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;

/**
 * Utility class providing factory methods for user-related test objects.
 * <p>
 * This class centralizes creation of {@link User} entities and user-related DTOs
 * used across tests. It provides default users, configurable users and common
 * user response objects.
 * </p>
 */
public final class UserTestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private UserTestEntities() {
    }

    /**
     * Creates a User instance for testing purposes.
     *
     * @param firstname the user's first name
     * @param lastname  the user's last name
     * @param email     the user's email address
     * @param role      the user's role
     * @return new User instance
     */
    public static User createUser(String firstname, String lastname, String email, Role role) {

        return User.builder()
                .firstname(firstname)
                .lastname(lastname)
                .email(email)
                .password("passwd")
                .role(role)
                .build();

    }

    /**
     * Returns a UserBuilder pre-filled with default test data (John Doe).
     * <p>
     * Useful when you need to override only specific fields in a test case.
     *
     * @return UserBuilder with default values set
     */
    public static User.UserBuilder userBuilder() {
        return User.builder()
                .idUser(1L)
                .firstname("John")
                .lastname("Doe")
                .email("johndoe@example.com")
                .password("test_password")
                .role(Role.USER);
    }

    /**
     * Creates a default User instance (John Doe) with standard test values.
     *
     * @return a default User instance
     */
    public static User createUser() {
        return userBuilder().build();
    }

    /**
     * Creates an admin User instance for tests.
     *
     * @return admin User instance
     */
    public static User createAdminUser() {
        return User.builder()
                .idUser(99L)
                .role(Role.ADMIN)
                .build();
    }

    /**
     * Creates a regular User instance with the given id for tests.
     *
     * @param id the user's id
     * @return regular User instance
     */
    public static User createRegularUser(Long id) {
        return User.builder()
                .idUser(id)
                .role(Role.USER)
                .build();
    }

    /**
     * Creates a default UserDTO with a sample list of orders.
     *
     * @return UserDTO instance
     */
    public static UserDTO createUserDTO() {
        UserOrdersDTO userOrders = createUserOrdersDTO();
        return new UserDTO(1L,
                "John",
                "Doe",
                "johndoe@example.com",
                Role.USER,
                List.of(userOrders));
    }

    /**
     * Creates a UserCreationDTO with default test data.
     *
     * @return UserCreationDTO instance
     */
    public static UserCreationDTO createUserCreationDTO() {

        return new UserCreationDTO("John", "Doe", "johndoe@example.com", "test_password"
                , "USER");

    }

    /**
     * Creates a UserProfileUpdateRequestDTO with default updated profile data.
     *
     * @return UserProfileUpdateRequestDTO instance
     */
    public static UserProfileUpdateRequestDTO createUserProfileUpdateRequestDTO() {
        return new UserProfileUpdateRequestDTO(
                "Jane",
                "Smith",
                "jane@smith.com"
        );
    }

    /**
     * Creates a UserResponseDTO corresponding to the default test user.
     *
     * @return UserResponseDTO instance
     */
    public static UserResponseDTO createUserResponseDTO() {
        return new UserResponseDTO(1L, "John", "Doe",
                "johndoe@example.com", Role.USER);
    }

    /**
     * Creates a UserOrdersDTO with a default offer and dates.
     *
     * @return UserOrdersDTO instance
     */
    public static UserOrdersDTO createUserOrdersDTO() {
        Offer offer = createOffer();
        return new UserOrdersDTO(
                10L,
                offer,
                LocalDateTime.of(2026, Month.JANUARY, 16, 15, 0),
                LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0),
                OrderStatus.NOWE,
                PaymentMethod.GOTOWKA,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC
        );
    }

    /**
     * Creates an authenticated user identity with default test data.
     * <p>
     * Defaults: userId=1L, role=USER.
     *
     * @return an AuthenticatedUser with default test data
     */
    public static AuthenticatedUser createAuthenticatedUser() {
        return new AuthenticatedUser(
                1L,
                Role.USER
        );
    }

    /**
     * Creates an authenticated user identity with custom user ID and role.
     *
     * @param userId ID of the authenticated user
     * @param role   role of the authenticated user
     * @return an AuthenticatedUser with the provided ID and role
     */
    public static AuthenticatedUser createAuthenticatedUser(
            long userId,
            Role role
    ) {
        return new AuthenticatedUser(
                userId,
                role
        );
    }

    /**
     * Creates an authenticated admin identity with default test data.
     * <p>
     * Defaults: userId=99L, role=ADMIN.
     *
     * @return an AuthenticatedUser representing an admin
     */
    public static AuthenticatedUser createAuthenticatedAdmin() {
        return new AuthenticatedUser(
                99L,
                Role.ADMIN
        );
    }

    /**
     * Creates a UserPrincipal from the provided User entity.
     *
     * @param user source user entity
     * @return a security principal containing the user's authentication data
     */
    public static UserPrincipal createUserPrincipal(User user) {
        return UserPrincipal.from(user);
    }

    /**
     * Creates a UserPrincipal with default test user data.
     *
     * @return a UserPrincipal with default test data
     */
    public static UserPrincipal createUserPrincipal() {
        return createUserPrincipal(createUser());
    }
}
