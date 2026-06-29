package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.dto.UserInOrderDTO;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.time.LocalDateTime;
import java.time.Month;

import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

/**
 * Utility class providing factory methods for order-related test objects.
 * <p>
 * This class helps create {@link Order} entities and order-related DTOs
 * with consistent default test data. It is intended to reduce duplicated
 * setup code in tests related to orders and user order summaries.
 * </p>
 */
public final class OrderTestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private OrderTestEntities() {
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
                .orderDate(LocalDateTime.of(2026, Month.JANUARY, 16, 15, 0))
                .visitDate(LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0))
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
                LocalDateTime.of(2026, Month.JANUARY, 16, 15, 0),
                LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0),
                Status.NOWE,
                PaymentMethod.GOTOWKA,
                PaymentStatus.OCZEKUJE_NA_PLATNOSC
        );
    }

    /**
     * Creates an OrderCreationDTO with default test data.
     * <p>
     * Useful for testing order creation endpoint and service.
     *
     * @return OrderCreationDTO instance
     */
    public static OrderCreationDTO createOrderCreationDTO() {
        return new OrderCreationDTO(
                1L,
                LocalDateTime.of(2026, Month.OCTOBER, 17, 17, 0),
                PaymentMethod.GOTOWKA
        );
    }

    /**
     * Creates an OrderUpdatedRequestDTO with default updated test data.
     * <p>
     * Useful for testing order update endpoint and service.
     *
     * @return OrderUpdatedRequestDTO instance
     */
    public static OrderUpdatedRequestDTO createOrderUpdatedRequestDTO() {
        return new OrderUpdatedRequestDTO(
                1L,
                LocalDateTime.of(2026, Month.JANUARY, 16, 12, 0),
                Status.NOWE
        );
    }
}
