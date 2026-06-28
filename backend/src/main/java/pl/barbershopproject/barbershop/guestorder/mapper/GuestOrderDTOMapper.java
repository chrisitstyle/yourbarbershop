package pl.barbershopproject.barbershop.guestorder.mapper;

import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderDTO;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

public class GuestOrderDTOMapper {

    private GuestOrderDTOMapper() {
        throw new IllegalStateException("Utility class");
    }

    public static GuestOrderDTO toDTO(GuestOrder guestOrder) {
        Payment payment = guestOrder.getPayment();

        PaymentMethod paymentMethod = payment != null
                ? payment.getPaymentMethod()
                : null;

        PaymentStatus paymentStatus = payment != null
                ? payment.getPaymentStatus()
                : null;

        return new GuestOrderDTO(
                guestOrder.getIdGuestOrder(),
                guestOrder.getFirstname(),
                guestOrder.getLastname(),
                guestOrder.getPhonenumber(),
                guestOrder.getEmail(),
                guestOrder.getOffer(),
                guestOrder.getOrderDate(),
                guestOrder.getVisitDate(),
                guestOrder.getStatus(),
                paymentMethod,
                paymentStatus
        );
    }
}
