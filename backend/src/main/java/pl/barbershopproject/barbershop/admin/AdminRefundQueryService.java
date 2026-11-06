
package pl.barbershopproject.barbershop.admin;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundHistoryItemDTO;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundHistoryResponseDTO;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundHistorySummaryDTO;
import pl.barbershopproject.barbershop.admin.dto.AdminRefundResponseDTO;
import pl.barbershopproject.barbershop.guestorder.GuestOrder;
import pl.barbershopproject.barbershop.order.Order;
import pl.barbershopproject.barbershop.payment.Payment;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefund;
import pl.barbershopproject.barbershop.payment.refund.PaymentRefundRepository;
import pl.barbershopproject.barbershop.payment.refund.RefundStatus;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Provides read-only access to refund details, history, and statistics.
 */
@Service
@RequiredArgsConstructor
public class AdminRefundQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final EnumSet<RefundStatus> COMPLETED_STATUSES =
            EnumSet.of(RefundStatus.SUCCEEDED);

    private static final EnumSet<RefundStatus> IN_PROGRESS_STATUSES =
            EnumSet.of(
                    RefundStatus.REQUESTED,
                    RefundStatus.SUBMITTING,
                    RefundStatus.PENDING);

    private static final EnumSet<RefundStatus> ATTENTION_REQUIRED_STATUSES =
            EnumSet.of(
                    RefundStatus.FAILED,
                    RefundStatus.CANCELED,
                    RefundStatus.REQUIRES_ACTION,
                    RefundStatus.RECONCILIATION_REQUIRED);

    private final PaymentRefundRepository paymentRefundRepository;

    /**
     * Returns the latest persisted details of a single refund.
     *
     * @param refundId refund identifier
     * @return persisted refund details
     */
    @Transactional(readOnly = true)
    public AdminRefundResponseDTO getRefundDetails(Long refundId) {
        PaymentRefund refund = paymentRefundRepository
                .findById(refundId)
                .orElseThrow(() -> new NoSuchElementException("Zwrot o ID: " + refundId + " nie istnieje"));

        return AdminRefundResponseDTO.from(refund);
    }

    /**
     * Returns paginated refund history with optional filtering.
     * <p>
     * Reservation identifiers are checked against both registered
     * and guest reservations.
     * <p>
     * Summary statistics cover all persisted refunds, regardless
     * of the currently selected history filters.
     *
     * @param refundStatus optional exact refund status
     * @param reservationId optional reservation identifier
     * @param page zero-based page index
     * @param size requested page size
     * @return filtered refund history with global summary statistics
     */
    @Transactional(readOnly = true)
    public AdminRefundHistoryResponseDTO getRefundHistory(
            RefundStatus refundStatus,
            Long reservationId,
            int page,
            int size
    ) {
        validatePaginationParameters(page, size);
        validateReservationId(reservationId);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Order.desc("requestedAt"),
                        Sort.Order.desc("idPaymentRefund")));

        Specification<PaymentRefund> refundFilters = createRefundFilters(refundStatus, reservationId);

        Page<AdminRefundHistoryItemDTO> refundPage = paymentRefundRepository
                        .findAll(refundFilters, pageable)
                        .map(AdminRefundHistoryItemDTO::from);

        AdminRefundHistorySummaryDTO summary = getRefundHistorySummary();

        return AdminRefundHistoryResponseDTO.from(refundPage, summary);
    }

    /**
     * Builds database-level filters for the refund history.
     */
    private Specification<PaymentRefund> createRefundFilters(
            RefundStatus refundStatus,
            Long reservationId
    ) {
        return (refundRoot, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (refundStatus != null) {
                predicates.add(criteriaBuilder.equal(
                                refundRoot.get("refundStatus"),
                                refundStatus));
            }

            if (reservationId != null) {
                Join<PaymentRefund, Payment> paymentJoin = refundRoot.join("payment");

                Join<Payment, Order> registeredOrderJoin = paymentJoin.join("order", JoinType.LEFT);

                Join<Payment, GuestOrder> guestOrderJoin = paymentJoin.join("guestOrder", JoinType.LEFT);

                Predicate matchesRegisteredReservation = criteriaBuilder.equal(
                                registeredOrderJoin.get("idOrder"),
                                reservationId);

                Predicate matchesGuestReservation = criteriaBuilder.equal(
                                guestOrderJoin.get("idGuestOrder"),
                                reservationId);

                predicates.add(criteriaBuilder.or(
                                matchesRegisteredReservation,
                                matchesGuestReservation));
            }

            return criteriaBuilder.and(predicates.toArray(Predicate[]::new));
        };
    }

    /**
     * Counts refunds by processing outcome for dashboard statistics.
     */
    private AdminRefundHistorySummaryDTO getRefundHistorySummary() {
        long completedRefunds = paymentRefundRepository.countByRefundStatusIn(COMPLETED_STATUSES);

        long refundsInProgress = paymentRefundRepository.countByRefundStatusIn(IN_PROGRESS_STATUSES);

        long refundsRequiringAttention = paymentRefundRepository.countByRefundStatusIn(
                        ATTENTION_REQUIRED_STATUSES);

        return new AdminRefundHistorySummaryDTO(
                completedRefunds,
                refundsInProgress,
                refundsRequiringAttention);
    }

    /**
     * Prevents invalid or excessively large paginated queries.
     */
    private void validatePaginationParameters(int page, int size) {
        if (page < 0)
        {
            throw new IllegalArgumentException("Numer strony nie może być ujemny");
        }

        if (size < 1 || size > MAX_PAGE_SIZE) {
            throw new IllegalArgumentException("Liczba elementów na stronie musi wynosić od 1 do "
                            + MAX_PAGE_SIZE);
        }
    }

    /**
     * Validates the optional reservation identifier filter.
     */
    private void validateReservationId(Long reservationId) {
        if (reservationId != null && reservationId <= 0) {
            throw new IllegalArgumentException("ID rezerwacji musi być większe od zera");
        }
    }
}
