package pl.barbershopproject.barbershop.order.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.payment.PaymentMethod;

import java.time.LocalDateTime;

public record OrderCreationDTO(
        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @NotNull(message = "Data wizyty jest wymagana")
        @Future(message = "Data wizyty musi być w przyszłości")
        LocalDateTime visitDate,

        @NotNull(message = "Metoda płatności jest wymagana")
        PaymentMethod paymentMethod
) {
}