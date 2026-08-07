package pl.barbershopproject.barbershop.order;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createUnsavedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities.createOrder;
import static pl.barbershopproject.barbershop.utils.testentities.UserTestEntities.createUser;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class OrderRepositoryTest {

    private static final LocalDateTime ORDER_DATE =
            LocalDateTime.of(2026, Month.JANUARY,16,15,0
            );

    private static final LocalDateTime VISIT_DATE_NEXT_DAY =
            LocalDateTime.of(
                    2026,
                    Month.JANUARY,
                    17,
                    17,
                    0
            );

    private static final LocalDateTime VISIT_DATE_TWO_DAYS_LATER =
            LocalDateTime.of(
                    2026,
                    Month.JANUARY,
                    18,
                    17,
                    0
            );

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save should persist order and assign id")
    void save_persistsOrderAndAssignsId() {
        User savedUser = saveUser();
        Offer savedOffer = saveOffer();

        Order order = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_NEXT_DAY,
                OrderStatus.NOWE
        );

        Order savedOrder = orderRepository.saveAndFlush(order);

        assertThat(savedOrder.getIdOrder()).isPositive();

        assertThat(savedOrder.getUser().getEmail())
                .isEqualTo("johndoe@example.com");

        assertThat(savedOrder.getOffer().getKind())
                .isEqualTo("test_kind");

        assertThat(savedOrder.getBookedOffer()).isNotNull();

        assertThat(savedOrder.getBookedOffer().getName())
                .isEqualTo(savedOffer.getKind());

        assertThat(savedOrder.getBookedOffer().getPrice())
                .isEqualByComparingTo(savedOffer.getCost());
    }

    @Test
    @DisplayName("findById should return saved order with user, offer and snapshot")
    void findById_returnsOrder() {
        User savedUser = saveUser();
        Offer savedOffer = saveOffer();

        Order order = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_TWO_DAYS_LATER,
                OrderStatus.ZREALIZOWANE
        );

        Order savedOrder = orderRepository.saveAndFlush(order);
        Long orderId = savedOrder.getIdOrder();

        entityManager.clear();

        Optional<Order> foundOrder =
                orderRepository.findById(orderId);

        assertThat(foundOrder).isPresent();

        Order result = foundOrder.orElseThrow();

        assertThat(result.getUser().getEmail())
                .isEqualTo("johndoe@example.com");

        assertThat(result.getOffer().getKind())
                .isEqualTo("test_kind");

        assertThat(result.getBookedOffer()).isNotNull();

        assertThat(result.getBookedOffer().getName())
                .isEqualTo(savedOffer.getKind());

        assertThat(result.getBookedOffer().getPrice())
                .isEqualByComparingTo(savedOffer.getCost());

        assertThat(result.getOrderStatus())
                .isEqualTo(OrderStatus.ZREALIZOWANE);
    }

    @Test
    @DisplayName("save should persist historical booked offer snapshot")
    void save_persistsHistoricalBookedOfferSnapshot() {
        User savedUser = saveUser();
        Offer savedOffer = saveOffer();

        Order order = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_NEXT_DAY,
                OrderStatus.NOWE
        );

        order.setBookedOffer(createBookedOffer(
                "Historyczne strzyżenie",
                new BigDecimal("120.00")
        ));

        Order savedOrder = orderRepository.saveAndFlush(order);
        Long orderId = savedOrder.getIdOrder();

        entityManager.clear();

        Order foundOrder = orderRepository.findById(orderId)
                .orElseThrow();

        assertThat(foundOrder.getBookedOffer()).isNotNull();

        assertThat(foundOrder.getBookedOffer().getName())
                .isEqualTo("Historyczne strzyżenie");

        assertThat(foundOrder.getBookedOffer().getPrice())
                .isEqualByComparingTo("120.00");

        assertThat(foundOrder.getOffer().getKind())
                .isEqualTo(savedOffer.getKind());

        assertThat(foundOrder.getOffer().getCost())
                .isEqualByComparingTo(savedOffer.getCost());
    }

    @Test
    @DisplayName("findAll should return all saved orders with snapshots")
    void findAll_returnsAllOrders() {
        User savedUser = saveUser();
        Offer savedOffer = saveOffer();

        Order firstOrder = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_NEXT_DAY,
                OrderStatus.NOWE
        );

        Order secondOrder = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_TWO_DAYS_LATER,
                OrderStatus.ANULOWANE
        );

        orderRepository.saveAllAndFlush(
                List.of(firstOrder, secondOrder)
        );

        entityManager.clear();

        List<Order> orders = orderRepository.findAll();

        assertThat(orders)
                .hasSize(2)
                .extracting(Order::getOrderStatus)
                .containsExactlyInAnyOrder(
                        OrderStatus.NOWE,
                        OrderStatus.ANULOWANE
                );

        assertThat(orders)
                .allSatisfy(order -> {
                    assertThat(order.getBookedOffer()).isNotNull();

                    assertThat(order.getBookedOffer().getName())
                            .isEqualTo(savedOffer.getKind());

                    assertThat(order.getBookedOffer().getPrice())
                            .isEqualByComparingTo(savedOffer.getCost());
                });
    }

    @Test
    @DisplayName("deleteById should remove order from repository")
    void deleteById_removesOrder() {
        User savedUser = saveUser();
        Offer savedOffer = saveOffer();

        Order order = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_NEXT_DAY,
                OrderStatus.ZREALIZOWANE
        );

        Order savedOrder = orderRepository.saveAndFlush(order);
        Long orderId = savedOrder.getIdOrder();

        orderRepository.deleteById(orderId);
        orderRepository.flush();

        entityManager.clear();

        Optional<Order> foundOrder =
                orderRepository.findById(orderId);

        assertThat(foundOrder).isEmpty();
    }

    @Test
    @DisplayName("findOrdersByStatus should return only orders with specific orderStatus")
    void findOrdersByStatus_returnsOrdersWithGivenStatus() {
        User savedUser = saveUser();
        Offer savedOffer = saveOffer();

        Order newOrder = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_NEXT_DAY,
                OrderStatus.NOWE
        );

        Order cancelledOrder = createOrder(
                savedUser,
                savedOffer,
                ORDER_DATE,
                VISIT_DATE_TWO_DAYS_LATER,
                OrderStatus.ANULOWANE
        );

        orderRepository.saveAllAndFlush(
                List.of(newOrder, cancelledOrder)
        );

        entityManager.clear();

        List<Order> newOrders =
                orderRepository.findOrdersByStatus(OrderStatus.NOWE);

        assertThat(newOrders)
                .hasSize(1)
                .allSatisfy(order -> {
                    assertThat(order.getOrderStatus())
                            .isEqualTo(OrderStatus.NOWE);

                    assertThat(order.getBookedOffer())
                            .isNotNull();

                    assertThat(order.getBookedOffer().getName())
                            .isEqualTo(savedOffer.getKind());

                    assertThat(order.getBookedOffer().getPrice())
                            .isEqualByComparingTo(savedOffer.getCost());
                });
    }

    private User saveUser() {
        User user = createUser();
        user.setIdUser(null);

        return userRepository.saveAndFlush(user);
    }

    private Offer saveOffer() {
        Offer offer = createUnsavedOffer();

        return offerRepository.saveAndFlush(offer);
    }
}