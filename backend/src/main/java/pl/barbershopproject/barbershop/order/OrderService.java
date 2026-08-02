package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.order.mapper.OrderCreationDTOMapper;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.payment.PaymentCreationResult;
import pl.barbershopproject.barbershop.payment.PaymentCreator;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.Clock;
import java.util.List;
import java.util.NoSuchElementException;

@Service
@RequiredArgsConstructor
class OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Zamówienie o ID: ";
    private static final String DOES_NOT_EXIST_MSG = " nie istnieje";
    private static final String AVAILABLE_STATUSES_MSG = "Dostępne statusy: ";

    private final OrderRepository orderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentCreator paymentCreator;
    private final OrderEvents orderEvents;
    private final Clock clock;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderCreationResponseDTO addOrder(
            OrderCreationDTO request,
            User user
    ) {
        Offer offer = offerQuery.getRequiredOffer(request.idOffer());

        Order order = OrderCreationDTOMapper.toEntity(
                request,
                user,
                offer,
                clock
        );

        appointmentReservation.reserveSlot(order.getVisitDate());

        Order savedOrder = orderRepository.save(order);

        PaymentCreationResult paymentResult = paymentCreator.createForOrder(
                savedOrder,
                offer,
                request.paymentMethod()
        );

        orderEvents.created(savedOrder, paymentResult.payment());

        return new OrderCreationResponseDTO(
                savedOrder.getIdOrder(),
                paymentResult.payment().getPaymentMethod(),
                paymentResult.payment().getPaymentStatus(),
                paymentResult.checkoutUrl()
        );
    }

    @Cacheable(value = "orders", key = "'all'")
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "orders", key = "#idOrder")
    public OrderDTO getSingleOrder(Long idOrder) {
        return OrderDTOMapper.toDTO(getRequiredOrder(idOrder));
    }

    @Cacheable(
            value = "orders",
            key = "'status_' + #status.toUpperCase()"
    )
    public List<OrderDTO> getOrdersByStatus(String status) {
        Status parsedStatus = parseStatus(status);

        return orderRepository.findOrdersByStatus(parsedStatus).stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDTO updateOrder(
            OrderUpdatedRequestDTO request,
            Long idOrder
    ) {
        Order order = getRequiredOrder(idOrder);
        Offer offer = offerQuery.getRequiredOffer(request.idOffer());

        Status oldStatus = order.getStatus();
        Status targetStatus = request.status() != null
                ? request.status()
                : oldStatus;

        appointmentReservation.updateSlotReservation(
                order.getVisitDate(),
                oldStatus,
                request.visitDate(),
                targetStatus
        );

        order.setOffer(offer);
        order.setVisitDate(request.visitDate());
        order.setStatus(targetStatus);

        Order savedOrder = orderRepository.save(order);

        orderEvents.updated(savedOrder, oldStatus);

        return OrderDTOMapper.toDTO(savedOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public void deleteOrderById(Long idOrder) {
        Order order = getRequiredOrder(idOrder);

        appointmentReservation.releaseIfReserved(
                order.getVisitDate(),
                order.getStatus()
        );

        orderRepository.delete(order);
        orderEvents.deleted(idOrder);
    }

    private Order getRequiredOrder(Long idOrder) {
        return orderRepository.findById(idOrder)
                .orElseThrow(() -> new NoSuchElementException(
                        ORDER_NOT_FOUND_MSG
                                + idOrder
                                + DOES_NOT_EXIST_MSG
                ));
    }

    private Status parseStatus(String status) {
        try {
            return Status.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException _) {
            throw new IllegalArgumentException(
                    AVAILABLE_STATUSES_MSG + List.of(Status.values())
            );
        }
    }
}