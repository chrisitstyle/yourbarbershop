package pl.barbershopproject.barbershop.utils.testentities;

import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.dto.BookedOfferDTO;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.dto.UserInOrderDTO;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;
import java.time.Month;

import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createOffer;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

/**
 * Utility class providing factory methods for order-related test objects.
 *
 * <p>This class helps create {@link Order} entities and order-related DTOs
 * with consistent default test data. It is intended to reduce duplicated
 * setup code in tests related to orders and user order summaries.</p>
 */
public final class OrderTestEntities {

    /**
     * Private constructor to block instantiation of utility class.
     */
    private OrderTestEntities() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Returns an OrderBuilder pre-filled with default test data.
     *
     * <p>The offer and booked offer snapshot are created from the same
     * {@link Offer} instance.</p>
     *
     * @return order builder with default values
     */
    public static Order.OrderBuilder orderBuilder() {
        Offer offer = createOffer();

        return Order.builder()
                .idOrder(10L)
                .user(createUser())
                .offer(offer)
                .bookedOffer(createBookedOffer(offer))
                .orderDate(LocalDateTime.of(
                        2026,
                        Month.JANUARY,
                        16,
                        15,
                        0
                ))
                .visitDate(LocalDateTime.of(
                        2026,
                        Month.OCTOBER,
                        17,
                        17,
                        0
                ))
                .status(Status.NOWE);
    }

    /**
     * Creates a default Order instance.
     *
     * @return default order
     */
    public static Order createOrder() {
        return orderBuilder().build();
    }

    /**
     * Creates an Order instance for testing purposes.
     *
     * @param user user placing the order
     * @param offer offer associated with the order
     * @param orderDate date the order was placed
     * @param visitDate date the visit is scheduled
     * @param status order status
     * @return new Order instance
     */
    public static Order createOrder(
            User user,
            Offer offer,
            LocalDateTime orderDate,
            LocalDateTime visitDate,
            Status status
    ) {
        return Order.builder()
                .user(user)
                .offer(offer)
                .bookedOffer(createBookedOffer(offer))
                .orderDate(orderDate)
                .visitDate(visitDate)
                .status(status)
                .build();
    }

    /**
     * Creates a UserInOrderDTO with default test data.
     *
     * @return user information embedded in an order DTO
     */
    public static UserInOrderDTO createUserInOrderDTO() {
        return new UserInOrderDTO(
                1L,
                "John",
                "Doe",
                "johndoe@example.com"
        );
    }

    /**
     * Creates a complete OrderDTO with default test data.
     *
     * @return default order DTO
     */
    public static OrderDTO createOrderDTO() {
        Offer offer = createOffer();

        BookedOfferDTO bookedOfferDTO = new BookedOfferDTO(
                offer.getIdOffer(),
                offer.getKind(),
                offer.getCost()
        );

        return new OrderDTO(
                1L,
                createUserInOrderDTO(),
                bookedOfferDTO,
                LocalDateTime.of(
                        2026,
                        Month.JANUARY,
                        16,
                        15,
                        0
                ),
                LocalDateTime.of(
                        2026,
                        Month.OCTOBER,
                        17,
                        17,
                        0
                ),
                Status.NOWE,
                PaymentMethod.GOTOWKA,
                PaymentStatus.NIE_WYMAGANA
        );
    }

    /**
     * Creates an OrderCreationDTO with default test data.
     *
     * @return default order creation request
     */
    public static OrderCreationDTO createOrderCreationDTO() {
        return new OrderCreationDTO(
                1L,
                LocalDateTime.of(
                        2026,
                        Month.OCTOBER,
                        17,
                        17,
                        0
                ),
                PaymentMethod.GOTOWKA
        );
    }

    /**
     * Creates an OrderUpdatedRequestDTO with default updated test data.
     *
     * @return default order update request
     */
    public static OrderUpdatedRequestDTO createOrderUpdatedRequestDTO() {
        return new OrderUpdatedRequestDTO(
                1L,
                LocalDateTime.of(
                        2026,
                        Month.JANUARY,
                        16,
                        12,
                        0
                ),
                Status.NOWE
        );
    }
}