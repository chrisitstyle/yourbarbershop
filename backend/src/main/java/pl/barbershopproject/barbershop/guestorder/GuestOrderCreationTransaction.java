package pl.barbershopproject.barbershop.guestorder;

import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;

/**
 * Resolves an {@code Idempotency-Key} and, when necessary, persists a guest order,
 * reserves its appointment slot and creates its payment within one transaction.
 */
interface GuestOrderCreationTransaction {

    /**
     * Resolves an existing idempotency request or creates a new guest order.
     *
     * @param guestOrderCreationDTO guest order creation data
     * @param idempotencyKey client-generated key identifying the operation
     * @param requestHash SHA-256 hash of the request data
     * @return the current result of the idempotent guest order operation
     */
    GuestOrderCreationTransactionResult create(
            GuestOrderCreationDTO guestOrderCreationDTO,
            String idempotencyKey,
            String requestHash
    );
}
