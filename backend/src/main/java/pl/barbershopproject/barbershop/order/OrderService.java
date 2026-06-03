package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.order.mapper.OrderCreationDTOMapper;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.util.Status;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class OrderService {

    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final AppointmentAvailabilityService appointmentAvailabilityService;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public Order addOrder(OrderCreationDTO orderCreationDTO, User user) {
        Offer offer = offerRepository.findById(orderCreationDTO.idOffer())
                .orElseThrow(() -> new NoSuchElementException("Oferta o ID: " + orderCreationDTO.idOffer() + " nie istnieje"));

        Order orderToSave = OrderCreationDTOMapper.toEntity(orderCreationDTO, user, offer);

        appointmentAvailabilityService.reserveSlot(orderToSave.getVisitDate());

        Order savedOrder = orderRepository.save(orderToSave);

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

        Status targetStatus = updatedOrder.getStatus() != null
                ? updatedOrder.getStatus()
                : existingOrder.getStatus();

        appointmentAvailabilityService.updateSlotReservation(
                existingOrder.getVisitDate(),
                existingOrder.getStatus(),
                updatedOrder.getVisitDate(),
                targetStatus
        );

        existingOrder.setUser(updatedOrder.getUser());
        existingOrder.setOffer(updatedOrder.getOffer());
        existingOrder.setOrderDate(updatedOrder.getOrderDate());
        existingOrder.setVisitDate(updatedOrder.getVisitDate());
        existingOrder.setStatus(targetStatus);

        return orderRepository.save(existingOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public void deleteOrderById(Long idOrder) {
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> new NoSuchElementException("Zamówienie o ID: " + idOrder + " nie istnieje"));

        appointmentAvailabilityService.releaseIfReserved(order.getVisitDate(), order.getStatus());

        orderRepository.delete(order);
    }

}
