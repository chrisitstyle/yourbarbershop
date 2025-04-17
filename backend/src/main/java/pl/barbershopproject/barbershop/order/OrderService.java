package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.util.Status;

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

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
    }

    public OrderDTO getSingleOrder(long idOrder) {
        Order order = orderRepository.findById(idOrder).orElseThrow(NoSuchElementException::new);
        return OrderDTOMapper.toDTO(order);
    }

    public List<OrderDTO> getOrdersByStatus(String status) {

        return orderRepository.findOrdersByStatus(Status.valueOf(status.toUpperCase()))
                .stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
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
