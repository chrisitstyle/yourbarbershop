package pl.barbershopproject.barbershop.order.event;

import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(
        String email,
        String firstname,
        LocalDateTime visitDate,
        String offerKind,
        BigDecimal offerCost,
        PaymentMethod paymentMethod,
        PaymentStatus paymentStatus
) {
}
