package pl.barbershopproject.barbershop.utils;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
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
                .password("passwd")
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
    public static Offer createOffer(long idOffer, String kind, BigDecimal cost) {

        return Offer.builder()
                .idOffer(idOffer)
                .kind(kind)
                .cost(cost)
                .build();
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
        Order order = new Order();
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(orderDate);
        order.setVisitDate(visitDate);
        order.setStatus(status);
        return order;
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

        GuestOrder guestOrder = new GuestOrder();
        guestOrder.setFirstname(firstname);
        guestOrder.setLastname(lastname);
        guestOrder.setPhonenumber(phonenumber);
        guestOrder.setEmail(email);
        guestOrder.setOffer(offer);
        guestOrder.setOrderDate(orderDate);
        guestOrder.setVisitDate(visitDate);
        guestOrder.setStatus(status);
        return guestOrder;

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
        return new UserResponseDTO(1L, "johndoe@example.com", "John",
                "Doe", Role.USER);
    }

    /**
     * Creates a UserOrdersDTO with a default offer and dates.
     *
     * @return UserOrdersDTO instance
     */
    public static UserOrdersDTO createUserOrdersDTO() {
        Offer offer = createOffer(1, "test_kind", BigDecimal.valueOf(120));
        return new UserOrdersDTO(
                10L,
                offer,
                LocalDateTime.of(2024, 10, 23, 15, 0),
                LocalDateTime.of(2024, 10, 24, 17, 0),
                Status.NOWE

        );
    }
}
