package pl.barbershopproject.barbershop.order;

import pl.barbershopproject.barbershop.order.dto.OrderCreationDTO;
import pl.barbershopproject.barbershop.user.User;

/**
 * Persists an order, reserves its appointment slot and creates its payment
 * inside a single database transaction.
 */
interface OrderCreationTransaction {

    /**
     * Executes the transactional part of order creation.
     *
     * @param orderCreationDTO order creation data
     * @param user authenticated user creating the order
     * @return data required after the transaction has committed
     */
    OrderCreationTransactionResult create(OrderCreationDTO orderCreationDTO,User user);
}
