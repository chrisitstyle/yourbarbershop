package pl.barbershopproject.barbershop.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.offer.dto.OfferCreationDTO;
import pl.barbershopproject.barbershop.offer.dto.UpdateOfferDTO;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class OfferService {

    private final OfferRepository offerRepository;

    @CacheEvict(value = "offers", allEntries = true)
    public Offer addOffer(OfferCreationDTO offerCreationDTO) {
        Offer offer = Offer.builder()
                .kind(offerCreationDTO.kind())
                .cost(offerCreationDTO.cost())
                .build();

        return offerRepository.save(offer);
    }

    @Cacheable(value = "offers")
    public List<Offer> getAllOffers() {
        return offerRepository.findAll();
    }

    @Cacheable(value = "offers", key = "#idOffer")
    public Offer getSingleOffer(Long idOffer) {
        return offerRepository.findById(idOffer)
                .orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje"));
    }

    @Transactional
    @CacheEvict(value = "offers", allEntries = true)
    public Offer updateOffer(UpdateOfferDTO updatedOffer, Long idOffer) {
        Offer existingOffer = offerRepository.findById(idOffer)
                .orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje"));

        existingOffer.setKind(updatedOffer.kind());
        existingOffer.setCost(updatedOffer.cost());

        return offerRepository.save(existingOffer);
    }

    @Transactional
    @CacheEvict(value = "offers", allEntries = true)
    public void deleteOfferById(Long idOffer) {
        if (!offerRepository.existsById(idOffer)) {
            throw new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje");
        }
        offerRepository.deleteById(idOffer);
    }
}
