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
             offerService.addOffer(offer);
            response = ResponseEntity.status(HttpStatus.CREATED).body("Oferta została dodana.");
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie udało się dodać oferty.");
        }
        return response;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Offer>> getAllOffers() {
        try {
            List<Offer> offers = offerService.getAllOffers();
            return ResponseEntity.ok(offers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/get/{id_offer}")
    public ResponseEntity<Offer> getSingleOffer(@PathVariable long id_offer) {
        try {
            Offer offer = offerService.getSingleOffer(id_offer);
            return new ResponseEntity<>(offer, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update/{id_offer}")
    public ResponseEntity<Object> updateOffer(@RequestBody Offer updatedOffer, @PathVariable long id_offer) {
        try {
            Offer updated = offerService.updateOffer(updatedOffer, id_offer);
            return new ResponseEntity<>(updated, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>("Oferta nie znaleziona", HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>("Wystąpił błąd podczas aktualizacji oferty", HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
