package pl.barbershopproject.barbershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.barbershopproject.barbershop.model.Offer;
import pl.barbershopproject.barbershop.service.OfferService;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
public class OfferController {

    private final OfferService offerService;

    @PostMapping("/add")
    public ResponseEntity<String> addOffer(@RequestBody Offer offer) {
        ResponseEntity<String> response;
        try {
            response = offerService.addOffer(offer);
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie udało się dodać oferty.");
        }
        return response;
    }

    @GetMapping("/get")
    public List<Offer> getAllOffers(){
        return offerService.getAllOffers();
    }

    @GetMapping("/get/{id_offer}")
    public Offer getSingleOffer(@PathVariable long id_offer){
        return offerService.getSingleOffer(id_offer);
    }

    @PutMapping("/update/{id_offer}")
    public Offer updateOffer(@RequestBody Offer updatedOffer, @PathVariable long id_offer){
        return offerService.updateOffer(updatedOffer, id_offer);
    }

    @DeleteMapping("/delete/{id_offer}")
    public ResponseEntity<String> deleteOfferById(@PathVariable long id_offer) {
        try {
            offerService.deleteOfferById(id_offer);
            return new ResponseEntity<>("Oferta o ID " + id_offer + " została usunięta.", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
