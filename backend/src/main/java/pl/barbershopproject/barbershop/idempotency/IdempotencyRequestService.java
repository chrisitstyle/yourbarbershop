package pl.barbershopproject.barbershop.idempotency;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import pl.barbershopproject.barbershop.exception.IdempotencyConflictException;
import pl.barbershopproject.barbershop.payment.PaymentCheckoutRequest;

import java.time.Clock;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * Persists and resolves Idempotency-Key records used during order creation.
 *
 * <p>Registration and resource assignment participate in the order transaction,
 * while final checkout completion is stored in a separate transaction.</p>
 */
@Service
@RequiredArgsConstructor
class IdempotencyRequestService implements IdempotencyRequestManager {

    private static final String REQUEST_NOT_FOUND_MESSAGE =
            "Nie znaleziono żądania idempotentnego o ID: ";

    private final IdempotencyRequestRepository idempotencyRequestRepository;
    private final Clock clock;

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public IdempotencyRequestResult startOrderCreation(
            String idempotencyKey,
            String requestHash,
            Long userId
    ) {
        Objects.requireNonNull(userId, "User ID nie może być null");

        return start(
                IdempotencyOperation.ORDER_CREATION,
                idempotencyKey,
                requestHash,
                userId
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public IdempotencyRequestResult startGuestOrderCreation(
            String idempotencyKey,
            String requestHash
    ) {
        return start(
                IdempotencyOperation.GUEST_ORDER_CREATION,
                idempotencyKey,
                requestHash,
                null
        );
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public void markResourceCreated(
            Long requestId,
            Long resourceId,
            PaymentCheckoutRequest checkoutRequest
    ) {
        IdempotencyRequest request = getRequiredRequest(requestId);

        request.markResourceCreated(
                resourceId,
                checkoutRequest,
                clock
        );

        idempotencyRequestRepository.save(request);
    }

    @Override
    // stores the final result independently from the already committed order transaction.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void markCompleted(
            Long requestId,
            String checkoutUrl
    ) {
        IdempotencyRequest request = getRequiredRequest(requestId);

        request.markCompleted(checkoutUrl, clock);

        idempotencyRequestRepository.save(request);
    }

    private IdempotencyRequestResult start(
            IdempotencyOperation operation,
            String idempotencyKey,
            String requestHash,
            Long ownerUserId
    ) {
        return idempotencyRequestRepository
                .findByOperationAndIdempotencyKey(operation, idempotencyKey)
                .map(request -> resolveExisting(
                        request,
                        requestHash,
                        ownerUserId
                ))
                .orElseGet(() -> registerNew(
                        operation,
                        idempotencyKey,
                        requestHash,
                        ownerUserId
                ));
    }

    private IdempotencyRequestResult registerNew(
            IdempotencyOperation operation,
            String idempotencyKey,
            String requestHash,
            Long ownerUserId
    ) {
        IdempotencyRequest request = IdempotencyRequest.start(
                operation,
                idempotencyKey,
                requestHash,
                ownerUserId,
                clock
        );

        try {
            IdempotencyRequest savedRequest = idempotencyRequestRepository.saveAndFlush(request);

            return new IdempotencyRequestResult(
                    savedRequest.getIdIdempotencyRequest(),
                    IdempotencyResolution.NEW,
                    null,
                    null,
                    null
            );
        } catch (DataIntegrityViolationException exception) {
            throw new IdempotencyRequestCollisionException(
                    "Idempotency-Key został równocześnie użyty przez inne żądanie",
                    exception
            );
        }
    }

    private IdempotencyRequestResult resolveExisting(
            IdempotencyRequest request,
            String requestHash,
            Long ownerUserId
    ) {
        if (!request.hasRequestHash(requestHash)) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key został już użyty dla innych danych żądania"
            );
        }

        if (!request.belongsTo(ownerUserId)) {
            throw new IdempotencyConflictException(
                    "Idempotency-Key został już użyty przez innego użytkownika"
            );
        }

        return switch (request.getStatus()) {
            case PROCESSING -> new IdempotencyRequestResult(
                    request.getIdIdempotencyRequest(),
                    IdempotencyResolution.IN_PROGRESS,
                    null,
                    null,
                    null
            );

            case RESOURCE_CREATED -> new IdempotencyRequestResult(
                    request.getIdIdempotencyRequest(),
                    IdempotencyResolution.RESOURCE_CREATED,
                    request.getResourceId(),
                    request.toCheckoutRequest(),
                    null
            );

            case COMPLETED -> new IdempotencyRequestResult(
                    request.getIdIdempotencyRequest(),
                    IdempotencyResolution.COMPLETED,
                    request.getResourceId(),
                    request.toCheckoutRequest(),
                    request.getCheckoutUrl()
            );
        };
    }

    private IdempotencyRequest getRequiredRequest(Long requestId) {
        Objects.requireNonNull(
                requestId,
                "Idempotency request ID nie może być null"
        );

        return idempotencyRequestRepository.findById(requestId)
                .orElseThrow(() -> new NoSuchElementException(
                        REQUEST_NOT_FOUND_MESSAGE + requestId
                ));
    }
}
