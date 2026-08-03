package pl.barbershopproject.barbershop.guestorder;

import pl.barbershopproject.barbershop.guestorder.dto.GuestOrderCreationDTO;

/**
 * Persists a guest order, reserves its appointment slot and creates its payment
 * inside a single database transaction.
 */
interface GuestOrderCreationTransaction {

    /**
     * Executes the transactional part of guest-order creation.
     *
     * @param guestOrderCreationDTO guest-order creation data
     * @return data required after the transaction has committed
     */
    GuestOrderCreationTransactionResult create(GuestOrderCreationDTO guestOrderCreationDTO);
}
