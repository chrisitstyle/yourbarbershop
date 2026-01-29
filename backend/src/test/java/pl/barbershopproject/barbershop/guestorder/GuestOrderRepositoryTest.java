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
import pl.barbershopproject.barbershop.utils.TestEntities;

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
        Offer offer = offerRepository.save(TestEntities.createOffer());
        GuestOrder guestOrder = TestEntities.createGuestOrder();
        guestOrder.setOffer(offer);
        guestOrder.setIdGuestOrder(null);

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);

        assertThat(savedGuestOrder.getIdGuestOrder()).isGreaterThan(0);
        assertThat(savedGuestOrder.getOffer().getKind()).isEqualTo("test_kind");
        assertThat(savedGuestOrder.getStatus()).isEqualTo(Status.NOWE);
        assertThat(savedGuestOrder.getEmail()).isEqualTo("guestjohndoe@example.com");
        assertThat(savedGuestOrder.getFirstname()).isEqualTo("GuestJohn");
        assertThat(savedGuestOrder.getLastname()).isEqualTo("GuestDoe");
        assertThat(savedGuestOrder.getPhonenumber()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("findById should return saved guest order with offer")
    void findById_returnsGuestOrder() {
        Offer offer = offerRepository.save(TestEntities.createOffer());
        GuestOrder guestOrder = TestEntities.createGuestOrder();
        guestOrder.setOffer(offer);
        guestOrder.setIdGuestOrder(null);
        guestOrder.setStatus(Status.ANULOWANE);

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);

        Optional<GuestOrder> foundGuestOrder = guestOrderRepository.findById(savedGuestOrder.getIdGuestOrder());

        assertThat(foundGuestOrder).isPresent();
        assertThat(foundGuestOrder.get().getOffer().getKind()).isEqualTo("test_kind");
        assertThat(foundGuestOrder.get().getStatus()).isEqualTo(Status.ANULOWANE);
        assertThat(foundGuestOrder.get().getEmail()).isEqualTo("guestjohndoe@example.com");
        assertThat(foundGuestOrder.get().getFirstname()).isEqualTo("GuestJohn");
        assertThat(foundGuestOrder.get().getLastname()).isEqualTo("GuestDoe");
        assertThat(foundGuestOrder.get().getPhonenumber()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("findAll should return all saved guest orders with offers")
    void findAll_returnsAllGuestOrders() {
        Offer offer = offerRepository.save(TestEntities.createOffer());

        GuestOrder o1 = TestEntities.createGuestOrder();
        o1.setIdGuestOrder(null);
        o1.setOffer(offer);
        o1.setStatus(Status.NOWE);
        GuestOrder o2 = TestEntities.createGuestOrder();
        o2.setIdGuestOrder(null);
        o2.setOffer(offer);
        o2.setStatus(Status.ZREALIZOWANE);

        guestOrderRepository.saveAll(List.of(o1, o2));

        List<GuestOrder> guestOrders = guestOrderRepository.findAll();

        assertThat(guestOrders)
                .hasSize(2)
                .allSatisfy(ord -> {
                    assertThat(ord.getOffer()).isNotNull();
                    assertThat(ord.getEmail()).isEqualTo("guestjohndoe@example.com");
                    assertThat(ord.getFirstname()).isEqualTo("GuestJohn");
                    assertThat(ord.getLastname()).isEqualTo("GuestDoe");
                    assertThat(ord.getPhonenumber()).isEqualTo("123456789");
                })
                .extracting(GuestOrder::getStatus)
                .contains(Status.NOWE, Status.ZREALIZOWANE);
    }

    @Test
    @DisplayName("deleteById should remove guest order from repository")
    void deleteById_removesGuestOrder() {
        Offer offer = offerRepository.save(TestEntities.createOffer());
        GuestOrder guestOrder = TestEntities.createGuestOrder();
        guestOrder.setIdGuestOrder(null);
        guestOrder.setOffer(offer);

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);
        Long guestOrderId = savedGuestOrder.getIdGuestOrder();

        guestOrderRepository.deleteById(guestOrderId);

        Optional<GuestOrder> found = guestOrderRepository.findById(guestOrderId);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("findGuestOrdersByStatus should return only guest orders with specific status")
    void findGuestOrdersByStatus_returnsOrdersWithNOWEStatus() {
        Offer offer = offerRepository.save(TestEntities.createOffer());

        GuestOrder o1 = TestEntities.createGuestOrder();
        o1.setIdGuestOrder(null);
        o1.setOffer(offer);
        GuestOrder o2 = TestEntities.createGuestOrder();
        o2.setIdGuestOrder(null);
        o2.setOffer(offer);
        o2.setStatus(Status.ANULOWANE);
        guestOrderRepository.saveAll(List.of(o1, o2));

        List<GuestOrder> noweOrders = guestOrderRepository.findGuestOrdersByStatus(Status.NOWE);

        assertThat(noweOrders)
                .hasSize(1)
                .allMatch(ord -> ord.getStatus() == Status.NOWE);
    }

}