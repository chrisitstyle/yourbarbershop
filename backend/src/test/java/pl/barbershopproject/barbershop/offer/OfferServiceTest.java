package pl.barbershopproject.barbershop.offer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.offer.dto.OfferCreationDTO;
import pl.barbershopproject.barbershop.offer.dto.UpdateOfferDTO;
import pl.barbershopproject.barbershop.utils.testentities.OfferTestEntities;

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

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private OfferService offerService;

    private Offer offer;

    @BeforeEach
    void setUp() {
        offer = OfferTestEntities.createOffer();
    }

    @Test
    void addOffer_ShouldReturnOffer() {
        OfferCreationDTO offerCreationDTO = new OfferCreationDTO(
                offer.getKind(),
                offer.getCost()
        );

        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        Offer savedOffer = offerService.addOffer(offerCreationDTO);

        assertNotNull(savedOffer);
        assertEquals(offer.getIdOffer(), savedOffer.getIdOffer());
        assertEquals(offer.getKind(), savedOffer.getKind());
        assertEquals(offer.getCost(), savedOffer.getCost());

        verify(offerRepository, times(1)).save(argThat(saved ->
                saved.getKind().equals(offerCreationDTO.kind())
                        && saved.getCost().equals(offerCreationDTO.cost())
        ));
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void getAllOffers_ShouldReturnAllOffers() {
        when(offerRepository.findAll()).thenReturn(List.of(offer));

        List<Offer> allOffersReturn = offerService.getAllOffers();

        assertNotNull(allOffersReturn);
        verify(offerRepository, times(1)).findAll();
    }

    @Test
    void getSingleOffer_ShouldReturnSingleOffer() {
        long idOffer = 1L;
        when(offerRepository.findById(idOffer)).thenReturn(Optional.of(offer));

        Offer singleOffer = offerService.getSingleOffer(idOffer);

        assertNotNull(singleOffer);
    }

    @Test
    void updateOffer_ShouldUpdateAndReturnOffer_WhenOfferExists() {
        UpdateOfferDTO updatedOffer = new UpdateOfferDTO(
                "New Kind",
                new BigDecimal("150.0")
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.of(offer));
        when(offerRepository.save(any(Offer.class))).thenReturn(offer);

        Offer savedOffer = offerService.updateOffer(updatedOffer, 1L);

        assertNotNull(savedOffer);
        assertEquals("New Kind", savedOffer.getKind());
        assertEquals(new BigDecimal("150.0"), savedOffer.getCost());

        verify(offerRepository, times(1)).findById(1L);
        verify(offerRepository, times(1)).save(offer);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void updateOffer_ShouldThrowException_WhenOfferDoesNotExist() {
        UpdateOfferDTO updatedOffer = new UpdateOfferDTO(
                "New Kind",
                new BigDecimal("150.0")
        );

        when(offerRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> offerService.updateOffer(updatedOffer, 1L));

        verify(offerRepository, times(1)).findById(1L);
        verify(offerRepository, never()).save(any(Offer.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void deleteOfferById_ShouldDeleteOffer_WhenOfferExists() {
        when(offerRepository.existsById(1L)).thenReturn(true);

        offerService.deleteOfferById(1L);

        verify(offerRepository, times(1)).existsById(1L);
        verify(offerRepository, times(1)).deleteById(1L);
        verify(eventPublisher, times(1)).publishEvent(any(AuditEvent.class));
    }

    @Test
    void deleteOfferById_ShouldThrowException_WhenOfferDoesNotExist() {
        when(offerRepository.existsById(1L)).thenReturn(false);

        assertThrows(NoSuchElementException.class, () -> offerService.deleteOfferById(1L));

        verify(offerRepository, times(1)).existsById(1L);
        verify(offerRepository, never()).deleteById(1L);
        verify(eventPublisher, never()).publishEvent(any());
    }
}