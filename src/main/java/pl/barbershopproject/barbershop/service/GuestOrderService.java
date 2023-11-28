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
        return guestOrderRepository.findById(idGuestOrder).orElseThrow(NoSuchElementException::new);
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
