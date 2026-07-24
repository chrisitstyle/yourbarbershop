package pl.barbershopproject.barbershop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import pl.barbershopproject.barbershop.payment.PaymentMethod;

import java.time.LocalDateTime;

@Schema(description = "Payload for placing a new reservation as a logged-in user")
public record OrderCreationDTO(
        @Schema(description = "ID of the selected service offer", example = "1")
        @NotNull(message = "ID oferty jest wymagane")
        Long idOffer,

        @Schema(description = "Scheduled appointment date and time", example = "2026-08-15T10:00:00")
        @NotNull(message = "Data wizyty jest wymagana")
        @Future(message = "Data wizyty musi być w przyszłości")
        LocalDateTime visitDate,

        @Schema(description = "Chosen payment method", example = "GOTOWKA_NA_MIEJSCU")
        @NotNull(message = "Metoda płatności jest wymagana")
        PaymentMethod paymentMethod
) {
}