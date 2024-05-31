package pl.barbershopproject.barbershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.model.GuestOrder;
import pl.barbershopproject.barbershop.repository.GuestOrderRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private final GuestOrderRepository guestOrderRepository;

    public ResponseEntity<String> addGuestOrder(GuestOrder guestOrder) {
        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body("Zamówienie bez rejestracji konta zostało dodane.");
    }

    public List<GuestOrder> getAllGuestOrders (){
        return guestOrderRepository.findAll();
    }
    public GuestOrder getGuestOrder(long idGuestOrder){
        return guestOrderRepository.findById(idGuestOrder).orElse(null);
    }

    public List<GuestOrder> getGuestOrdersByStatus(String status){

        return guestOrderRepository.findGuestOrdersByStatus(status);
    }
    @Transactional
    public ResponseEntity<GuestOrder> updateGuestOrder(GuestOrder updatedGuestOrder, Long idGuestOrder) {
        return guestOrderRepository.findById(idGuestOrder)
                .map(guestOrder -> {
                    guestOrder.setFirstname(updatedGuestOrder.getFirstname());
                    guestOrder.setLastname(updatedGuestOrder.getLastname());
                    guestOrder.setPhonenumber(updatedGuestOrder.getPhonenumber());
                    guestOrder.setEmail(updatedGuestOrder.getEmail());
                    guestOrder.setOffer(updatedGuestOrder.getOffer());
                    guestOrder.setOrderDate(updatedGuestOrder.getOrderDate());
                    guestOrder.setVisitDate(updatedGuestOrder.getVisitDate());
                    guestOrder.setStatus(updatedGuestOrder.getStatus());
                    GuestOrder updatedGuestOrderEntity = guestOrderRepository.save(guestOrder);
                    return ResponseEntity.ok(updatedGuestOrderEntity);
                })
                .orElse(ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    @Transactional
    public void deleteGuestOrderById(long idGuestOrder) {
        Optional<GuestOrder> guestOrderExists = guestOrderRepository.findById(idGuestOrder);

        if (guestOrderExists.isPresent()) {
            guestOrderRepository.deleteById(idGuestOrder);
        } else {
            throw new NoSuchElementException("Zamówienie o podanym ID nie istnieje");
        }
    }

}
