package pl.barbershopproject.barbershop.order.mapper;

import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.order.dto.UserInOrderDTO;
import pl.barbershopproject.barbershop.order.dto.OrderDTO;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

public class OrderDTOMapper {

    private OrderDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static OrderDTO toDTO(Order order) {
        UserInOrderDTO userInOrder = new UserInOrderDTO(
                order.getUser().getIdUser(),
                order.getUser().getFirstname(),
                order.getUser().getLastname(),
                order.getUser().getEmail()
        );

        Payment payment = order.getPayment();

        PaymentMethod paymentMethod = payment != null
                ? payment.getPaymentMethod()
                : null;

        PaymentStatus paymentStatus = payment != null
                ? payment.getPaymentStatus()
                : null;

        return new OrderDTO(
                order.getIdOrder(),
                userInOrder,
                order.getOffer(),
                order.getOrderDate(),
                order.getVisitDate(),
                order.getStatus(),
                paymentMethod,
                paymentStatus
        );
    }
}