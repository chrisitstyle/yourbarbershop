package pl.barbershopproject.barbershop.guestorder.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

@Schema(description = "Response returned after creating a guest reservation")
public record GuestOrderCreationResponseDTO(
        @Schema(description = "Unique ID of the created guest order", example = "15")
        Long guestOrderId,

        @Schema(description = "Selected payment method", example = "KARTA_ONLINE")
        PaymentMethod paymentMethod,

        @Schema(description = "Initial status of the payment", example = "PENDING")
        PaymentStatus paymentStatus,

        @Schema(description = "Stripe Checkout URL if online payment was chosen", example = "https://checkout.stripe.com/c/pay/cs_test_abc123")
        String checkoutUrl
) {
}
