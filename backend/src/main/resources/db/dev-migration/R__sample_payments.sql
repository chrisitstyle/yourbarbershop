-- DEVELOPMENT SAMPLE PAYMENTS
--
-- Creates missing payment records for sample registered and guest orders.
-- Sample orders use an on-site cash payment, so no Stripe identifiers are required.
-- The NOT EXISTS predicates make this repeatable migration idempotent.

INSERT INTO `payment` (
    `id_order`,
    `id_guest_order`,
    `payment_method`,
    `payment_status`,
    `stripe_checkout_session_id`,
    `stripe_payment_intent_id`,
    `amount`,
    `currency`,
    `created_at`,
    `paid_at`
)
SELECT
    user_order.`id_order`,
    NULL,
    'GOTOWKA',
    'NIE_WYMAGANA',
    NULL,
    NULL,
    user_order.`booked_offer_price`,
    'PLN',
    user_order.`order_date`,
    NULL
FROM `user_order`
WHERE NOT EXISTS (
    SELECT 1
    FROM `payment`
    WHERE `payment`.`id_order` = user_order.`id_order`
);

INSERT INTO `payment` (
    `id_order`,
    `id_guest_order`,
    `payment_method`,
    `payment_status`,
    `stripe_checkout_session_id`,
    `stripe_payment_intent_id`,
    `amount`,
    `currency`,
    `created_at`,
    `paid_at`
)
SELECT
    NULL,
    guest_order.`id_guest_order`,
    'GOTOWKA',
    'NIE_WYMAGANA',
    NULL,
    NULL,
    guest_order.`booked_offer_price`,
    'PLN',
    guest_order.`order_date`,
    NULL
FROM `guest_order`
WHERE NOT EXISTS (
    SELECT 1
    FROM `payment`
    WHERE `payment`.`id_guest_order` = guest_order.`id_guest_order`
);