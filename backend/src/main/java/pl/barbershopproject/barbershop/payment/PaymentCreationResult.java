package pl.barbershopproject.barbershop.payment;

/**
 * Represents the result of the payment creation process.
 *
 * <p>The result contains the persisted payment and, when an online
 * payment is required, the URL of the external checkout session.</p>
 *
 * @param payment created and persisted payment
 * @param checkoutUrl checkout session URL, or {@code null} when the selected
 *                    payment method does not require online checkout
 */
public record PaymentCreationResult(
        Payment payment,
        String checkoutUrl
) {
}