package pl.barbershopproject.barbershop.guestorder;

import pl.barbershopproject.barbershop.idempotency.IdempotencyResolution;
import pl.barbershopproject.barbershop.ordercreation.CreationTransactionResult;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

import java.util.Objects;

/**
 * Represents the result of resolving or creating an idempotent guest order,
 * including the idempotency state and payment checkout data required to
 * complete the creation flow.
 *
 * @param idempotencyRequestId identifier of the persisted idempotency request
 * @param resolution           current resolution of the idempotent operation
 * @param guestOrderId         identifier of the persisted guest order, if already created
 * @param checkoutRequest      immutable payment data used to create or resume checkout
 * @param checkoutUrl          checkout URL for online payment, or {@code null} when checkout
 *                             is not required or has not been completed yet
 */
record GuestOrderCreationTransactionResult(
        Long idempotencyRequestId,
        IdempotencyResolution resolution,
        Long guestOrderId,
        PaymentCheckoutRequest checkoutRequest,
        String checkoutUrl
) implements CreationTransactionResult {

    GuestOrderCreationTransactionResult {
        Objects.requireNonNull(
                idempotencyRequestId,
                "Idempotency request ID nie może być null"
        );

        Objects.requireNonNull(
                resolution,
                "Idempotency resolution nie może być null"
        );

        if (resolution == IdempotencyResolution.IN_PROGRESS) {
            if (guestOrderId != null
                    || checkoutRequest != null
                    || checkoutUrl != null) {
                throw new IllegalArgumentException(
                        "Przetwarzane żądanie nie może zawierać wyniku zamówienia"
                );
            }
        } else if (resolution == IdempotencyResolution.RESOURCE_CREATED
                || resolution == IdempotencyResolution.COMPLETED) {
            Objects.requireNonNull(
                    guestOrderId,
                    "GuestOrder ID nie może być null"
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

    static GuestOrderCreationTransactionResult inProgress(
            Long idempotencyRequestId
    ) {
        return new GuestOrderCreationTransactionResult(
                idempotencyRequestId,
                IdempotencyResolution.IN_PROGRESS,
                null,
                null,
                null
        );
    }

    static GuestOrderCreationTransactionResult resourceCreated(
            Long idempotencyRequestId,
            Long guestOrderId,
            PaymentCheckoutRequest checkoutRequest
    ) {
        return new GuestOrderCreationTransactionResult(
                idempotencyRequestId,
                IdempotencyResolution.RESOURCE_CREATED,
                guestOrderId,
                checkoutRequest,
                null
        );
    }

    static GuestOrderCreationTransactionResult completed(
            Long idempotencyRequestId,
            Long guestOrderId,
            PaymentCheckoutRequest checkoutRequest,
            String checkoutUrl
    ) {
        return new GuestOrderCreationTransactionResult(
                idempotencyRequestId,
                IdempotencyResolution.COMPLETED,
                guestOrderId,
                checkoutRequest,
                checkoutUrl
        );
    }
}
