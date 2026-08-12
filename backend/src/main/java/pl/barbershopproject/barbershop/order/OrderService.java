package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.exception.MissingPaymentException;
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
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentCheckout;
import pl.barbershopproject.barbershop.payment.PaymentOfferUpdater;
import pl.barbershopproject.barbershop.security.AuthenticatedUser;
import pl.barbershopproject.barbershop.security.CurrentUserProvider;
import pl.barbershopproject.barbershop.user.User;
import pl.barbershopproject.barbershop.user.UserRepository;
import pl.barbershopproject.barbershop.utils.OrderModificationPolicy;
import pl.barbershopproject.barbershop.utils.OrderStatus;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;

@Service
@RequiredArgsConstructor
class OrderService {

    private static final String ORDER_NOT_FOUND_MSG = "Zamówienie o ID: ";

    private static final String DOES_NOT_EXIST_MSG = " nie istnieje";

    private static final String AVAILABLE_ORDER_STATUSES_MSG = "Dostępne statusy zamówienia: ";

    private final CurrentUserProvider currentUserProvider;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentOfferUpdater paymentOfferUpdater;
    private final OrderModificationPolicy orderModificationPolicy;
    private final OrderEvents orderEvents;
    private final OrderCreationTransaction orderCreationTransaction;
    private final PaymentCheckout paymentCheckout;
    private final IdempotencyRequestHasher idempotencyRequestHasher;
    private final IdempotencyRequestManager idempotencyRequestManager;

    @CacheEvict(value = "orders", allEntries = true)
    public OrderCreationResponseDTO addOrder(
            OrderCreationDTO orderCreationDTO,
            String idempotencyKey
    ) {
        AuthenticatedUser authenticatedUser = currentUserProvider.getCurrentUser();

        User user = userRepository.findById(authenticatedUser.userId())
                .orElseThrow(() ->
                        new NoSuchElementException(
                                "Użytkownik o podanym ID nie istnieje"));

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
                        transactionResult.checkoutRequest());

        idempotencyRequestManager.markCompleted(
                transactionResult.idempotencyRequestId(),
                checkoutUrl);

        return createResponse(
                transactionResult,
                checkoutUrl);
    }

    @Cacheable(value = "orders", key = "'all'")
    public List<OrderDTO> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(OrderDTOMapper::toDTO)
                .toList();
    }

    @Cacheable(value = "orders", key = "#idOrder")
    public OrderDTO getSingleOrder(Long idOrder) {
        return OrderDTOMapper.toDTO(
                getRequiredOrder(idOrder)
        );
    }

    @Cacheable(
            value = "orders",
            key = "'status_' + #orderStatus.toUpperCase()"
    )
    public List<OrderDTO> getOrdersByStatus(String orderStatus) {
        OrderStatus parsedOrderStatus =
                parseOrderStatus(orderStatus);

        return orderRepository
                .findOrdersByStatus(parsedOrderStatus)
                .stream()
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
        Payment payment = getRequiredPayment(order);

        OrderStatus currentOrderStatus = order.getOrderStatus();

        OrderStatus targetOrderStatus = request.orderStatus() != null
                        ? request.orderStatus()
                        : currentOrderStatus;

        orderModificationPolicy.validateUpdate(
                currentOrderStatus,
                targetOrderStatus,
                payment
        );

        Offer targetOffer = offerQuery.getRequiredOffer(
                request.idOffer()
        );

        updateOfferIfChanged(
                order,
                targetOffer,
                payment
        );

        appointmentReservation.updateSlotReservation(
                order.getVisitDate(),
                currentOrderStatus,
                request.visitDate(),
                targetOrderStatus
        );

        order.setVisitDate(request.visitDate());
        order.setOrderStatus(targetOrderStatus);

        Order savedOrder = orderRepository.save(order);

        orderEvents.updated(
                savedOrder,
                currentOrderStatus
        );

        return OrderDTOMapper.toDTO(savedOrder);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public void deleteOrderById(Long idOrder) {
        Order order = getRequiredOrder(idOrder);

        appointmentReservation.releaseIfReserved(
                order.getVisitDate(),
                order.getOrderStatus()
        );

        orderRepository.delete(order);
        orderEvents.deleted(idOrder);
    }

    private void updateOfferIfChanged(
            Order order,
            Offer targetOffer,
            Payment payment
    ) {
        if (!hasOfferChanged(
                order.getOffer(),
                targetOffer
        )) {
            return;
        }

        paymentOfferUpdater.updateAfterOfferChange(
                payment,
                targetOffer
        );

        order.setOffer(targetOffer);
        order.setBookedOffer(
                BookedOffer.from(targetOffer)
        );
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

    private Payment getRequiredPayment(Order order) {
        Payment payment = order.getPayment();

        if (payment == null) {
            throw new MissingPaymentException(
                    "Zamówienie",
                    order.getIdOrder()
            );
        }

        return payment;
    }

    private OrderStatus parseOrderStatus(String orderStatus) {
        try {
            return OrderStatus.valueOf(
                    orderStatus.toUpperCase()
            );
        } catch (IllegalArgumentException _) {
            throw new IllegalArgumentException(
                    AVAILABLE_ORDER_STATUSES_MSG
                            + List.of(OrderStatus.values())
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