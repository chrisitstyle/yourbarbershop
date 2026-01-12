package pl.barbershopproject.barbershop.guestorder;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.util.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@DataJpaTest
class GuestOrderRepositoryTest {

    @Autowired
    private GuestOrderRepository guestOrderRepository;
    @Autowired
    private OfferRepository offerRepository;

    @Test
    @DisplayName("save method should persist guest order and assign id")
    void save_persistsGuestOrder_andAssignsId() {
        Offer offer = createAndSaveOffer();
        GuestOrder guestOrder = createTestGuestOrder(offer, Status.NOWE);

        GuestOrder saved = guestOrderRepository.save(guestOrder);

        assertThat(saved.getIdGuestOrder()).isGreaterThan(0);
        assertThat(saved.getOffer().getKind()).isEqualTo("haircut");
        assertThat(saved.getStatus()).isEqualTo(Status.NOWE);
        assertThat(saved.getEmail()).isEqualTo("johndoe@example.com");
        assertThat(saved.getFirstname()).isEqualTo("John");
        assertThat(saved.getLastname()).isEqualTo("Doe");
        assertThat(saved.getPhonenumber()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("findById should return saved guest order with offer")
    void findById_returnsGuestOrder() {
        Offer offer = createAndSaveOffer();
        GuestOrder guestOrder = createTestGuestOrder(offer, Status.ANULOWANE);

        GuestOrder saved = guestOrderRepository.save(guestOrder);

        Optional<GuestOrder> found = guestOrderRepository.findById(saved.getIdGuestOrder());

        assertThat(found).isPresent();
        assertThat(found.get().getOffer().getKind()).isEqualTo("haircut");
        assertThat(found.get().getStatus()).isEqualTo(Status.ANULOWANE);
        assertThat(found.get().getEmail()).isEqualTo("johndoe@example.com");
        assertThat(found.get().getFirstname()).isEqualTo("John");
        assertThat(found.get().getLastname()).isEqualTo("Doe");
        assertThat(found.get().getPhonenumber()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("findAll should return all saved guest orders with offers")
    void findAll_returnsAllGuestOrders() {
        Offer offer = createAndSaveOffer();

        GuestOrder o1 = createTestGuestOrder(offer, Status.NOWE);
        GuestOrder o2 = createTestGuestOrder(offer, Status.ZREALIZOWANE);

        guestOrderRepository.saveAll(List.of(o1, o2));

        List<GuestOrder> guestOrders = guestOrderRepository.findAll();

        assertThat(guestOrders)
                .hasSize(2)
                .allSatisfy(ord -> {
                    assertThat(ord.getOffer()).isNotNull();
                    assertThat(ord.getEmail()).isEqualTo("johndoe@example.com");
                    assertThat(ord.getFirstname()).isEqualTo("John");
                    assertThat(ord.getLastname()).isEqualTo("Doe");
                    assertThat(ord.getPhonenumber()).isEqualTo("123456789");
                })
                .extracting(GuestOrder::getStatus)
                .contains(Status.NOWE, Status.ZREALIZOWANE);
    }

    @Test
    @DisplayName("deleteById should remove guest order from repository")
    void deleteById_removesGuestOrder() {
        Offer offer = createAndSaveOffer();
        GuestOrder guestOrder = createTestGuestOrder(offer, Status.NOWE);

        GuestOrder saved = guestOrderRepository.save(guestOrder);
        Long guestOrderId = saved.getIdGuestOrder();

        guestOrderRepository.deleteById(guestOrderId);

        Optional<GuestOrder> found = guestOrderRepository.findById(guestOrderId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findGuestOrdersByStatus should return only guest orders with specific status")
    void findGuestOrdersByStatus_returnsOrdersWithNOWEStatus() {
        Offer offer = createAndSaveOffer();

        GuestOrder o1 = createTestGuestOrder(offer, Status.NOWE);
        GuestOrder o2 = createTestGuestOrder(offer, Status.ANULOWANE);

        guestOrderRepository.saveAll(List.of(o1, o2));

        List<GuestOrder> noweOrders = guestOrderRepository.findGuestOrdersByStatus(Status.NOWE);

        assertThat(noweOrders)
                .hasSize(1)
                .allMatch(ord -> ord.getStatus() == Status.NOWE);
    }

    // utils
    private Offer createAndSaveOffer() {
        Offer offer = new Offer();
        offer.setKind("haircut");
        offer.setCost(BigDecimal.valueOf(50));
        return offerRepository.save(offer);
    }

    private GuestOrder createTestGuestOrder(Offer offer, Status status) {
        GuestOrder guestOrder = new GuestOrder();
        guestOrder.setFirstname("John");
        guestOrder.setLastname("Doe");
        guestOrder.setPhonenumber("123456789");
        guestOrder.setEmail("johndoe@example.com");
        guestOrder.setOrderDate(LocalDateTime.now());
        guestOrder.setVisitDate(LocalDateTime.now().plusDays(1));
        guestOrder.setOffer(offer);
        guestOrder.setStatus(status);
        return guestOrder;
    }
}