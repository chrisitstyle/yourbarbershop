package pl.barbershopproject.barbershop.unit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import pl.barbershopproject.barbershop.controller.OfferController;
import pl.barbershopproject.barbershop.model.Offer;
import pl.barbershopproject.barbershop.service.OfferService;

import java.util.Arrays;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class OfferControllerTest {

    @Mock
    private OfferService offerService;

    @InjectMocks
    private OfferController offerController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addOfferSuccessTest() {
        Offer offer = new Offer();
        when(offerService.addOffer(any(Offer.class))).thenReturn(ResponseEntity.ok("Oferta została pomyślnie dodana"));

        ResponseEntity<String> responseEntity = offerController.addOffer(offer);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Oferta została pomyślnie dodana", responseEntity.getBody());
    }

    @Test
    void addOfferFailureTest() {
        Offer offer = new Offer();
        when(offerService.addOffer(any(Offer.class))).thenThrow(new RuntimeException("Wystąpił  błąd"));

        ResponseEntity<String> responseEntity = offerController.addOffer(offer);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertEquals("Nie udało się dodać oferty.", responseEntity.getBody());
    }

    @Test
    void getAllOffersTest() {
        List<Offer> offers = Arrays.asList(new Offer(), new Offer());
        when(offerService.getAllOffers()).thenReturn(offers);

        List<Offer> result = offerController.getAllOffers();

        assertEquals(offers, result);
    }

    @Test
    void getSingleOfferTest() {
        long offerId = 1L;
        Offer offer = new Offer();
        when(offerService.getSingleOffer(offerId)).thenReturn(offer);

        Offer result = offerController.getSingleOffer(offerId);

        assertEquals(offer, result);
    }

    @Test
    void updateOfferTest() {
        long offerId = 1L;
        Offer updatedOffer = new Offer();
        when(offerService.updateOffer(any(Offer.class), eq(offerId))).thenReturn(updatedOffer);

        Offer result = offerController.updateOffer(updatedOffer, offerId);

        assertEquals(updatedOffer, result);
    }

    @Test
    void deleteOfferByIdSuccessTest() {
        long offerId = 1L;
        doNothing().when(offerService).deleteOfferById(offerId);

        ResponseEntity<String> responseEntity = offerController.deleteOfferById(offerId);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Oferta o ID 1 została usunięta.", responseEntity.getBody());
    }


    @Test
    void deleteOfferByIdNotFoundTest() {
        long offerId = 1L;
        doThrow(new NoSuchElementException("Oferta nie zostala znaleziona")).when(offerService).deleteOfferById(offerId);

        ResponseEntity<String> responseEntity = offerController.deleteOfferById(offerId);

        assertEquals(HttpStatus.NOT_FOUND, responseEntity.getStatusCode());
        assertEquals("Oferta nie zostala znaleziona", responseEntity.getBody());
    }

}
