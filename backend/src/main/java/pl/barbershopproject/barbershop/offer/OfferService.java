package pl.barbershopproject.barbershop.offer;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.offer.dto.OfferCreationDTO;
import pl.barbershopproject.barbershop.offer.dto.UpdateOfferDTO;

import java.math.BigDecimal;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class OfferService {

    private final OfferRepository offerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @CacheEvict(value = "offers", allEntries = true)
    public Offer addOffer(OfferCreationDTO offerCreationDTO) {
        Offer offer = Offer.builder()
                .kind(offerCreationDTO.kind())
                .cost(offerCreationDTO.cost())
                .build();

        Offer savedOffer = offerRepository.save(offer);

        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.OFFER_CREATED,
                EntityType.OFFER,
                String.valueOf(savedOffer.getIdOffer()),
                String.format("{\"kind\":\"%s\", \"cost\":%s}", savedOffer.getKind(), savedOffer.getCost())
        ));

        return savedOffer;
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

        String oldKind = existingOffer.getKind();
        BigDecimal oldCost = existingOffer.getCost();

        existingOffer.setKind(updatedOffer.kind());
        existingOffer.setCost(updatedOffer.cost());

        Offer savedOffer = offerRepository.save(existingOffer);

        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.OFFER_UPDATED,
                EntityType.OFFER,
                String.valueOf(idOffer),
                String.format("{\"oldKind\":\"%s\", \"newKind\":\"%s\", \"oldCost\":%s, \"newCost\":%s}",
                        oldKind, updatedOffer.kind(), oldCost, updatedOffer.cost())
        ));

        return savedOffer;
    }

    @Transactional
    @CacheEvict(value = "offers", allEntries = true)
    public void deleteOfferById(Long idOffer) {
        if (!offerRepository.existsById(idOffer)) {
            throw new NoSuchElementException("Oferta o ID: " + idOffer + " nie istnieje");
        }
        offerRepository.deleteById(idOffer);

        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.OFFER_DELETED,
                EntityType.OFFER,
                String.valueOf(idOffer),
                null
        ));
    }

    private String getActorEmailSafely() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            return authentication.getName();
        }
        return "SYSTEM";
    }
}