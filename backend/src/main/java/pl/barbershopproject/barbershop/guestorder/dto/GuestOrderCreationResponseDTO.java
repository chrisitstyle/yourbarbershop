package pl.barbershopproject.barbershop.guestorder.dto;

import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

public record GuestOrderCreationResponseDTO(
        Long guestOrderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String checkoutUrl
) {
}
