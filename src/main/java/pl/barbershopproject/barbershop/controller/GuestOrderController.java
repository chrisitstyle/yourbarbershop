package pl.barbershopproject.barbershop.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import pl.barbershopproject.barbershop.model.GuestOrder;

import pl.barbershopproject.barbershop.service.GuestOrderService;

import java.util.List;
import java.util.NoSuchElementException;

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
    public List<GuestOrder> getAllGuestOrders(@RequestParam(required = false) String status){
        if(status != null && !status.isEmpty()){
            return guestOrderService.getGuestOrdersByStatus(status);
        }else{
            return guestOrderService.getAllGuestOrders();
        }

    }

    @GetMapping("/get/{idGuestOrder}")
    public GuestOrder getGuestOrder(@PathVariable long idGuestOrder){
        return guestOrderService.getGuestOrder(idGuestOrder);
    }

    @PutMapping("/update/{idGuestOrder}")
    public ResponseEntity<GuestOrder> updateGuestOrder(@RequestBody GuestOrder updatedGuestOrder, @PathVariable long idGuestOrder) {
        return guestOrderService.updateGuestOrder(updatedGuestOrder, idGuestOrder);
    }

    @DeleteMapping("/delete/{idGuestOrder}")
    public ResponseEntity<String> deleteGuestOrderById(@PathVariable long idGuestOrder) {
        try {
            guestOrderService.deleteGuestOrderById(idGuestOrder);
            return new ResponseEntity<>("Zamówienie o ID " + idGuestOrder + " zostało usunięte.", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
