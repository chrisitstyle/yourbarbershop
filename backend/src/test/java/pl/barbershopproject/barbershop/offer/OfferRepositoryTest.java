package pl.barbershopproject.barbershop.offer;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import pl.barbershopproject.barbershop.utils.TestEntities;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class OfferRepositoryTest {

    @Autowired
    private OfferRepository offerRepository;

    @Test
    @DisplayName("save should persist offer and assign id")
    void save_persistsOffer_andAssignsId() {
        // given
        Offer offer = TestEntities.createOffer();
        offer.setIdOffer(null);
        // when
        Offer savedOffer = offerRepository.save(offer);
        // then
        assertThat(savedOffer.getIdOffer()).isGreaterThan(0);
        assertThat(savedOffer.getKind()).isEqualTo("test_kind");
        assertThat(savedOffer.getCost()).isEqualByComparingTo("120");
    }

    @Test
    @DisplayName("findById should return existed offer")
    void findById_returnsOffer() {
        // given
        Offer offer = TestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);
        // when
        Optional<Offer> found = offerRepository.findById(savedOffer.getIdOffer());
        // then
        assertThat(found).isPresent();
        assertThat(found.get().getKind()).isEqualTo("test_kind");
        assertThat(found.get().getCost()).isEqualByComparingTo("120.00");
    }

    @Test
    @DisplayName("findAll should return all offers")
    void findAll_returnsAllSavedOffers() {
        // given
        Offer o1 = TestEntities.createOffer("children cut", BigDecimal.valueOf(30));
        Offer o2 = TestEntities.createOffer("styling", BigDecimal.valueOf(70));
        offerRepository.saveAll(List.of(o1, o2));
        // when
        List<Offer> offers = offerRepository.findAll();
        // then
        assertThat(offers).extracting(Offer::getKind)
                .contains("children cut", "styling");
    }

    @Test
    @DisplayName("deleteById should remove offer")
    void deleteById_removesOffer() {
        // given
        Offer offer = TestEntities.createOffer();
        offer.setIdOffer(null);
        Offer savedOffer = offerRepository.save(offer);
        // when
        offerRepository.deleteById(savedOffer.getIdOffer());
        // then
        Optional<Offer> found = offerRepository.findById(savedOffer.getIdOffer());
        assertThat(found).isEmpty();
    }
}