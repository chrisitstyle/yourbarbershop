package pl.barbershopproject.barbershop.orderupdate;

import pl.barbershopproject.barbershop.utils.OrderStatus;

/**
 * Contains the status transition resolved during preparation of an order update.
 *
 * @param currentStatus status before the update
 * @param targetStatus  status that should be applied after validation
 */
public record OrderUpdateResult(
        OrderStatus currentStatus,
        OrderStatus targetStatus
) { }
