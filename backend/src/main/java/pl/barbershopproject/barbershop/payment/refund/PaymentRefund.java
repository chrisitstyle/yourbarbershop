package pl.barbershopproject.barbershop.payment.refund;

import jakarta.persistence.*;
import lombok.*;
import pl.barbershopproject.barbershop.payment.Payment;

import java.math.BigDecimal;
import java.time.Instant;


/**
 * Represents a refund associated with a payment.
 * Stores refund details, processing status, and Stripe transaction information.
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(
name = "payment_refund",
indexes = {
        @Index(
                name = "idx_payment_refund_payment",
                columnList = "id_payment")})
public class PaymentRefund {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_payment_refund")
    private Long idPaymentRefund;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_payment", nullable = false)
    private Payment payment;

    @Column(name = "stripe_refund_id", unique = true)
    private String stripeRefundId;

    @Column(
            name = "idempotency_key",
            nullable = false,
            unique = true,
            length = 64)
    private String idempotencyKey;

    @Column(
            name = "amount",
            nullable = false,
            precision = 12,
            scale = 2)
    private BigDecimal amount;

    @Column(name = "currency", nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_status", nullable = false, length = 32)
    private RefundStatus refundStatus;

    @Column(name = "requested_at", nullable = false)
    private Instant requestedAt;

    @Column(name = "completed_at")
    private Instant completedAt;

    @Column(name = "failure_reason", length = 1000)
    private String failureReason;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;
}
