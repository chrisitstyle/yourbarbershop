package pl.barbershopproject.barbershop.guestorder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.util.Status;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guestorders")
class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping
    public ResponseEntity<GuestOrder> addGuestOrder(@Valid @RequestBody GuestOrder guestOrder) {
        GuestOrder savedGuestOrder = guestOrderService.addGuestOrder(guestOrder);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedGuestOrder.getIdGuestOrder())
                .toUri();

        return ResponseEntity.created(location).body(savedGuestOrder);
    }

    @GetMapping
    public List<GuestOrder> getAllGuestOrders(@RequestParam(required = false) Status status) {
        return status != null
                ? guestOrderService.getGuestOrdersByStatus(status)
                : guestOrderService.getAllGuestOrders();
    }

    @GetMapping("/{idGuestOrder}")
    public GuestOrder getGuestOrder(@PathVariable Long idGuestOrder) {
        return guestOrderService.getGuestOrder(idGuestOrder);
    }

    @PutMapping("/{idGuestOrder}")
    public GuestOrder updateGuestOrder(@Valid @RequestBody GuestOrder updatedGuestOrder,
                                       @PathVariable Long idGuestOrder) {
        return guestOrderService.updateGuestOrder(updatedGuestOrder, idGuestOrder);
    }

    @DeleteMapping("/{idGuestOrder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuestOrderById(@PathVariable Long idGuestOrder) {
        guestOrderService.deleteGuestOrderById(idGuestOrder);
    }
}
