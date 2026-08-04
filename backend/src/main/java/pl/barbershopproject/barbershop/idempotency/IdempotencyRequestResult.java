package pl.barbershopproject.barbershop.idempotency;

import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

import java.util.Objects;

/**
 * Contains the result of resolving an Idempotency-Key.
 *
 * <p>Depending on the resolution, the caller may create a new order,
 * resume checkout processing or return the stored response.</p>
 */
public record IdempotencyRequestResult(
        Long requestId,
        IdempotencyResolution resolution,
        Long resourceId,
        PaymentCheckoutRequest checkoutRequest,
        String checkoutUrl
) {

    public IdempotencyRequestResult {
        Objects.requireNonNull(requestId, "Idempotency request ID nie może być null");
        Objects.requireNonNull(resolution, "Idempotency resolution nie może być null");

        boolean resourceExists = resolution == IdempotencyResolution.RESOURCE_CREATED
                || resolution == IdempotencyResolution.COMPLETED;

        if (resourceExists) {
            Objects.requireNonNull(resourceId, "Resource ID nie może być null");
            Objects.requireNonNull(checkoutRequest, "PaymentCheckoutRequest nie może być null");
        }

        if (!resourceExists && (resourceId != null || checkoutRequest != null || checkoutUrl != null)) {
            throw new IllegalArgumentException(
                    "Żądanie bez utworzonego zasobu nie może zawierać wyniku operacji"
            );
        }
    }

    public boolean isNew() {
        return resolution == IdempotencyResolution.NEW;
    }

    public boolean isInProgress() {
        return resolution == IdempotencyResolution.IN_PROGRESS;
    }

    public boolean requiresCompletion() {
        return resolution == IdempotencyResolution.RESOURCE_CREATED;
    }

    public boolean isCompleted() {
        return resolution == IdempotencyResolution.COMPLETED;
    }
}
