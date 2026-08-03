package pl.barbershopproject.barbershop.guestorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.offer.dto.BookedOfferDTO;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;
import pl.barbershopproject.barbershop.utils.Status;

import java.time.LocalDateTime;

@Schema(description = "Complete details of a guest reservation")
public record GuestOrderDTO(
        @Schema(description = "Unique guest order ID", example = "15")
        Long idGuestOrder,

        @Schema(description = "Guest's first name", example = "Jan")
        String firstname,

        @Schema(description = "Guest's last name", example = "Kowalski")
        String lastname,

        @Schema(description = "Guest's phone number", example = "+48123456789")
        String phonenumber,

        @Schema(description = "Guest's email address", example = "jan.kowalski@example.com")
        String email,

        @Schema(description = "Reserved service offer details")
        BookedOfferDTO offer,

        @Schema(description = "Timestamp when the order was placed", example = "2026-07-21T09:15:00")
        LocalDateTime orderDate,

        @Schema(description = "Scheduled visit date and time", example = "2026-08-15T14:30:00")
        LocalDateTime visitDate,

        @Schema(description = "Status of the reservation", example = "NOWE")
        Status status,

        @Schema(description = "Selected payment method", example = "KARTA_ONLINE")
        PaymentMethod paymentMethod,

        @Schema(description = "Payment status", example = "COMPLETED")
        PaymentStatus paymentStatus
) {
}
