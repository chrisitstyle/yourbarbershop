package pl.barbershopproject.barbershop.guestorder;

import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

import java.util.Objects;

/**
 * Contains data produced by the committed guest-order creation transaction.
 *
 * @param guestOrderId identifier of the persisted guest order
 * @param checkoutRequest immutable payment data used after transaction commit
 */
record GuestOrderCreationTransactionResult(
        Long guestOrderId,
        PaymentCheckoutRequest checkoutRequest
) {

    GuestOrderCreationTransactionResult {
        Objects.requireNonNull(guestOrderId,
                "GuestOrder ID nie może być null");

        Objects.requireNonNull(checkoutRequest, "PaymentCheckoutRequest nie może być null");
    }
}
