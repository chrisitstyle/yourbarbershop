package pl.barbershopproject.barbershop.order;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.user.Role;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
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
        User user = createAndSaveUser();
        Offer offer = createAndSaveOffer();

        Order order = new Order();
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(LocalDateTime.now());
        order.setVisitDate(LocalDateTime.now().plusDays(1));
        order.setStatus(Status.NOWE);

        Order savedOrder = orderRepository.save(order);

        assertThat(savedOrder.getIdOrder()).isGreaterThan(1);
        assertThat(savedOrder.getUser().getEmail()).isEqualTo("test@user.com");
        assertThat(savedOrder.getOffer().getKind()).isEqualTo("haircut");
    }

    @Test
    @DisplayName("findById should return saved order with user and offer")
    void findById_returnsOrder() {
        User user = createAndSaveUser();
        Offer offer = createAndSaveOffer();

        Order order = new Order();
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(LocalDateTime.now());
        order.setVisitDate(LocalDateTime.now().plusDays(2));
        order.setStatus(Status.ZREALIZOWANE);
        Order savedOrder = orderRepository.save(order);

        Optional<Order> found = orderRepository.findById(savedOrder.getIdOrder());

        assertThat(found).isPresent();
        assertThat(found.get().getUser().getEmail()).isEqualTo("test@user.com");
        assertThat(found.get().getOffer().getKind()).isEqualTo("haircut");
        assertThat(found.get().getStatus()).isEqualTo(Status.ZREALIZOWANE);
    }

    @Test
    @DisplayName("findAll should return all saved orders with users and offers")
    void findAll_returnsAllOrders() {
        User user = createAndSaveUser();
        Offer offer = createAndSaveOffer();

        Order o1 = new Order();
        o1.setUser(user);
        o1.setOffer(offer);
        o1.setOrderDate(LocalDateTime.now());
        o1.setVisitDate(LocalDateTime.now().plusDays(1));
        o1.setStatus(Status.NOWE);

        Order o2 = new Order();
        o2.setUser(user);
        o2.setOffer(offer);
        o2.setOrderDate(LocalDateTime.now());
        o2.setVisitDate(LocalDateTime.now().plusDays(2));
        o2.setStatus(Status.ANULOWANE);

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
        User user = createAndSaveUser();
        Offer offer = createAndSaveOffer();

        Order order = new Order();
        order.setUser(user);
        order.setOffer(offer);
        order.setOrderDate(LocalDateTime.now());
        order.setVisitDate(LocalDateTime.now().plusDays(1));
        order.setStatus(Status.ZREALIZOWANE);

        Order savedOrder = orderRepository.save(order);
        Long orderId = savedOrder.getIdOrder();

        orderRepository.deleteById(orderId);

        Optional<Order> found = orderRepository.findById(orderId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findOrdersByStatus should return only orders with specific status")
    void findOrdersByStatus_returnsOrdersWithGivenStatus() {
        User user = createAndSaveUser();
        Offer offer = createAndSaveOffer();

        Order o1 = new Order();
        o1.setUser(user);
        o1.setOffer(offer);
        o1.setOrderDate(LocalDateTime.now());
        o1.setVisitDate(LocalDateTime.now().plusDays(1));
        o1.setStatus(Status.NOWE);

        Order o2 = new Order();
        o2.setUser(user);
        o2.setOffer(offer);
        o2.setOrderDate(LocalDateTime.now());
        o2.setVisitDate(LocalDateTime.now().plusDays(2));
        o2.setStatus(Status.ANULOWANE);

        orderRepository.saveAll(List.of(o1, o2));

        List<Order> noweOrders = orderRepository.findOrdersByStatus(Status.NOWE);

        assertThat(noweOrders)
                .hasSize(1)
                .allMatch(ord -> ord.getStatus() == Status.NOWE);
    }

    private User createAndSaveUser() {
        User user = User.builder()
                .email("test@user.com")
                .firstname("Test")
                .lastname("User")
                .password("pwd")
                .role(Role.USER)
                .build();
        return userRepository.save(user);
    }

    private Offer createAndSaveOffer() {
        Offer offer = new Offer();
        offer.setKind("haircut");
        offer.setCost(BigDecimal.valueOf(50));
        return offerRepository.save(offer);
    }
}
