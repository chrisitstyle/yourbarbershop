package pl.barbershopproject.barbershop.offer;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/offers")
class OfferController {

    private final OfferService offerService;

    @PostMapping
    public ResponseEntity<Offer> addOffer(@Valid @RequestBody Offer offer) {
        Offer savedOffer = offerService.addOffer(offer);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedOffer.getIdOffer())
                .toUri();

        return ResponseEntity.created(location).body(savedOffer);
    }

    @GetMapping
    public List<Offer> getAllOffers() {
        return offerService.getAllOffers();
    }

    @GetMapping("/{idOffer}")
    public Offer getSingleOffer(@PathVariable long idOffer) {
        return offerService.getSingleOffer(idOffer);
    }

    @PutMapping("/{idOffer}")
    public Offer updateOffer(@Valid @RequestBody Offer updatedOffer,
                             @PathVariable long idOffer) {
        return offerService.updateOffer(updatedOffer, idOffer);
    }


    @DeleteMapping("/{idOffer}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOfferById(@PathVariable long idOffer) {
        offerService.deleteOfferById(idOffer);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
