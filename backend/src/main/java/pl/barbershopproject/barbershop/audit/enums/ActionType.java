package pl.barbershopproject.barbershop.audit.enums;

/**
 * enum representing all supported audit action types across the system
 */
public enum ActionType {
    // order actions
    ORDER_CREATED,
    ORDER_UPDATED,
    ORDER_DELETED,

    // guest order actions
    GUEST_ORDER_CREATED,
    GUEST_ORDER_UPDATED,
    GUEST_ORDER_DELETED,

    // offer actions
    OFFER_CREATED,
    OFFER_UPDATED,
    OFFER_DELETED,

    // user actions
    USER_CREATED,
    USER_UPDATED,
    USER_DELETED
}
