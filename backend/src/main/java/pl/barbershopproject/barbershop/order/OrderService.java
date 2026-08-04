package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestCollisionException;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestHasher;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.offer.BookedOffer;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.dto.OrderCreationResponseDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderUpdatedRequestDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.order.mapper.OrderDTOMapper;
import pl.barbershopproject.barbershop.payment.PaymentCheckout;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.utils.Status;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Zamówienie o ID: ";
    private static final String DOES_NOT_EXIST_MSG = " nie istnieje";
    private static final String AVAILABLE_STATUSES_MSG = "Dostępne statusy: ";

    private final OrderRepository orderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentOfferUpdater paymentOfferUpdater;
    private final OrderEvents orderEvents;
    private final OrderCreationTransaction orderCreationTransaction;
    private final PaymentCheckout paymentCheckout;
    private final IdempotencyRequestHasher idempotencyRequestHasher;
    private final IdempotencyRequestManager idempotencyRequestManager;

    @CacheEvict(value = "orders", allEntries = true)
    public OrderCreationResponseDTO addOrder(
            OrderCreationDTO orderCreationDTO,
            User user,
            String idempotencyKey
    ) {
        String requestHash = idempotencyRequestHasher.hash(
                "order-creation-v1",
                "idOffer",
                orderCreationDTO.idOffer(),
                "visitDate",
                orderCreationDTO.visitDate(),
                "paymentMethod",
                orderCreationDTO.paymentMethod().name()
        );

        OrderCreationTransactionResult transactionResult = createOrderTransaction(
                        orderCreationDTO,
                        user,
                        idempotencyKey,
                        requestHash
                );

        if (transactionResult.isInProgress()) {
            throw new IdempotencyConflictException(
                    "Żądanie z tym Idempotency-Key jest nadal przetwarzane"
            );
        }

        if (transactionResult.isCompleted()) {
            return createResponse(
                    transactionResult,
                    transactionResult.checkoutUrl()
            );
        }

        String checkoutUrl = paymentCheckout.createCheckoutIfRequired(
                transactionResult.checkoutRequest()
        );

        idempotencyRequestManager.markCompleted(
                transactionResult.idempotencyRequestId(),
                checkoutUrl
        );

        return createResponse(
                transactionResult,
                checkoutUrl
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

    @Cacheable(value = "orders", key = "'status_' + #status.toUpperCase()")
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
        Offer targetOffer = offerQuery.getRequiredOffer(request.idOffer());

        Status oldStatus = order.getStatus();
        Status targetStatus = request.status() != null
                ? request.status()
                : oldStatus;

        updateOfferIfChanged(order, targetOffer);

        appointmentReservation.updateSlotReservation(
                order.getVisitDate(),
                oldStatus,
                request.visitDate(),
                targetStatus
        );

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

    private void updateOfferIfChanged(Order order, Offer targetOffer) {
        if (!hasOfferChanged(order.getOffer(), targetOffer)) {
            return;
        }

        paymentOfferUpdater.updateAfterOfferChange(order.getPayment(), targetOffer);

        order.setOffer(targetOffer);
        order.setBookedOffer(BookedOffer.from(targetOffer));
    }

    private boolean hasOfferChanged(
            Offer currentOffer,
            Offer targetOffer
    ) {
        if (currentOffer == null) {
            return true;
        }

        return !Objects.equals(
                currentOffer.getIdOffer(),
                targetOffer.getIdOffer()
        );
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

    private OrderCreationTransactionResult createOrderTransaction(
            OrderCreationDTO orderCreationDTO,
            User user,
            String idempotencyKey,
            String requestHash
    ) {
        try {
            return orderCreationTransaction.create(
                    orderCreationDTO,
                    user,
                    idempotencyKey,
                    requestHash
            );
        } catch (IdempotencyRequestCollisionException _) {
            try {
                return orderCreationTransaction.create(
                        orderCreationDTO,
                        user,
                        idempotencyKey,
                        requestHash
                );
            } catch (IdempotencyRequestCollisionException _) {
                throw new IdempotencyConflictException(
                        "Żądanie z tym Idempotency-Key jest już przetwarzane"
                );
            }
        }
    }

    private OrderCreationResponseDTO createResponse(
            OrderCreationTransactionResult transactionResult,
            String checkoutUrl
    ) {
        return new OrderCreationResponseDTO(
                transactionResult.orderId(),
                transactionResult.checkoutRequest().paymentMethod(),
                transactionResult.checkoutRequest().paymentStatus(),
                checkoutUrl
        );
    }
}