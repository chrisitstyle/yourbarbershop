
package pl.barbershopproject.barbershop.payment.refund;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.PaymentMethod;
import pl.barbershopproject.barbershop.payment.PaymentRepository;
import pl.barbershopproject.barbershop.payment.PaymentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Manages transactional persistence and concurrency control for refunds.
 */
@Service
@RequiredArgsConstructor
public class RefundTransactionService {

    private final PaymentRepository paymentRepository;
    private final PaymentRefundRepository paymentRefundRepository;

    @Transactional
    public RefundReservation reserveFullRefund(Long paymentId) {
        Payment payment = paymentRepository
                .findForUpdateByIdPayment(paymentId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Payment not found"));

        validateRefundEligibility(payment);

        if (paymentRefundRepository.existsByPayment_IdPayment(paymentId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A refund request already exists for this payment");
        }

        PaymentRefund refund = PaymentRefund.builder()
                .payment(payment)
                .idempotencyKey(UUID.randomUUID().toString())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .refundStatus(RefundStatus.REQUESTED)
                .requestedAt(Instant.now())
                .build();

        PaymentRefund savedRefund = paymentRefundRepository.saveAndFlush(refund);

        long amountInMinorUnits = payment.getAmount()
                .movePointRight(2)
                .longValueExact();

        return new RefundReservation(
                savedRefund.getIdPaymentRefund(),
                payment.getStripePaymentIntentId(),
                amountInMinorUnits,
                payment.getCurrency(),
                savedRefund.getIdempotencyKey());
    }

    @Transactional
    public void markReconciliationRequired(Long refundId) {
        PaymentRefund paymentRefund = getRefundForUpdate(refundId);

        if (paymentRefund.getRefundStatus() == RefundStatus.SUCCEEDED
                || paymentRefund.getRefundStatus() == RefundStatus.FAILED
                || paymentRefund.getRefundStatus() == RefundStatus.CANCELED) {
            return;
        }

        paymentRefund.setRefundStatus(RefundStatus.RECONCILIATION_REQUIRED);

        paymentRefund.setFailureReason("Stripe refund outcome requires reconciliation");
    }


    @Transactional(readOnly = true)
    public RefundLookupTarget loadRefundForReconciliation(Long refundId) {
        PaymentRefund paymentRefund = paymentRefundRepository
                .findById(refundId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Refund not found"));

        return new RefundLookupTarget(
                paymentRefund.getIdPaymentRefund(),
                paymentRefund.getRefundStatus(),
                paymentRefund.getStripeRefundId(),
                paymentRefund.getPayment().getStripePaymentIntentId());
    }

    public record RefundLookupTarget(
            Long refundId,
            RefundStatus refundStatus,
            String stripeRefundId,
            String paymentIntentId
    ) {
    }


    private void validateRefundEligibility(Payment payment) {
        if (payment.getPaymentMethod() != PaymentMethod.KARTA_ONLINE
                || payment.getPaymentStatus() != PaymentStatus.OPLACONA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Only completed online payments can be refunded");
        }

        if (payment.getStripePaymentIntentId() == null
                || payment.getStripePaymentIntentId().isBlank()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Stripe PaymentIntent is missing");
        }

        // this implementation supports PLN (two decimal places)
        if (!"PLN".equalsIgnoreCase(payment.getCurrency())
                || payment.getAmount() == null
                || payment.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Invalid payment amount or currency");
        }

        try {
            payment.getAmount().movePointRight(2).longValueExact();
        } catch (ArithmeticException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Payment amount cannot be represented in grosze");
        }
    }

    private PaymentRefund getRefundForUpdate(Long refundId) {
        return paymentRefundRepository
                .findForUpdateByIdPaymentRefund(refundId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Refund not found"));
    }

    public record RefundReservation(
            Long refundId,
            String paymentIntentId,
            long amountInMinorUnits,
            String currency,
            String idempotencyKey) {
    }
}
