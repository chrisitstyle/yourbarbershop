package pl.barbershopproject.barbershop.guestorder;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationResponseDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderUpdateRequestDTO;
import pl.barbershopproject.barbershop.utils.Status;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/guestorders")
@Tag(name = "Guest Orders", description = "Management of orders and reservations created by unauthenticated guest users")
public class GuestOrderController {

    private final GuestOrderService guestOrderService;

    @Operation(summary = "Create a guest order/reservation", description = "Allows unauthenticated users to create a new appointment reservation. Publicly accessible.")
    @ApiResponse(responseCode = "201", description = "Guest order successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "404", description = "Offer not found with the specified ID")
    @ApiResponse(responseCode = "409", description = "Conflict - Selected appointment slot is already taken")
    @SecurityRequirements()
    @PostMapping
    public ResponseEntity<GuestOrderCreationResponseDTO> addGuestOrder(
            @Parameter(description = "Unique key used to safely retry guest-order creation",
                    required = true,
                    example = "88fa85f2-0569-4da0-9152-68ef69478036")
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @Valid
            @RequestBody
            GuestOrderCreationDTO guestOrderCreationDTO) {
        GuestOrderCreationResponseDTO addedGuestOrder = guestOrderService.addGuestOrder(
                        guestOrderCreationDTO,
                        idempotencyKey
                );

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(addedGuestOrder.guestOrderId())
                .toUri();

        return ResponseEntity.created(location).body(addedGuestOrder);
    }

    @Operation(summary = "Get all guest orders", description = "Retrieves a list of all guest orders with optional status filtering. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of guest orders")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @GetMapping
    public List<GuestOrderDTO> getAllGuestOrders(
            @Parameter(description = "Optional status filter, e.g., 'NOWE', 'ZAKONCZONE', 'ANULOWANE'")
            @RequestParam(required = false) Status status
    ) {
        return status != null
                ? guestOrderService.getGuestOrdersByStatus(status)
                : guestOrderService.getAllGuestOrders();
    }

    @Operation(summary = "Get guest order details by ID", description = "Retrieves details of a specific guest order. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved guest order details")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @ApiResponse(responseCode = "404", description = "Guest order not found with the specified ID")
    @GetMapping("/{idGuestOrder}")
    public GuestOrderDTO getGuestOrder(@PathVariable Long idGuestOrder) {
        return guestOrderService.getGuestOrder(idGuestOrder);
    }

    @Operation(summary = "Update an existing guest order", description = "Updates guest order details or visit status. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "200", description = "Guest order successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @ApiResponse(responseCode = "404", description = "Guest order or Offer not found")
    @ApiResponse(responseCode = "409", description = "Conflict - Target appointment slot is already taken")
    @PutMapping("/{idGuestOrder}")
    public GuestOrderDTO updateGuestOrder(
            @Valid @RequestBody GuestOrderUpdateRequestDTO updatedGuestOrder,
            @PathVariable Long idGuestOrder
    ) {
        return guestOrderService.updateGuestOrder(updatedGuestOrder, idGuestOrder);
    }

    @Operation(summary = "Delete a guest order", description = "Deletes a guest order from the database and releases any reserved slot. Restricted to ADMIN users.")
    @ApiResponse(responseCode = "204", description = "Guest order successfully deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Access restricted to administrators")
    @ApiResponse(responseCode = "404", description = "Guest order not found with the specified ID")
    @DeleteMapping("/{idGuestOrder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteGuestOrderById(@PathVariable Long idGuestOrder) {
        guestOrderService.deleteGuestOrderById(idGuestOrder);
    }
}