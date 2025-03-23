package pl.barbershopproject.barbershop.unit.offer;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.offer.OfferService;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OfferServiceTest {

    @Mock
    private OfferRepository offerRepository;

    @InjectMocks
    private OfferService offerService;

    private Offer offer;

    @BeforeEach
    void setUp() {
         offer = new Offer();
        offer.setIdOffer(1L);
        offer.setKind("Test kind");
        offer.setCost(new BigDecimal("100"));

    }

    @Test
    void OfferService_AddOffer_ShouldReturnOffer(){

        when(offerRepository.save(offer)).thenReturn(offer);

        Offer savedOffer = offerService.addOffer(offer);

        assertNotNull(savedOffer);
       assertEquals(offer.getIdOffer(), savedOffer.getIdOffer());
         assertEquals(offer.getKind(), savedOffer.getKind());
        assertEquals(offer.getCost(), savedOffer.getCost());

        verify(offerRepository, times(1)).save(offer);


    }
    @Test
    void OfferService_GetAllOffers_ShouldReturnAllOffers(){

        when(offerRepository.findAll()).thenReturn(List.of(offer));

        List<Offer> allOffersReturn = offerService.getAllOffers();

        Assertions.assertNotNull(allOffersReturn);
        verify(offerRepository, times(1)).findAll();
    }

    @Test
    void OfferService_GetSingleOffer_ShouldReturnSingleOffer(){
        long idOffer = 1L;
        when(offerRepository.findById(idOffer)).thenReturn(Optional.of(offer));

        Offer singleOffer = offerService.getSingleOffer(idOffer);

        assertNotNull(singleOffer);

    }

    @Test
    void OfferService_UpdateOffer_ShouldUpdateAndReturnOffer_WhenOfferExists() {

        Offer updatedOffer = new Offer();
        updatedOffer.setKind("New Kind");
        updatedOffer.setCost(new BigDecimal("150.0"));
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        Offer savedOffer = offerService.updateOffer(updatedOffer, 1L);

        assertNotNull(savedOffer);
        assertEquals("New Kind", savedOffer.getKind());
        assertEquals(new BigDecimal("150.0"), savedOffer.getCost());

        verify(offerRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).save(offer);
    }

    @Test
   void OfferService_UpdateOffer_ShouldThrowException_WhenOfferDoesNotExist() {
        Offer updatedOffer = new Offer();

        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> offerService.updateOffer(updatedOffer, 1L));

        verify(offerRepository, times(1)).findById(1L);
        verify(offerRepository, never()).save(any(Offer.class));
    }

    @Test
    void OfferService_DeleteOfferById_ShouldDeleteOffer_WhenOfferExists() {
        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));

        offerService.deleteOfferById(1L);

        verify(offerRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).deleteById(1L);
    }


    @Test
    void OfferService_DeleteOfferById_ShouldThrowException_WhenOfferDoesNotExist() {
        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> offerService.deleteOfferById(1L));

        verify(offerRepository, times(1)).findById(1L);
        verify(offerRepository, never()).deleteById(1L);
    }

}
