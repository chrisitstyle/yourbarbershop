package pl.barbershopproject.barbershop.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import pl.barbershopproject.barbershop.model.Order;
import pl.barbershopproject.barbershop.service.OrderService;

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
            response = orderService.addOrder(order);
        } catch (Exception e) {
            response = ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Nie udało się dodać Zamówienia.");
        }
        return response;
    }

    @GetMapping("/get")
    public List<Order> getAllOrders(@RequestParam(required = false) String status){

        if(status != null && !status.isEmpty()){
            return orderService.getOrdersByStatus(status);
        }
        else{
            return orderService.getAllOrders();
        }
    }
    @GetMapping("/get/{id_order}")
    public Order getSingleOrder(@PathVariable long id_order){
        return orderService.getSingleOrder(id_order);
    }

    @PutMapping("/update/{id_order}")
    public ResponseEntity<Order> updateOrder(@RequestBody Order updatedOrder, @PathVariable long id_order) {
        return orderService.updateOrder(updatedOrder, id_order);
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
