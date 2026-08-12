package pl.barbershopproject.barbershop.order;

import pl.barbershopproject.barbershop.idempotency.IdempotencyResolution;
import pl.barbershopproject.barbershop.ordercreation.CreationTransactionResult;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

import java.util.Objects;

/**
 * Represents the result of resolving or creating an idempotent order,
 * including the idempotency state and payment checkout data required to
 * complete the creation flow.
 *
 * @param idempotencyRequestId identifier of the persisted idempotency request
 * @param resolution           current resolution of the idempotent operation
 * @param orderId              identifier of the persisted order, if already created
 * @param checkoutRequest      immutable payment data used to create or resume checkout
 * @param checkoutUrl          checkout URL for online payment, or {@code null} when checkout
 *                             is not required or has not been completed yet
 */
record OrderCreationTransactionResult(
        Long idempotencyRequestId,
        IdempotencyResolution resolution,
        Long orderId,
        PaymentCheckoutRequest checkoutRequest,
        String checkoutUrl
) implements CreationTransactionResult {

    OrderCreationTransactionResult {
        Objects.requireNonNull(
                idempotencyRequestId,
                "Idempotency request ID nie może być null"
        );

        Objects.requireNonNull(
                resolution,
                "Idempotency resolution nie może być null"
        );

        if (resolution == IdempotencyResolution.IN_PROGRESS) {
            if (orderId != null
                    || checkoutRequest != null
                    || checkoutUrl != null) {
                throw new IllegalArgumentException(
                        "Przetwarzane żądanie nie może zawierać wyniku zamówienia"
                );
            }
        } else if (resolution == IdempotencyResolution.RESOURCE_CREATED
                || resolution == IdempotencyResolution.COMPLETED) {
            Objects.requireNonNull(
                    orderId,
                    "Order ID nie może być null"
            );

            Objects.requireNonNull(
                    checkoutRequest,
                    "PaymentCheckoutRequest nie może być null"
            );
        } else {
            throw new IllegalArgumentException(
                    "Stan NEW nie może opuścić transakcji tworzenia zamówienia"
            );
        }
    }

    static OrderCreationTransactionResult inProgress(
            Long idempotencyRequestId
    ) {
        return new OrderCreationTransactionResult(
                idempotencyRequestId,
                IdempotencyResolution.IN_PROGRESS,
                null,
                null,
                null
        );
    }

    static OrderCreationTransactionResult resourceCreated(
            Long idempotencyRequestId,
            Long orderId,
            PaymentCheckoutRequest checkoutRequest
    ) {
        return new OrderCreationTransactionResult(
                idempotencyRequestId,
                IdempotencyResolution.RESOURCE_CREATED,
                orderId,
                checkoutRequest,
                null
        );
    }

    static OrderCreationTransactionResult completed(
            Long idempotencyRequestId,
            Long orderId,
            PaymentCheckoutRequest checkoutRequest,
            String checkoutUrl
    ) {
        return new OrderCreationTransactionResult(
                idempotencyRequestId,
                IdempotencyResolution.COMPLETED,
                orderId,
                checkoutRequest,
                checkoutUrl
        );
    }
}
