package pl.barbershopproject.barbershop.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class OfferService {

    private final OfferRepository offerRepository;

    public Offer addOffer(Offer offer) {

        return offerRepository.save(offer);

    }

    public List<Offer> getAllOffers(){
        return offerRepository.findAll();
    }
    public Offer getSingleOffer(long idOffer) {
        return offerRepository.findById(idOffer)
                .orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje"));
    }

    @Transactional
    public Offer updateOffer(Offer updatedOffer, long idOffer) {
        Offer existingOffer = offerRepository.findById(idOffer).orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje"));
        existingOffer.setKind(updatedOffer.getKind());
        existingOffer.setCost(updatedOffer.getCost());
        return offerRepository.save(existingOffer);
    }

    @Transactional
    public void deleteOfferById(long idOffer) {
        if (!offerRepository.existsById(idOffer)) {
            throw new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje");
        }
        offerRepository.deleteById(idOffer);
    }
}
