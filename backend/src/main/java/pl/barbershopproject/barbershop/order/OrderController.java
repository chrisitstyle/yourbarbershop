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
import java.util.NoSuchElementException;

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
    public OrderDTO getSingleOrder(@PathVariable long idOrder) {
        return orderService.getSingleOrder(idOrder);
    }

    @PutMapping("/{idOrder}")
    public Order updateOrder(@Valid @RequestBody Order updatedOrder,
                             @PathVariable long idOrder) {
        return orderService.updateOrder(updatedOrder, idOrder);
    }

    @DeleteMapping("/{idOrder}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOrderById(@PathVariable long idOrder) {
        orderService.deleteOrderById(idOrder);
    }

    @ExceptionHandler(NoSuchElementException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public String handleNotFound(NoSuchElementException ex) {
        return ex.getMessage();
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public String handleBadRequest(IllegalArgumentException ex) {
        return ex.getMessage();
    }
}
