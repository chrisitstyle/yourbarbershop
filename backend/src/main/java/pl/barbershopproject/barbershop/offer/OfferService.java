package pl.barbershopproject.barbershop.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

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
    public Offer getSingleOffer(long idOffer){
        return offerRepository.findById(idOffer).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public Offer updateOffer(Offer updatedOffer, long idOffer){
        return offerRepository.findById(idOffer)
                .map(offer -> {
                    offer.setKind(updatedOffer.getKind());
                    offer.setCost(updatedOffer.getCost());
                    return offerRepository.save(offer);
                }).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public void deleteOfferById(long idOffer) {
        Optional<Offer> offerExists = offerRepository.findById(idOffer);

        if (offerExists.isPresent()) {
            offerRepository.deleteById(idOffer);
        } else {
            throw new NoSuchElementException("Oferta o podanym ID nie istnieje");
        }
    }
}
