package pl.barbershopproject.barbershop.idempotency;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Stores the state and result of an idempotent order creation request.
 *
 * <p>When the client repeats a request with the same Idempotency-Key,
 * this entity allows the application to return the original result instead
 * of creating another order, payment or Stripe Checkout Session.</p>
 *
 * <p>The request hash also prevents the same key from being reused
 * with different request data.</p>
 */
@Entity
@Table(name = "idempotency_request")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class IdempotencyRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_idempotency_request")
    private Long idIdempotencyRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private IdempotencyOperation operation;

    @Column(name = "idempotency_key", nullable = false, length = 255)
    private String idempotencyKey;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Column(name = "owner_user_id")
    private Long ownerUserId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private IdempotencyStatus status;

    @Column(name = "resource_id")
    private Long resourceId;

    @Column(name = "payment_id")
    private Long paymentId;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_method", length = 50)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", length = 50)
    private PaymentStatus paymentStatus;

    @Column(precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency;

    @Column(name = "product_name", length = 255)
    private String productName;

    @Column(name = "checkout_url", length = 2048)
    private String checkoutUrl;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    /**
     * Creates a new idempotency record before the order creation process starts.
     */
    static IdempotencyRequest start(
            IdempotencyOperation operation,
            String idempotencyKey,
            String requestHash,
            Long ownerUserId,
            Clock clock
    ) {
        Objects.requireNonNull(
                operation,
                "Operacja idempotentna nie może być null"
        );

        Objects.requireNonNull(clock, "Clock nie może być null");

        String requiredKey = requireText(
                idempotencyKey,
                "Idempotency-Key nie może być pusty"
        );

        if (requiredKey.length() > 255) {
            throw new IllegalArgumentException(
                    "Idempotency-Key nie może przekraczać 255 znaków"
            );
        }

        String requiredHash = requireText(
                requestHash,
                "Hash żądania nie może być pusty"
        );

        if (requiredHash.length() != 64) {
            throw new IllegalArgumentException(
                    "Hash żądania musi mieć 64 znaki"
            );
        }

        LocalDateTime now = LocalDateTime.now(clock);

        IdempotencyRequest request = new IdempotencyRequest();
        request.operation = operation;
        request.idempotencyKey = requiredKey;
        request.requestHash = requiredHash;
        request.ownerUserId = ownerUserId;
        request.status = IdempotencyStatus.PROCESSING;
        request.createdAt = now;
        request.updatedAt = now;

        return request;
    }

    /**
     * Stores the order and payment result after the database transaction
     * has successfully created all required resources.
     */
    void markResourceCreated(
            Long resourceId,
            PaymentCheckoutRequest checkoutRequest,
            Clock clock
    ) {
        Objects.requireNonNull(clock, "Clock nie może być null");

        if (status != IdempotencyStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Żądanie idempotentne nie znajduje się w stanie PROCESSING"
            );
        }

        Objects.requireNonNull(
                resourceId,
                "ID utworzonego zasobu nie może być null"
        );

        Objects.requireNonNull(
                checkoutRequest,
                "PaymentCheckoutRequest nie może być null"
        );

        this.resourceId = resourceId;
        this.paymentId = checkoutRequest.paymentId();
        this.paymentMethod = checkoutRequest.paymentMethod();
        this.paymentStatus = checkoutRequest.paymentStatus();
        this.amount = checkoutRequest.amount();
        this.currency = checkoutRequest.currency();
        this.productName = checkoutRequest.productName();
        this.status = IdempotencyStatus.RESOURCE_CREATED;
        this.updatedAt = LocalDateTime.now(clock);
    }

    /**
     * Stores the final response after checkout processing has finished.
     *
     * <p>The checkout URL remains null for payment methods that do not
     * require Stripe Checkout.</p>
     */
    void markCompleted(String checkoutUrl, Clock clock) {
        Objects.requireNonNull(clock, "Clock nie może być null");

        if (status == IdempotencyStatus.COMPLETED) {
            if (Objects.equals(this.checkoutUrl, checkoutUrl)) {
                return;
            }

            throw new IllegalStateException(
                    "Żądanie idempotentne zostało już zakończone z innym wynikiem"
            );
        }

        if (status != IdempotencyStatus.RESOURCE_CREATED) {
            throw new IllegalStateException(
                    "Nie można zakończyć żądania przed utworzeniem zasobu"
            );
        }

        LocalDateTime now = LocalDateTime.now(clock);

        this.checkoutUrl = checkoutUrl;
        this.status = IdempotencyStatus.COMPLETED;
        this.updatedAt = now;
        this.completedAt = now;
    }

    /**
     * Rebuilds the immutable payment data needed to resume
     * Stripe Checkout creation after an interrupted request.
     */
    PaymentCheckoutRequest toCheckoutRequest() {
        if (status == IdempotencyStatus.PROCESSING) {
            throw new IllegalStateException(
                    "Dane płatności nie zostały jeszcze zapisane"
            );
        }

        return new PaymentCheckoutRequest(
                paymentId,
                paymentMethod,
                paymentStatus,
                amount,
                currency,
                productName
        );
    }

    /**
     * Checks whether a repeated request contains the same payload
     * as the original request.
     */
    boolean hasRequestHash(String requestHash) {
        return this.requestHash.equals(requestHash);
    }

    /**
     * Checks whether the idempotency record belongs to the authenticated user.
     */
    boolean belongsTo(Long userId) {
        return Objects.equals(ownerUserId, userId);
    }

    /**
     * Indicates that the stored response can be returned without
     * executing any additional business logic.
     */
    boolean isCompleted() {
        return status == IdempotencyStatus.COMPLETED;
    }

    private static String requireText(String value, String message) {
        Objects.requireNonNull(value, message);

        if (value.isBlank()) {
            throw new IllegalArgumentException(message);
        }

        return value;
    }
}