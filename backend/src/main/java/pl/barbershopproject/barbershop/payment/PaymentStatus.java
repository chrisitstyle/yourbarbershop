package pl.barbershopproject.barbershop.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Processing status of an order's payment")
public enum PaymentStatus {

    @Schema(description = "No online payment required (e.g. pay-at-salon option)")
    NIE_WYMAGANA,

    @Schema(description = "Awaiting customer payment via Stripe")
    OCZEKUJE_NA_PLATNOSC,

    @Schema(description = "Payment completed successfully")
    OPLACONA,

    @Schema(description = "Payment processing failed")
    NIEUDANA,

    @Schema(description = "Payment session expired before completion")
    WYGASLA,

    @Schema(description = "Payment has been refunded")
    ZWROCONA
}
