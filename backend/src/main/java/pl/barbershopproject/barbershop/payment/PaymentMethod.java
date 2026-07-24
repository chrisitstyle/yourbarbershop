package pl.barbershopproject.barbershop.payment;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Available payment methods for reservations")
public enum PaymentMethod {

    @Schema(description = "Cash payment on site at the barbershop")
    GOTOWKA,

    @Schema(description = "Online card payment handled via Stripe Checkout")
    KARTA_ONLINE,

    @Schema(description = "Card payment on site at the payment terminal")
    KARTA_NA_MIEJSCU
}

