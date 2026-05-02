package pl.barbershopproject.barbershop.user;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.OrderRepository;
import pl.barbershopproject.barbershop.util.Status;
import pl.barbershopproject.barbershop.utils.TestEntities;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private OfferRepository offerRepository;
    @Autowired
    private OrderRepository orderRepository;

    @Test
    @DisplayName("save method should persist user to the database and assign id")
    void save_persistsUserToDatabase() {
        User user = TestEntities.createUser();
        user.setEmail("johndoe@saveexample.com");
        user.setIdUser(null);
        // when
        User savedUser = userRepository.save(user);
        // then
        assertThat(savedUser.getIdUser()).isNotNull();
        Optional<User> found = userRepository.findByEmail("johndoe@saveexample.com");
        assertThat(found).isPresent();
        assertThat(found.get().getFirstname()).isEqualTo("John");
    }

    @Test
    @DisplayName("existsByEmail should return true for existing email")
    void existsByEmail_existingEmail_returnsTrue() {
        // given
        User user = TestEntities.createUser();
        user.setEmail("johndoe@existsexample.com");
        user.setIdUser(null);
        userRepository.save(user);

        // when
        boolean result = userRepository.existsByEmail("johndoe@existsexample.com");
        // then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("existsByEmail should return false for not existing email")
    void existsByEmail_nonExistingEmail_returnsFalse() {
        // when
        boolean result = userRepository.existsByEmail("notfound@example.com");
        // then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("findByEmail should return user for existing email")
    void findByEmail_existingEmail_returnsUser() {
        // given
        User user = TestEntities.createUser();
        user.setEmail("johnwick@example.com");
        user.setIdUser(null);
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findByEmail("johnwick@example.com");
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getFirstname()).isEqualTo("John");
    }

    @Test
    @DisplayName("findByEmail should return empty for not existing email")
    void findByEmail_nonExistingEmail_returnsEmpty() {
        // when
        Optional<User> found = userRepository.findByEmail("nonexists@example.com");
        // then
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findById should return user with orders")
    void findById_existingId_returnsUserWithOrders() {
        // given
        User user = TestEntities.createUser();
        user.setEmail("order@me.com");
        user.setIdUser(null);
        User savedUser = userRepository.save(user);
        // when
        Optional<User> found = userRepository.findById(savedUser.getIdUser());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("order@me.com");
    }

    @Test
    @DisplayName("findAllWithOrders should return all users")
    void findAllWithOrders_returnsAllUsers() {
        // given
        User user1 = TestEntities.createUser();
        user1.setIdUser(null);

        User user2 = TestEntities.createUser("Negan", "Smith", "negansmith@example.com", Role.ADMIN);
        user2.setIdUser(null);

        userRepository.saveAll(List.of(user1, user2));
        // when
        List<User> users = userRepository.findAllWithOrders();

        // then
        assertThat(users).isNotEmpty();
        assertThat(users).extracting(User::getEmail)
                .contains("johndoe@example.com", "negansmith@example.com");
    }

    @Test
    @DisplayName("findById returns user with orders and offers")
    void findById_userHasOrderWithOffer_returnsUserWithOrderAndOffer() {
        // given
        User user = TestEntities.createUser();
        user.setIdUser(null);
        userRepository.save(user);

        Offer offer = TestEntities.createOffer("strzyżenie", BigDecimal.valueOf(35.0));
        offerRepository.save(offer);

        Order order = TestEntities.createOrder(user, offer, LocalDateTime.now(), LocalDateTime.now().plusDays(1),
                Status.NOWE);

        orderRepository.save(order);

        user.getUserOrders().add(order);
        userRepository.save(user);

        // when
        Optional<User> found = userRepository.findById(user.getIdUser());

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getUserOrders()).hasSize(1);
        assertThat(found.get().getUserOrders().getFirst().getOffer().getKind()).isEqualTo("strzyżenie");
    }

    @Test
    @DisplayName("deleteById should remove user from repository")
    void deleteById_removesUser() {
        // given
        User user = TestEntities.createUser();
        user.setIdUser(null);
        User savedUser = userRepository.save(user);
        Long userId = savedUser.getIdUser();
        // when
        userRepository.deleteById(userId);
        // then
        Optional<User> found = userRepository.findById(userId);
        assertThat(found).isEmpty();
    }
}