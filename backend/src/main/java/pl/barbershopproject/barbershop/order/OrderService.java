package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentAvailabilityService;
import pl.barbershopproject.barbershop.audit.enums.ActionType;
import pl.barbershopproject.barbershop.audit.enums.EntityType;
import pl.barbershopproject.barbershop.audit.event.AuditEvent;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferRepository;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderCreatedEvent;
import pl.barbershopproject.barbershop.order.mapper.OrderCreationDTOMapper;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.payment.*;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static pl.barbershopproject.barbershop.utils.SecurityUtils.getActorEmailSafely;

@Service
@RequiredArgsConstructor
class OrderService {

    private static final String OFFER_NOT_FOUND_MSG = "Oferta o ID: ";
    private static final String ORDER_NOT_FOUND_MSG = "Zamówienie o ID: ";
    private static final String DOES_NOT_EXIST_MSG = " nie istnieje";
    private static final String AVAILABLE_STATUSES_MSG = "Dostępne statusy: ";

    private final OrderRepository orderRepository;
    private final OfferRepository offerRepository;
    private final AppointmentAvailabilityService appointmentAvailabilityService;
    private final StripeCheckoutService stripeCheckoutService;
    private final PaymentRepository paymentRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final Clock clock;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderCreationResponseDTO addOrder(OrderCreationDTO orderCreationDTO, User user) {
        Offer offer = offerRepository.findById(orderCreationDTO.idOffer())
                .orElseThrow(() -> offerNotFoundException(orderCreationDTO.idOffer()));

        Order orderToSave = OrderCreationDTOMapper.toEntity(orderCreationDTO, user, offer, clock);
        appointmentAvailabilityService.reserveSlot(orderToSave.getVisitDate());

        Order savedOrder = orderRepository.save(orderToSave);

        Payment paymentToSave = Payment.builder()
                .order(savedOrder)
                .paymentMethod(orderCreationDTO.paymentMethod())
                .paymentStatus(PaymentStatus.OCZEKUJE_NA_PLATNOSC)
                .amount(offer.getCost())
                .currency("PLN")
                .createdAt(LocalDateTime.now(clock))
                .build();

        Payment savedPayment = paymentRepository.save(paymentToSave);

        eventPublisher.publishEvent(new AuditEvent(
                user.getEmail(),
                ActionType.ORDER_CREATED,
                EntityType.ORDER,
                String.valueOf(savedOrder.getIdOrder()),
                String.format("{\"offerKind\":\"%s\", \"cost\":%s, \"visitDate\":\"%s\"}",
                        offer.getKind(), offer.getCost(), savedOrder.getVisitDate())
        ));

        if (savedPayment.getPaymentMethod() == PaymentMethod.KARTA_ONLINE) {
            StripeCheckoutSessionResponse checkoutSession = stripeCheckoutService.createCheckoutSession(
                    savedPayment,
                    offer
            );

            savedPayment.setStripeCheckoutSessionId(checkoutSession.sessionId());
            paymentRepository.save(savedPayment);

            return new OrderCreationResponseDTO(
                    savedOrder.getIdOrder(),
                    savedPayment.getPaymentMethod(),
                    savedPayment.getPaymentStatus(),
                    checkoutSession.checkoutUrl()
            );
        }

        publishOrderCreatedEvent(savedOrder, savedPayment);

        return new OrderCreationResponseDTO(
                savedOrder.getIdOrder(),
                savedPayment.getPaymentMethod(),
                savedPayment.getPaymentStatus(),
                null
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
        return orderRepository.findById(idOrder)
                .map(OrderDTOMapper::toDTO)
                .orElseThrow(() -> orderNotFoundException(idOrder));
    }

    @Cacheable(value = "orders", key = "'status_' + #status.toUpperCase()")
    public List<OrderDTO> getOrdersByStatus(String status) {
        try {
            Status enumStatus = Status.valueOf(status.toUpperCase());
            return orderRepository.findOrdersByStatus(enumStatus).stream()
                    .map(OrderDTOMapper::toDTO)
                    .toList();
        } catch (IllegalArgumentException _) {
            throw new IllegalArgumentException(AVAILABLE_STATUSES_MSG + List.of(Status.values()));
        }
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderDTO updateOrder(OrderUpdatedRequestDTO updatedOrder, Long idOrder) {
        Order existingOrder = orderRepository.findById(idOrder)
                .orElseThrow(() -> orderNotFoundException(idOrder));

        Offer offer = offerRepository.findById(updatedOrder.idOffer())
                .orElseThrow(() -> offerNotFoundException(updatedOrder.idOffer()));

        Status targetStatus = updatedOrder.status() != null
                ? updatedOrder.status()
                : existingOrder.getStatus();

        appointmentAvailabilityService.updateSlotReservation(
                existingOrder.getVisitDate(),
                existingOrder.getStatus(),
                updatedOrder.visitDate(),
                targetStatus
        );

        Status oldStatus = existingOrder.getStatus();

        existingOrder.setOffer(offer);
        existingOrder.setVisitDate(updatedOrder.visitDate());
        existingOrder.setStatus(targetStatus);

        Order savedOrder = orderRepository.save(existingOrder);

        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.ORDER_UPDATED,
                EntityType.ORDER,
                String.valueOf(idOrder),
                String.format("{\"oldStatus\":\"%s\", \"newStatus\":\"%s\", \"visitDate\":\"%s\"}",
                        oldStatus, targetStatus, updatedOrder.visitDate())
        ));

        return OrderDTOMapper.toDTO(savedOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public void deleteOrderById(Long idOrder) {
        Order order = orderRepository.findById(idOrder)
                .orElseThrow(() -> orderNotFoundException(idOrder));

        appointmentAvailabilityService.releaseIfReserved(order.getVisitDate(), order.getStatus());

        orderRepository.delete(order);

        eventPublisher.publishEvent(new AuditEvent(
                getActorEmailSafely(),
                ActionType.ORDER_DELETED,
                EntityType.ORDER,
                String.valueOf(idOrder),
                null
        ));
    }

    private NoSuchElementException offerNotFoundException(Long idOffer) {
        return new NoSuchElementException(OFFER_NOT_FOUND_MSG + idOffer + DOES_NOT_EXIST_MSG);
    }

    private NoSuchElementException orderNotFoundException(Long idOrder) {
        return new NoSuchElementException(ORDER_NOT_FOUND_MSG + idOrder + DOES_NOT_EXIST_MSG);
    }

    private void publishOrderCreatedEvent(Order order, Payment payment) {
        eventPublisher.publishEvent(new OrderCreatedEvent(
                order.getUser().getEmail(),
                order.getUser().getFirstname(),
                order.getVisitDate(),
                order.getOffer().getKind(),
                order.getOffer().getCost(),
                payment.getPaymentMethod(),
                payment.getPaymentStatus()
        ));
    }


}