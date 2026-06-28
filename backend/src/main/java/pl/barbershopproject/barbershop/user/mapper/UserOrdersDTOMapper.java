package pl.barbershopproject.barbershop.user.mapper;

import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.user.dto.UserOrdersDTO;

public class UserOrdersDTOMapper {
    private UserOrdersDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static UserOrdersDTO toOrdersInUserDTO(Order order) {
        Payment payment = order.getPayment();

        PaymentMethod paymentMethod = payment != null
                ? payment.getPaymentMethod()
                : null;

        PaymentStatus paymentStatus = payment != null
                ? payment.getPaymentStatus()
                : null;

        return new UserOrdersDTO(
                order.getIdOrder(),
                order.getOffer(),
                order.getOrderDate(),
                order.getVisitDate(),
                order.getStatus(),
                paymentMethod,
                paymentStatus
//                order.getPayment().getPaymentMethod(),
//                order.getPayment().getPaymentStatus()
        );
    }
}
