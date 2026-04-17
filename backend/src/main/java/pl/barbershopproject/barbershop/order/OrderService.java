package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.util.Status;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor

class OrderService {

    private final OrderRepository orderRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public Order addOrder(Order order) {
        Order savedOrder = orderRepository.save(order);

        eventPublisher.publishEvent(new OrderCreatedEvent(
                savedOrder.getUser().getEmail(),
                savedOrder.getUser().getFirstname(),
                savedOrder.getVisitDate(),
                savedOrder.getOffer().getKind(),
                savedOrder.getOffer().getCost()
        ));

        return savedOrder;

    }

    @Cacheable(value = "orders", key = "'all'")
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "orders", key = "#idOrder")
    public OrderDTO getSingleOrder(Long idOrder) {
        return orderRepository.findById(idOrder)
                .map(OrderDTOMapper::toDTO)
                .orElseThrow(() -> new NoSuchElementException("Zamówienie o ID: " + idOrder + " nie istnieje"));
    }

    @Cacheable(value = "orders", key = "'status_' + #status.toUpperCase()")
    public List<OrderDTO> getOrdersByStatus(String status) {
        try {
            Status enumStatus = Status.valueOf(status.toUpperCase());
            return orderRepository.findOrdersByStatus(enumStatus).stream()
                    .map(OrderDTOMapper::toDTO)
                    .toList();
        } catch (IllegalArgumentException _) {
            throw new IllegalArgumentException("Dostępne statusy: " + List.of(Status.values()));
        }
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public Order updateOrder(Order updatedOrder, Long idOrder) {
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
    @CacheEvict(value = "orders", allEntries = true)
    public void deleteOrderById(Long idOrder) {
        if (!orderRepository.existsById(idOrder)) {
            throw new NoSuchElementException("Zamówienie o ID: " + idOrder + " nie istnieje");
        }
        orderRepository.deleteById(idOrder);
    }

}
