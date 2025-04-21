package pl.barbershopproject.barbershop.guestorder;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.util.Status;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class GuestOrderService {

    private final GuestOrderRepository guestOrderRepository;

    public GuestOrder addGuestOrder(GuestOrder guestOrder) {
        return guestOrderRepository.save(guestOrder);
    }

    public List<GuestOrder> getAllGuestOrders (){
        return guestOrderRepository.findAll();
    }
    public GuestOrder getGuestOrder(long idGuestOrder) {
        return guestOrderRepository.findById(idGuestOrder)
                .orElseThrow(() -> new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder));
    }

    public List<GuestOrder> getGuestOrdersByStatus(Status status){

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
    public void deleteGuestOrderById(long idGuestOrder) {

        if (!guestOrderRepository.existsById(idGuestOrder)) {
            throw new NoSuchElementException("Nie znaleziono zamówienia o ID: " + idGuestOrder);
        }
        guestOrderRepository.deleteById(idGuestOrder);
    }


}
