package pl.barbershopproject.barbershop.order;

import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.user.User;

/**
 * Resolves an {@code Idempotency-Key} and, when necessary, persists an order,
 * reserves its appointment slot and creates its payment within one transaction.
 */
interface OrderCreationTransaction {

    /**
     * Resolves an existing idempotency request or creates a new authenticated order.
     *
     * @param orderCreationDTO order creation data
     * @param user authenticated user creating the order
     * @param idempotencyKey client-generated key identifying the operation
     * @param requestHash SHA-256 hash of the request data
     * @return the current result of the idempotent order operation
     */
    OrderCreationTransactionResult create(
            OrderCreationDTO orderCreationDTO,
            User user,
            String idempotencyKey,
            String requestHash
    );
}
