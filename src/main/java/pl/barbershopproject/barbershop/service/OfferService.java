package pl.barbershopproject.barbershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.model.Offer;
import pl.barbershopproject.barbershop.repository.OfferRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class OfferService {

    private final OfferRepository offerRepository;


    public ResponseEntity<String> addOffer(Offer offer) {
        Offer savedOffer = offerRepository.save(offer);
        return ResponseEntity.status(HttpStatus.CREATED).body("Oferta została dodana.");
    }


    public List<Offer> getAllOffers(){
        return offerRepository.findAll();
    }
    public Offer getSingleOffer(long id_offer){
        return offerRepository.findById(id_offer).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public Offer updateOffer(Offer updatedOffer, long id_offer){
        return offerRepository.findById(id_offer)
                .map(offer -> {
                    offer.setKind(updatedOffer.getKind());
                    offer.setCost(updatedOffer.getCost());
                    return offerRepository.save(offer);
                }).orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public void deleteOfferById(long id_offer) {
        Optional<Offer> offerExists = offerRepository.findById(id_offer);

        if (offerExists.isPresent()) {
            offerRepository.deleteById(id_offer);
        } else {
            throw new NoSuchElementException("Oferta o podanym ID nie istnieje");
        }
    }
}
