package pl.barbershopproject.barbershop.order;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;

import java.net.URI;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<String> addOrder(@Valid @RequestBody Order order) {
        Order savedOrder = orderService.addOrder(order);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedOrder.getIdOrder())
                .toUri();

        return ResponseEntity.created(location)
                .body("Wizyta została dodana. ID Wizyty: " + savedOrder.getIdOrder());
    }

    @GetMapping
    public List<OrderDTO> getAllOrders(@RequestParam(required = false) String status) {
        return status != null && !status.isEmpty()
                ? orderService.getOrdersByStatus(status)
                : orderService.getAllOrders();
    }

    @GetMapping("/{idOrder}")
    public OrderDTO getSingleOrder(@PathVariable Long idOrder) {
        return orderService.getSingleOrder(idOrder);
    }

    @PutMapping("/{idOrder}")
    public Order updateOrder(@Valid @RequestBody Order updatedOrder,
                             @PathVariable Long idOrder) {
        return orderService.updateOrder(updatedOrder, idOrder);
    }

    @DeleteMapping("/{idOrder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@PathVariable Long idOrder) {
        orderService.deleteOrderById(idOrder);
    }
}
