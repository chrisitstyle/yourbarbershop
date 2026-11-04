
package pl.barbershopproject.barbershop.admin;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundResponseDTO;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefundRepository;

import java.util.NoSuchElementException;

/**
 * Provides read-only access to persisted refund details.
 */
@Service
@RequiredArgsConstructor
public class AdminRefundQueryService {

    private final PaymentRefundRepository paymentRefundRepository;

    @Transactional(readOnly = true)
    public AdminRefundResponseDTO getRefundDetails(Long refundId) {
        PaymentRefund refund = paymentRefundRepository.findById(refundId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Zwrot o ID: " + refundId + " nie istnieje"));

        return AdminRefundResponseDTO.from(refund);
    }
}
