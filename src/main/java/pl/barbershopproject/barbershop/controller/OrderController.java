package pl.barbershopproject.barbershop.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.barbershopproject.barbershop.model.Order;
import pl.barbershopproject.barbershop.service.OrderService;

import java.util.ArrayList;
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

    @GetMapping("/get/{id_order}")
    public ResponseEntity<Order> getSingleOrder(@PathVariable long id_order) {
        try {
            Order order = orderService.getSingleOrder(id_order);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PutMapping("/update/{id_order}")
    public ResponseEntity<?> updateOrder(@RequestBody Order updatedOrder, @PathVariable long id_order) {
        try {
            Order order = orderService.updateOrder(updatedOrder, id_order);
            return ResponseEntity.ok(order);
        } catch (NoSuchElementException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nie znaleziono wizyty o id: " + id_order);
        }
    }

    @DeleteMapping("/delete/{id_order}")
    public ResponseEntity<String> deleteOrderById(@PathVariable long id_order) {
        try {
            orderService.deleteOrderById(id_order);
            return new ResponseEntity<>("Zamówienie o ID " + id_order + " zostało usunięte.", HttpStatus.OK);
        } catch (NoSuchElementException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.NOT_FOUND);
        }
    }
}
