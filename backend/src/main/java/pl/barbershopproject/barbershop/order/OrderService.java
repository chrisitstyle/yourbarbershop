package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor

public class OrderService {

    private final OrderRepository orderRepository;


    public void addOrder(Order order) {
        orderRepository.save(order);

    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Order getSingleOrder(long idOrder) {
        return orderRepository.findById(idOrder).orElseThrow(NoSuchElementException::new);
    }

    public List<Order> getOrdersByStatus(String status) {

        return orderRepository.findOrdersByStatus(status);
    }

    @Transactional
    public Order updateOrder(Order updatedOrder, Long idOrder) {
        return orderRepository.findById(idOrder)
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
    public void deleteOrderById(long idOrder) {
        Optional<Order> orderExists = orderRepository.findById(idOrder);

        if (orderExists.isPresent()) {
            orderRepository.deleteById(idOrder);
        } else {
            throw new NoSuchElementException("Zamówienie o podanym ID nie istnieje");
        }
    }

}
