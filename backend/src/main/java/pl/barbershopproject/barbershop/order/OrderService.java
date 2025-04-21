package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.util.Status;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor

class OrderService {

    private final OrderRepository orderRepository;


    public Order addOrder(Order order) {
        return orderRepository.save(order);

    }

    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
    }

    public OrderDTO getSingleOrder(long idOrder) {
        return orderRepository.findById(idOrder)
                .map(OrderDTOMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Zamówienie o ID: " + idOrder + " nie istnieje"));
    }

    public List<OrderDTO> getOrdersByStatus(String status) {
        try {
            Status enumStatus = Status.valueOf(status.toUpperCase());
            return orderRepository.findOrdersByStatus(enumStatus).stream()
                    .map(OrderDTOMapper::toDTO)
                    .toList();
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Dostępne statusy: " + List.of(Status.values()));
        }
    }

    @Transactional
    public Order updateOrder(Order updatedOrder, long idOrder) {
        Order existingOrder = orderRepository.findById(idOrder)
                .orElseThrow(() -> new NoSuchElementException("Zamówienie o ID: " + idOrder));

        existingOrder.setUser(updatedOrder.getUser());
        existingOrder.setOffer(updatedOrder.getOffer());
        existingOrder.setOrderDate(updatedOrder.getOrderDate());
        existingOrder.setVisitDate(updatedOrder.getVisitDate());
        existingOrder.setStatus(updatedOrder.getStatus());

        return orderRepository.save(existingOrder);
    }

    @Transactional
    public void deleteOrderById(long idOrder) {
        if (!orderRepository.existsById(idOrder)) {
            throw new NoSuchElementException("Zamówienie o ID: " + idOrder + " nie istnieje");
        }
        orderRepository.deleteById(idOrder);
    }

}
