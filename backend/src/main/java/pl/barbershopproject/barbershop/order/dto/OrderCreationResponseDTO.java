package pl.barbershopproject.barbershop.order.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

@Schema(description = "Response returned after successfully creating an order, including Stripe redirect URL if applicable")
public record OrderCreationResponseDTO(
        @Schema(description = "Unique ID of the created order", example = "10")
        Long orderId,

        @Schema(description = "Selected payment method", example = "KARTA_ONLINE")
        PaymentMethod paymentMethod,

        @Schema(description = "Initial payment status", example = "PENDING")
        PaymentStatus paymentStatus,

        @Schema(description = "Stripe Checkout session URL if online payment was selected", example = "https://checkout.stripe.com/c/pay/cs_test_123")
        String checkoutUrl
) {
}
