package pl.barbershopproject.barbershop.guestorder;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import pl.barbershopproject.barbershop.integration.AbstractRepositoryTest;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static pl.barbershopproject.barbershop.utils.testentities.GuestOrderTestEntities.createUnsavedGuestOrder;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createBookedOffer;
import static pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities.createUnsavedOffer;

class GuestOrderRepositoryTest extends AbstractRepositoryTest {

    @Autowired
    private GuestOrderRepository guestOrderRepository;

    @Autowired
    private OfferRepository offerRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @DisplayName("save should persist guest order and assign id")
    void save_persistsGuestOrderAndAssignsId() {
        Offer savedOffer = saveOffer();

        GuestOrder guestOrder = createGuestOrder(
                savedOffer,
                OrderStatus.NOWE
        );

        GuestOrder savedGuestOrder =
                guestOrderRepository.saveAndFlush(guestOrder);

        assertThat(savedGuestOrder.getIdGuestOrder()).isPositive();

        assertThat(savedGuestOrder.getOffer()).isNotNull();

        assertThat(savedGuestOrder.getOffer().getKind()).isEqualTo(savedOffer.getKind());

        assertThat(savedGuestOrder.getBookedOffer()).isNotNull();

        assertThat(savedGuestOrder.getBookedOffer().getName()).isEqualTo(savedOffer.getKind());

        assertThat(savedGuestOrder.getBookedOffer().getPrice()).isEqualByComparingTo(savedOffer.getCost());

        assertThat(savedGuestOrder.getOrderStatus()).isEqualTo(OrderStatus.NOWE);

        assertThat(savedGuestOrder.getEmail()).isEqualTo("guestjohndoe@example.com");

        assertThat(savedGuestOrder.getFirstname()).isEqualTo("GuestJohn");

        assertThat(savedGuestOrder.getLastname()).isEqualTo("GuestDoe");

        assertThat(savedGuestOrder.getPhonenumber()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("findById should return saved guest order with offer and snapshot")
    void findById_returnsGuestOrder() {
        Offer savedOffer = saveOffer();

        GuestOrder guestOrder = createGuestOrder(
                savedOffer,
                OrderStatus.ANULOWANE
        );

        GuestOrder savedGuestOrder =
                guestOrderRepository.saveAndFlush(guestOrder);

        Long guestOrderId =
                savedGuestOrder.getIdGuestOrder();

        entityManager.clear();

        Optional<GuestOrder> foundGuestOrder =
                guestOrderRepository.findById(guestOrderId);

        assertThat(foundGuestOrder).isPresent();

        GuestOrder result = foundGuestOrder.orElseThrow();

        assertThat(result.getOffer()).isNotNull();

        assertThat(result.getOffer().getKind()).isEqualTo(savedOffer.getKind());

        assertThat(result.getBookedOffer()).isNotNull();

        assertThat(result.getBookedOffer().getName()).isEqualTo(savedOffer.getKind());

        assertThat(result.getBookedOffer().getPrice()).isEqualByComparingTo(savedOffer.getCost());

        assertThat(result.getOrderStatus()).isEqualTo(OrderStatus.ANULOWANE);

        assertThat(result.getEmail()).isEqualTo("guestjohndoe@example.com");

        assertThat(result.getFirstname()).isEqualTo("GuestJohn");

        assertThat(result.getLastname()).isEqualTo("GuestDoe");

        assertThat(result.getPhonenumber()).isEqualTo("123456789");
    }

    @Test
    @DisplayName("save should persist historical booked offer snapshot")
    void save_persistsHistoricalBookedOfferSnapshot() {
        Offer savedOffer = saveOffer();

        GuestOrder guestOrder = createGuestOrder(
                savedOffer,
                OrderStatus.NOWE
        );

        guestOrder.setBookedOffer(createBookedOffer(
                "Booked strzyżenie gościa",
                new BigDecimal("130.00")
        ));

        GuestOrder savedGuestOrder =
                guestOrderRepository.saveAndFlush(guestOrder);

        Long guestOrderId =
                savedGuestOrder.getIdGuestOrder();

        entityManager.clear();

        GuestOrder foundGuestOrder =
                guestOrderRepository.findById(guestOrderId)
                        .orElseThrow();

        assertThat(foundGuestOrder.getBookedOffer())
                .isNotNull();

        assertThat(foundGuestOrder.getBookedOffer().getName())
                .isEqualTo("Booked strzyżenie gościa");

        assertThat(foundGuestOrder.getBookedOffer().getPrice())
                .isEqualByComparingTo("130.00");

        assertThat(foundGuestOrder.getOffer().getKind())
                .isEqualTo(savedOffer.getKind());

        assertThat(foundGuestOrder.getOffer().getCost())
                .isEqualByComparingTo(savedOffer.getCost());
    }

    @Test
    @DisplayName("findAll should return all saved guest orders with snapshots")
    void findAll_returnsAllGuestOrders() {
        Offer savedOffer = saveOffer();

        GuestOrder firstGuestOrder = createGuestOrder(
                savedOffer,
                OrderStatus.NOWE
        );

        GuestOrder secondGuestOrder = createGuestOrder(savedOffer, OrderStatus.ZREALIZOWANE);

        guestOrderRepository.saveAllAndFlush(List.of(firstGuestOrder, secondGuestOrder)
        );

        entityManager.clear();

        List<GuestOrder> guestOrders =
                guestOrderRepository.findAll();

        assertThat(guestOrders)
                .hasSize(2)
                .allSatisfy(guestOrder -> {
                    assertThat(guestOrder.getOffer())
                            .isNotNull();

                    assertThat(guestOrder.getBookedOffer())
                            .isNotNull();

                    assertThat(guestOrder.getBookedOffer().getName())
                            .isEqualTo(savedOffer.getKind());

                    assertThat(guestOrder.getBookedOffer().getPrice())
                            .isEqualByComparingTo(savedOffer.getCost());

                    assertThat(guestOrder.getEmail())
                            .isEqualTo("guestjohndoe@example.com");

                    assertThat(guestOrder.getFirstname())
                            .isEqualTo("GuestJohn");

                    assertThat(guestOrder.getLastname())
                            .isEqualTo("GuestDoe");

                    assertThat(guestOrder.getPhonenumber())
                            .isEqualTo("123456789");
                });

        assertThat(guestOrders)
                .extracting(GuestOrder::getOrderStatus)
                .containsExactlyInAnyOrder(
                        OrderStatus.NOWE,
                        OrderStatus.ZREALIZOWANE
                );
    }

    @Test
    @DisplayName("deleteById should remove guest order from repository")
    void deleteById_removesGuestOrder() {
        Offer savedOffer = saveOffer();

        GuestOrder guestOrder = createGuestOrder(
                savedOffer,
                OrderStatus.NOWE
        );

        GuestOrder savedGuestOrder =
                guestOrderRepository.saveAndFlush(guestOrder);

        Long guestOrderId =
                savedGuestOrder.getIdGuestOrder();

        guestOrderRepository.deleteById(guestOrderId);
        guestOrderRepository.flush();

        entityManager.clear();

        Optional<GuestOrder> foundGuestOrder =
                guestOrderRepository.findById(guestOrderId);

        assertThat(foundGuestOrder).isEmpty();
    }

    @Test
    @DisplayName("findGuestOrdersByStatus should return only orders with specific orderStatus")
    void findGuestOrdersByStatus_returnsOrdersWithGivenStatus() {
        Offer savedOffer = saveOffer();

        GuestOrder newGuestOrder = createGuestOrder(savedOffer, OrderStatus.NOWE);

        GuestOrder cancelledGuestOrder = createGuestOrder(savedOffer, OrderStatus.ANULOWANE);

        guestOrderRepository.saveAllAndFlush(
                List.of(newGuestOrder, cancelledGuestOrder)
        );

        entityManager.clear();

        List<GuestOrder> newGuestOrders =
                guestOrderRepository.findGuestOrdersByStatus(
                        OrderStatus.NOWE
                );

        assertThat(newGuestOrders)
                .hasSize(1)
                .allSatisfy(guestOrder -> {
                    assertThat(guestOrder.getOrderStatus()).isEqualTo(OrderStatus.NOWE);

                    assertThat(guestOrder.getOffer()).isNotNull();

                    assertThat(guestOrder.getBookedOffer()).isNotNull();

                    assertThat(guestOrder.getBookedOffer().getName()).isEqualTo(savedOffer.getKind());

                    assertThat(guestOrder.getBookedOffer().getPrice()).isEqualByComparingTo(savedOffer.getCost());
                });
    }

    private Offer saveOffer() {
        return offerRepository.saveAndFlush(
                createUnsavedOffer()
        );
    }

    private GuestOrder createGuestOrder(
            Offer offer,
            OrderStatus orderStatus
    ) {
        GuestOrder guestOrder =
                createUnsavedGuestOrder();

        guestOrder.setIdGuestOrder(null);
        guestOrder.setOffer(offer);
        guestOrder.setBookedOffer(
                createBookedOffer(offer)
        );
        guestOrder.setOrderStatus(orderStatus);

        return guestOrder;
    }
}