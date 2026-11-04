package pl.barbershopproject.barbershop.payment.refund;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing payment refunds and retrieving refund records.
 */
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId);

    Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey);

    List<PaymentRefund> findAllByPayment_IdPaymentOrderByRequestedAtDesc(Long paymentId);

    boolean existsByPayment_IdPayment(Long paymentId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentRefund> findForUpdateByIdPaymentRefund(Long refundId);

    List<PaymentRefund>
    findByRefundStatusInAndRequestedAtBeforeAndIdPaymentRefundGreaterThanOrderByIdPaymentRefundAsc(
            Collection<RefundStatus> refundStatuses,
            Instant requestedBefore,
            Long lastProcessedRefundId,
            Pageable pageable
    );

}
