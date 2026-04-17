package pl.barbershopproject.barbershop.order.event;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record OrderCreatedEvent(String email,
                                String firstname,
                                LocalDateTime visitDate,
                                String offerKind,
                                BigDecimal offerCost) {
}
