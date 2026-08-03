package pl.barbershopproject.barbershop.order;

import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

import java.util.Objects;

/**
 * Contains data produced by the committed order creation transaction.
 *
 * @param orderId identifier of the persisted order
 * @param checkoutRequest immutable payment data used after transaction commit
 */
record OrderCreationTransactionResult(
        Long orderId,PaymentCheckoutRequest checkoutRequest) {

    OrderCreationTransactionResult {
        Objects.requireNonNull(orderId,"Order ID nie może być null");

        Objects.requireNonNull(checkoutRequest,"PaymentCheckoutRequest nie może być null");
    }
}
