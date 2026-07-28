package pl.barbershopproject.barbershop.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.offer.Offer;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;

@Schema(description = "Summary of an order associated with a user profile")
public record UserOrdersDTO(
        @Schema(description = "Unique order ID", example = "10")
        Long idOrder,

        @Schema(description = "Reserved service offer")
        Offer offer,

        @Schema(description = "Timestamp when the order was placed", example = "2026-07-20T12:00:00")
        LocalDateTime orderDate,

        @Schema(description = "Scheduled visit date and time", example = "2026-08-15T10:00:00")
        LocalDateTime visitDate,

        @Schema(description = "Status of the reservation", example = "NOWE")
        Status status,

        @Schema(description = "Payment method", example = "KARTA_ONLINE")
        PaymentMethod paymentMethod,

        @Schema(description = "Payment status", example = "OPLACONA")
        PaymentStatus paymentStatus
) {
}