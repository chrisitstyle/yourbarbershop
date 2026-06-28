package pl.barbershopproject.barbershop.guestorder;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.util.Status;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guestorders")
class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @PostMapping
    public ResponseEntity<GuestOrderCreationResponseDTO> addGuestOrder(
            @Valid @RequestBody GuestOrderCreationDTO guestOrderCreationDTO
    ) {
        GuestOrderCreationResponseDTO response = guestOrderService.addGuestOrder(guestOrderCreationDTO);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.guestOrderId())
                .toUri();

        return ResponseEntity.created(location).body(response);
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
    public GuestOrder updateGuestOrder(
            @Valid @RequestBody GuestOrder updatedGuestOrder,
            @PathVariable Long idGuestOrder
    ) {
        return guestOrderService.updateGuestOrder(updatedGuestOrder, idGuestOrder);
    }

    @DeleteMapping("/{idGuestOrder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuestOrderById(@PathVariable Long idGuestOrder) {
        guestOrderService.deleteGuestOrderById(idGuestOrder);
    }
}