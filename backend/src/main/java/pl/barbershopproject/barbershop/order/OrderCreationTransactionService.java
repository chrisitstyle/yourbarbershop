package pl.barbershopproject.barbershop.order;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.appointment.AppointmentReservation;
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
    private final Clock clock;

    @Override
    // persists the order, appointment slot and payment atomically in one transaction.
    @Transactional
    public OrderCreationTransactionResult create(
            OrderCreationDTO orderCreationDTO,
            User user
    ) {
        Offer offer = offerQuery.getRequiredOffer(orderCreationDTO.idOffer());

        Order order = OrderCreationDTOMapper.toEntity(
                orderCreationDTO,
                user,
                offer,
                clock
        );

        appointmentReservation.reserveSlot(order.getVisitDate());

        Order savedOrder = orderRepository.save(order);

        PaymentCreationResult paymentCreationResult = paymentCreator.createForOrder(savedOrder,
                orderCreationDTO.paymentMethod()
        );

        orderEvents.created(savedOrder, paymentCreationResult.payment());

        return new OrderCreationTransactionResult(
                savedOrder.getIdOrder(),
                paymentCreationResult.checkoutRequest()
        );
    }
}
