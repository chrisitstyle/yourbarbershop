package pl.barbershopproject.barbershop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.offer.dto.BookedOfferDTO;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.utils.Status;

import java.io.Serializable;
import java.time.LocalDateTime;

@Schema(description = "Complete details of an order/reservation")
public record OrderDTO(
        @Schema(description = "Unique order ID", example = "10")
        Long idOrder,

        @Schema(description = "Basic details of the user who made the reservation")
        UserInOrderDTO user,

        @Schema(description = "Reserved barber service offer")
        BookedOfferDTO offer,

        @Schema(description = "Timestamp when the order was placed", example = "2026-07-20T12:00:00")
        LocalDateTime orderDate,

        @Schema(description = "Scheduled visit date and time", example = "2026-08-15T10:00:00")
        LocalDateTime visitDate,

        @Schema(description = "Current status of the reservation", example = "NOWE")
        Status status,

        @Schema(description = "Selected payment method", example = "GOTOWKA_NA_MIEJSCU")
        PaymentMethod paymentMethod,

        @Schema(description = "Status of the payment", example = "NIE_WYMAGANA")
        PaymentStatus paymentStatus
) implements Serializable {
}
