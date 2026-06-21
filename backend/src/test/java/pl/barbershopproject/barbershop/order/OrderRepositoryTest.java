package pl.barbershopproject.barbershop.order;

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
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities;
import pl.barbershopproject.barbershop.utils.testentities.OrderTestEntities;
import pl.barbershopproject.barbershop.utils.testentities.UserTestEntities;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class OrderRepositoryTest {

    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OfferRepository offerRepository;

    @Test
    @DisplayName("save method should persist order and assign id")
    void save_persistsOrder_andAssignsId() {
        User user = UserTestEntities.createUser();
        user.setIdUser(null);
        User savedUser = userRepository.save(user);

        Offer offer = OfferTestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);


        Order order = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), Status.NOWE);
        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getIdOrder()).isGreaterThan(0);
        assertThat(savedOrder.getUser().getEmail()).isEqualTo("johndoe@example.com");
        assertThat(savedOrder.getOffer().getKind()).isEqualTo("test_kind");
    }

    @Test
    @DisplayName("findById should return saved order with user and offer")
    void findById_returnsOrder() {
        User user = UserTestEntities.createUser();
        user.setIdUser(null);
        User savedUser = userRepository.save(user);

        Offer offer = OfferTestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);

        Order order = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(),
                LocalDateTime.now().plusDays(2), Status.ZREALIZOWANE);
        Order savedOrder = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(savedOrder.getIdOrder());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("johndoe@example.com");
        assertThat(found.get().getOffer().getKind()).isEqualTo("test_kind");
        assertThat(found.get().getStatus()).isEqualTo(Status.ZREALIZOWANE);
    }

    @Test
    @DisplayName("findAll should return all saved orders with users and offers")
    void findAll_returnsAllOrders() {
        User user = UserTestEntities.createUser();
        user.setIdUser(null);
        User savedUser = userRepository.save(user);

        Offer offer = OfferTestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);

        Order o1 = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                Status.NOWE);

        Order o2 = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
                Status.ANULOWANE);

        orderRepository.saveAll(List.of(o1, o2));

        List<Order> orders = orderRepository.findAll();

        assertThat(orders)
                .hasSize(2)
                .extracting(Order::getStatus)
                .contains(Status.NOWE, Status.ANULOWANE);
    }

    @Test
    @DisplayName("deleteById should remove order from repository")
    void deleteById_removesOrder() {
        User user = UserTestEntities.createUser();
        user.setIdUser(null);
        User savedUser = userRepository.save(user);

        Offer offer = OfferTestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);

        Order order = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(),
                LocalDateTime.now().plusDays(1), Status.ZREALIZOWANE);

        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getIdOrder();

        orderRepository.deleteById(orderId);

        Optional<Order> found = orderRepository.findById(orderId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findOrdersByStatus should return only orders with specific status")
    void findOrdersByStatus_returnsOrdersWithGivenStatus() {
        User user = UserTestEntities.createUser();
        user.setIdUser(null);
        User savedUser = userRepository.save(user);

        Offer offer = OfferTestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);

        Order o1 = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                Status.NOWE);

        Order o2 = OrderTestEntities.createOrder(savedUser, savedOffer, LocalDateTime.now(), LocalDateTime.now().plusDays(2),
                Status.ANULOWANE);

        orderRepository.saveAll(List.of(o1, o2));

        List<Order> noweOrders = orderRepository.findOrdersByStatus(Status.NOWE);

        assertThat(noweOrders)
                .hasSize(1)
                .allMatch(ord -> ord.getStatus() == Status.NOWE);
    }

}
