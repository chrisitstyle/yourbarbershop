package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.mapper.GuestOrderCreationDTOMapper;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.util.Status;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class GuestOrderService {

    private final GuestOrderRepository guestOrderRepository;
    private final OfferRepository offerRepository;
    private final ApplicationEventPublisher eventPublisher;
    @Transactional
    public GuestOrder addGuestOrder(GuestOrderCreationDTO guestOrderCreationDTO) {
        Offer offer = offerRepository.findById(guestOrderCreationDTO.idOffer())
                .orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + guestOrderCreationDTO.idOffer() + " nie istnieje"));

        GuestOrder guestOrderToSave = GuestOrderCreationDTOMapper.toEntity(guestOrderCreationDTO, offer);

        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrderToSave);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                savedGuestOrder.getEmail(),
                savedGuestOrder.getFirstname(),
                savedGuestOrder.getVisitDate(),
                savedGuestOrder.getOffer().getKind(),
                savedGuestOrder.getOffer().getCost()
        ));

        return savedGuestOrder;
    }

    public List<GuestOrder> getAllGuestOrders() {
        return guestOrderRepository.findAll();
    }

    public GuestOrder getGuestOrder(Long idGuestOrder) {
        return guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder));
    }

    public List<GuestOrder> getGuestOrdersByStatus(Status status) {

        return guestOrderRepository.findGuestOrdersByStatus(status);
    }

    @Transactional
    public GuestOrder updateGuestOrder(GuestOrder updatedGuestOrder, Long idGuestOrder) {
        GuestOrder existingOrder = guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder));

        existingOrder.setFirstname(updatedGuestOrder.getFirstname());
        existingOrder.setLastname(updatedGuestOrder.getLastname());
        existingOrder.setPhonenumber(updatedGuestOrder.getPhonenumber());
        existingOrder.setEmail(updatedGuestOrder.getEmail());
        existingOrder.setOffer(updatedGuestOrder.getOffer());
        existingOrder.setVisitDate(updatedGuestOrder.getVisitDate());
        existingOrder.setStatus(updatedGuestOrder.getStatus());

        return guestOrderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteGuestOrderById(Long idGuestOrder) {

        if (!guestOrderRepository.existsById(idGuestOrder)) {
            throw new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder);
        }
        guestOrderRepository.deleteById(idGuestOrder);
    }


}
