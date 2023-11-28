package pl.barbershopproject.barbershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import pl.barbershopproject.barbershop.model.GuestOrder;
import pl.barbershopproject.barbershop.service.GuestOrderService;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guestorders")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping("/add")
    public ResponseEntity<String> addGuestOrder(@RequestBody GuestOrder guestOrder) {
        ResponseEntity<String> response;
        try {
            response = guestOrderService.addGuestOrder(guestOrder);
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie udało się dodać Zamówienia.");
        }
        return response;
    }

    @GetMapping("/get")
    public List<GuestOrder> getAllGuestOrders(){
        return guestOrderService.getAllGuestOrders();
    }
    @GetMapping("/get/{idGuestOrder}")
    public GuestOrder getGuestOrder(@PathVariable long idGuestOrder){
        return guestOrderService.getGuestOrder(idGuestOrder);
    }
}
