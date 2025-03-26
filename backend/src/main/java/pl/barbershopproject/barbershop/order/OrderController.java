package pl.barbershopproject.barbershop.order;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/add")
    public ResponseEntity<String> addOrder(@RequestBody Order order) {
        ResponseEntity<String> response;
        try {
            orderService.addOrder(order);
            response = ResponseEntity.status(HttpStatus.CREATED).body("Wizyta została złożona.");
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie udało się zapisać wizyty.");
        }
        return response;
    }

    @GetMapping("/get")
    public ResponseEntity<List<Order>> getAllOrders(@RequestParam(required = false) String status) {
        List<Order> orders;

        if (status != null && !status.isEmpty()) {
            orders = orderService.getOrdersByStatus(status);
            if (orders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        } else {
            orders = orderService.getAllOrders();

            if (orders.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
        }

        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @GetMapping("/get/{idOrder}")
    public ResponseEntity<Order> getSingleOrder(@PathVariable long idOrder) {
        try {
            Order order = orderService.getSingleOrder(idOrder);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update/{idOrder}")
    public ResponseEntity<Order> updateOrder(@RequestBody Order updatedOrder, @PathVariable long idOrder) {
        try {
            Order orderResponse = orderService.updateOrder(updatedOrder, idOrder);
            return ResponseEntity.ok(orderResponse);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/delete/{idOrder}")
    public ResponseEntity<String> deleteOrderById(@PathVariable long idOrder) {
        try {
            orderService.deleteOrderById(idOrder);
            return new ResponseEntity<>("Zamówienie o ID " + idOrder + " zostało usunięte.", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
