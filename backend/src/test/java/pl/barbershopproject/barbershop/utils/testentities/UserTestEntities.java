package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserOrdersDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;
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
                LocalDateTime.of(2026, 1, 16, 15, 0),
                LocalDateTime.of(2026, 10, 17, 17, 0),
                Status.NOWE
        );
    }
}
