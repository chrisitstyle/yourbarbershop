package pl.barbershopproject.barbershop.payment.refund;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for managing payment refunds and retrieving refund records.
 */
public interface PaymentRefundRepository extends JpaRepository<PaymentRefund, Long> {

    Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId);

    Optional<PaymentRefund> findByIdempotencyKey(String idempotencyKey);

    List<PaymentRefund> findAllByPayment_IdPaymentOrderByRequestedAtDesc(Long paymentId);
}
