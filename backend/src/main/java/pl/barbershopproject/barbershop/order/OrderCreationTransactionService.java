package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestManager;
import pl.barbershopproject.barbershop.idempotency.IdempotencyRequestResult;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.offer.OfferQuery;
import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.order.event.OrderEvents;
import pl.barbershopproject.barbershop.order.mapper.OrderCreationDTOMapper;
import pl.barbershopproject.barbershop.payment.PaymentCreationResult;
import pl.barbershopproject.barbershop.payment.PaymentCreator;
import pl.barbershopproject.barbershop.user.User;

import java.time.Clock;

@Service
@RequiredArgsConstructor
class OrderCreationTransactionService implements OrderCreationTransaction {

    private final OrderRepository orderRepository;
    private final OfferQuery offerQuery;
    private final AppointmentReservation appointmentReservation;
    private final PaymentCreator paymentCreator;
    private final OrderEvents orderEvents;
    private final IdempotencyRequestManager idempotencyRequestManager;
    private final Clock clock;

    @Override
    // Resolves idempotency and persists the order, slot and payment atomically.
    @Transactional
    public OrderCreationTransactionResult create(
            OrderCreationDTO orderCreationDTO,
            User user,
            String idempotencyKey,
            String requestHash
    ) {
        IdempotencyRequestResult idempotencyResult = idempotencyRequestManager.startOrderCreation(
                        idempotencyKey,
                        requestHash,
                        user.getIdUser()
                );

        return switch (idempotencyResult.resolution()) {
            case NEW -> createNewOrder(
                    idempotencyResult.requestId(),
                    orderCreationDTO,
                    user
            );

            case IN_PROGRESS ->
                    OrderCreationTransactionResult.inProgress(
                            idempotencyResult.requestId()
                    );

            case RESOURCE_CREATED ->
                    OrderCreationTransactionResult.resourceCreated(
                            idempotencyResult.requestId(),
                            idempotencyResult.resourceId(),
                            idempotencyResult.checkoutRequest()
                    );

            case COMPLETED ->
                    OrderCreationTransactionResult.completed(
                            idempotencyResult.requestId(),
                            idempotencyResult.resourceId(),
                            idempotencyResult.checkoutRequest(),
                            idempotencyResult.checkoutUrl()
                    );
        };
    }

    private OrderCreationTransactionResult createNewOrder(
            Long idempotencyRequestId,
            OrderCreationDTO orderCreationDTO,
            User user
    ) {
        Offer offer = offerQuery.getRequiredOffer(
                orderCreationDTO.idOffer()
        );

        Order order = OrderCreationDTOMapper.toEntity(
                orderCreationDTO,
                user,
                offer,
                clock
        );

        appointmentReservation.reserveSlot(
                order.getVisitDate()
        );

        Order savedOrder = orderRepository.save(order);

        PaymentCreationResult paymentCreationResult =
                paymentCreator.createForOrder(
                        savedOrder,
                        orderCreationDTO.paymentMethod()
                );

        orderEvents.created(
                savedOrder,
                paymentCreationResult.payment()
        );

        idempotencyRequestManager.markResourceCreated(
                idempotencyRequestId,
                savedOrder.getIdOrder(),
                paymentCreationResult.checkoutRequest()
        );

        return OrderCreationTransactionResult.resourceCreated(
                idempotencyRequestId,
                savedOrder.getIdOrder(),
                paymentCreationResult.checkoutRequest()
        );
    }
}