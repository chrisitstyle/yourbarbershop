package pl.barbershopproject.barbershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import pl.barbershopproject.barbershop.model.GuestOrder;
import pl.barbershopproject.barbershop.repository.GuestOrderRepository;

@Service
@RequiredArgsConstructor
public class GuestOrderService {

    private final GuestOrderRepository guestOrderRepository;

    public ResponseEntity<String> addGuestOrder(GuestOrder guestOrder) {
        GuestOrder savedGuestOrder = guestOrderRepository.save(guestOrder);
        return ResponseEntity.status(HttpStatus.CREATED).body("Zamówienie bez rejestracji konta zostało dodane.");
    }
}
