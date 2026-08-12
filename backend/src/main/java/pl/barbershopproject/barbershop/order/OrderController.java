package pl.barbershopproject.barbershop.order;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.idempotency.ValidIdempotencyKey;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
@Tag(name = "Orders (Logged In)", description = "Management of orders/reservations associated with user accounts")
class OrderController {

    private final OrderService orderService;

    @Operation(summary = "Create a new order/reservation by the user")
    @ApiResponse(responseCode = "201", description = "Order successfully created")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized - User is not authenticated")
    @ApiResponse(responseCode = "404", description = "Offer not found with the specified ID")
    @ApiResponse(responseCode = "409", description = "Conflict - Selected appointment slot is already taken")
    @PostMapping
    public ResponseEntity<OrderCreationResponseDTO> addOrder(
            @Parameter(
                    description = "Unique non-blank key used to safely retry order creation. Maximum length: 255 characters.",
                    required = true,
                    example = "88fa85f2-0569-4da0-9152-68ef69478036"
            )
            @ValidIdempotencyKey
            @RequestHeader("Idempotency-Key")
            String idempotencyKey,

            @Valid @RequestBody OrderCreationDTO order
    ) {
        OrderCreationResponseDTO response = orderService.addOrder(order, idempotencyKey);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.orderId())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "Get all orders", description = "Results can be filtered using the optional 'orderStatus' parameter")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of orders")
    @ApiResponse(responseCode = "400", description = "Invalid orderStatus filter value")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to view orders")
    @GetMapping
    public List<OrderDTO> getAllOrders(
            @Parameter(description = "Optional orderStatus filter, e.g., 'NOWE', 'ZREALIZOWANE', 'ANULOWANE'")
            @RequestParam(required = false) String orderStatus
    ) {
        return orderStatus != null && !orderStatus.isEmpty()
                ? orderService.getOrdersByStatus(orderStatus)
                : orderService.getAllOrders();
    }

    @Operation(summary = "Get details of a single order by ID")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved order details")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to view this order")
    @ApiResponse(responseCode = "404", description = "Order not found with the specified ID")
    @GetMapping("/{idOrder}")
    public OrderDTO getSingleOrder(@PathVariable Long idOrder) {
        return orderService.getSingleOrder(idOrder);
    }

    @Operation(summary = "Update an existing order (e.g., by an administrator)")
    @ApiResponse(responseCode = "200", description = "Order successfully updated")
    @ApiResponse(responseCode = "400", description = "Invalid request payload")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to update this order")
    @ApiResponse(responseCode = "404", description = "Order or Offer not found")
    @ApiResponse(responseCode = "409", description = "Conflict - Appointment slot is taken or the modification is not allowed by the current order or payment state")
    @PutMapping("/{idOrder}")
    public OrderDTO updateOrder(
            @Valid @RequestBody OrderUpdatedRequestDTO updatedOrder,
            @PathVariable Long idOrder
    ) {
        return orderService.updateOrder(updatedOrder, idOrder);
    }

    @Operation(summary = "Delete an order from the database (or cancel it)")
    @ApiResponse(responseCode = "204", description = "Order successfully deleted")
    @ApiResponse(responseCode = "401", description = "Unauthorized - Missing or invalid authentication token")
    @ApiResponse(responseCode = "403", description = "Forbidden - Insufficient permissions to delete this order")
    @ApiResponse(responseCode = "404", description = "Order not found with the specified ID")
    @DeleteMapping("/{idOrder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@PathVariable Long idOrder) {
        orderService.deleteOrderById(idOrder);
    }
}