package pl.barbershopproject.barbershop.order.dto;

import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

public record OrderCreationResponseDTO(
        Long orderId,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus,
        String checkoutUrl
) {
}
