package pl.barbershopproject.barbershop.payment.refund;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Repository for managing payment refunds and retrieving refund records.
 */
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long>,
        JpaSpecificationExecutor<PaymentRefund> {

    Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId);

    Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey);

    List<PaymentRefund> findAllByPayment_IdPaymentOrderByRequestedAtDesc(Long paymentId);

    boolean existsByPayment_IdPayment(Long paymentId);

    /**
     * Loads a refund using a pessimistic lock during status updates.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PaymentRefund> findForUpdateByIdPaymentRefund(Long refundId);

    /**
     * Retrieves paginated refunds together with the associated
     * payment and reservation details.
     */
    @Override
    @EntityGraph(attributePaths = {
            "payment",
            "payment.order",
            "payment.guestOrder"
    })
    Page<PaymentRefund> findAll(
            Specification<PaymentRefund> specification,
            Pageable pageable);

    /**
     * Counts refunds matching the specified lifecycle statuses.
     */
    long countByRefundStatusIn(Collection<RefundStatus> refundStatuses);

    /**
     * Retrieves refund candidates for background reconciliation.
     */
    List<PaymentRefund>
    findByRefundStatusInAndRequestedAtBeforeAndIdPaymentRefundGreaterThanOrderByIdPaymentRefundAsc(
            Collection<RefundStatus> refundStatuses,
            Instant requestedBefore,
            Long lastProcessedRefundId,
            Pageable pageable
    );

}
