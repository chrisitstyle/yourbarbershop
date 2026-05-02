package pl.barbershopproject.barbershop.utils;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.UserInOrderDTO;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.dto.UserCreationDTO;
import pl.barbershopproject.barbershop.user.dto.UserDTO;
import pl.barbershopproject.barbershop.user.dto.UserOrdersDTO;
import pl.barbershopproject.barbershop.user.dto.UserResponseDTO;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Utility class providing static factory methods for creating test entities (User, Offer, Order, GuestOrder) and DTOs.
 * <p>
 * Useful for building entities with minimal boilerplate in JPA/Spring Data test scenarios.
 * It provides both complete object creation and Builders pre-filled with default test data.
 */

public class TestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private TestEntities() {
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
     * Returns an OfferBuilder pre-filled with default test data.
     *
     * @return OfferBuilder with default values set
     */
    public static Offer.OfferBuilder offerBuilder() {
        return Offer.builder()
                .idOffer(1L)
                .kind("test_kind")
                .cost(BigDecimal.valueOf(120));
    }

    /**
     * Creates an Offer instance with specified kind and cost.
     *
     * @param kind the type/kind of the offer
     * @param cost the cost of the offer
     * @return new Offer instance
     */
    public static Offer createOffer(String kind, BigDecimal cost) {

        return Offer.builder()
                .kind(kind)
                .cost(cost)
                .build();
    }

    /**
     * Creates an Offer instance with specified ID, kind, and cost.
     *
     * @param idOffer the ID of the offer
     * @param kind    the type/kind of the offer
     * @param cost    the cost of the offer
     * @return new Offer instance
     */
    public static Offer createOffer(Long idOffer, String kind, BigDecimal cost) {

        return Offer.builder()
                .idOffer(idOffer)
                .kind(kind)
                .cost(cost)
                .build();
    }

    /**
     * Creates a default Offer instance.
     */
    public static Offer createOffer() {
        return offerBuilder().build();
    }

    /**
     * Creates an Offer instance specifically for JPA testing (NO ID set).
     * Hibernate requires ID to be null for a successful INSERT.
     */
    public static Offer createUnsavedOffer() {
        return Offer.builder()
                .kind("test_kind")
                .cost(BigDecimal.valueOf(150))
                .build(); // id remains null
    }

    /**
     * Returns an OrderBuilder pre-filled with default test data.
     * <p>
     * Uses default User and Offer.
     */
    public static Order.OrderBuilder orderBuilder() {
        return Order.builder()
                .idOrder(10L)
                .user(createUser())
                .offer(createOffer())
                .orderDate(LocalDateTime.of(2026, 1, 16, 15, 0))
                .visitDate(LocalDateTime.of(2026, 10, 17, 17, 0))
                .status(Status.NOWE);
    }

    /**
     * Creates a default Order instance.
     */
    public static Order createOrder() {
        return orderBuilder().build();
    }

    /**
     * Creates an Order instance for testing purposes.
     *
     * @param user      the user placing the order
     * @param offer     the offer associated with the order
     * @param orderDate the date the order was placed
     * @param visitDate the date the visit is scheduled
     * @param status    the order status
     * @return new Order instance
     */
    public static Order createOrder(User user, Offer offer, LocalDateTime orderDate, LocalDateTime visitDate, Status status) {
        return Order.builder()
                .user(user)
                .offer(offer)
                .orderDate(orderDate)
                .visitDate(visitDate)
                .status(status)
                .build();
    }

    /**
     * Creates a UserInOrderDTO with default test data (John Doe).
     * <p>
     * This DTO represents the simplified user information embedded within an OrderDTO.
     *
     * @return UserInOrderDTO instance
     */
    public static UserInOrderDTO createUserInOrderDTO() {
        return new UserInOrderDTO(1L, "John", "Doe", "johndoe@example.com");
    }

    /**
     * Creates a complete OrderDTO with default test data.
     * <p>
     * Includes nested UserInOrderDTO and Offer objects,
     * and status set to NOWE.
     *
     * @return OrderDTO instance
     */
    public static OrderDTO createOrderDTO() {
        return new OrderDTO(
                1L,
                createUserInOrderDTO(),
                createOffer(),
                LocalDateTime.of(2026, 1, 16, 15, 0),
                LocalDateTime.of(2026, 10, 17, 17, 0),
                Status.NOWE
        );
    }

    /**
     * Returns a GuestOrderBuilder pre-filled with default test data.
     */
    public static GuestOrder.GuestOrderBuilder guestOrderBuilder() {
        return GuestOrder.builder()
                .idGuestOrder(1L)
                .firstname("GuestJohn")
                .lastname("GuestDoe")
                .phonenumber("123456789")
                .email("guestjohndoe@example.com")
                .offer(createOffer())
                .orderDate(LocalDateTime.of(2026, 1, 16, 15, 0))
                .visitDate(LocalDateTime.of(2026, 10, 17, 17, 0))
                .status(Status.NOWE);
    }

    /**
     * Creates a default GuestOrder instance.
     */
    public static GuestOrder createGuestOrder() {
        return guestOrderBuilder().build();
    }

    /**
     * Creates a GuestOrder instance for testing purposes.
     *
     * @param firstname   guest's first name
     * @param lastname    guest's last name
     * @param phonenumber guest's phone number
     * @param email       guest's email address
     * @param offer       offer associated with the guest order
     * @param orderDate   date the guest order was placed
     * @param visitDate   scheduled visit date
     * @param status      guest order status
     * @return new GuestOrder instance
     */
    public static GuestOrder createGuestOrder(String firstname, String lastname, String phonenumber,
                                              String email, Offer offer, LocalDateTime orderDate,
                                              LocalDateTime visitDate, Status status) {

        return GuestOrder.builder()
                .firstname(firstname)
                .lastname(lastname)
                .phonenumber(phonenumber)
                .email(email)
                .offer(offer)
                .orderDate(orderDate)
                .visitDate(visitDate)
                .status(status)
                .build();

    }

    /**
     * Creates a GuestOrder instance specifically for JPA testing (NO ID set).
     * Hibernate requires ID to be null for a successful INSERT.
     */
    public static GuestOrder createUnsavedGuestOrder() {
        return GuestOrder.builder()
                .firstname("GuestJohn")
                .lastname("GuestDoe")
                .phonenumber("123456789")
                .email("guestjohndoe@example.com")
                .orderDate(LocalDateTime.of(2026, 1, 16, 15, 0))
                .visitDate(LocalDateTime.of(2026, 10, 17, 17, 0))
                .status(Status.NOWE)
                .build(); // ID and Offer remain null (must be set manually)
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
