package pl.barbershopproject.barbershop.service;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.model.Order;
import pl.barbershopproject.barbershop.repository.OrderRepository;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class OrderService {

    private final OrderRepository orderRepository;


    public Order addOrder(Order order) {
       return orderRepository.save(order);

    }

    public List<Order> getAllOrders(){
        return orderRepository.findAll();
    }
    public Order getSingleOrder(long id_order){
        return orderRepository.findById(id_order).orElseThrow(NoSuchElementException::new);
    }

    public List<Order> getOrdersByStatus(String status){

        return orderRepository.findOrdersByStatus(status);
    }

    @Transactional
    public Order updateOrder(Order updatedOrder, Long id_order) {
        return orderRepository.findById(id_order)
                .map(order -> {
                    order.setUser(updatedOrder.getUser());
                    order.setOffer(updatedOrder.getOffer());
                    order.setOrderDate(updatedOrder.getOrderDate());
                    order.setVisitDate(updatedOrder.getVisitDate());
                    order.setStatus(updatedOrder.getStatus());
                    return orderRepository.save(order);
                })
                .orElseThrow(NoSuchElementException::new);
    }

    @Transactional
    public void deleteOrderById(long id_order) {
        Optional<Order> orderExists = orderRepository.findById(id_order);

        if (orderExists.isPresent()) {
            orderRepository.deleteById(id_order);
        } else {
            throw new NoSuchElementException("Zamówienie o podanym ID nie istnieje");
        }
    }

}
