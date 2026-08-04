package pl.barbershopproject.barbershop.idempotency;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * Provides access to stored idempotency requests.
 *
 * <p>The repository is used to detect repeated requests by operation
 * and Idempotency-Key before creating another order.</p>
 */
interface IdempotencyRequestRepository extends JpaRepository<IdempotencyRequest, Long> {

    /**
     * Finds the original request for a specific API operation and client key.
     */
    Optional<IdempotencyRequest> findByOperationAndIdempotencyKey(
            IdempotencyOperation operation,
            String idempotencyKey
    );
}
