ALTER TABLE `user_order`
    ADD COLUMN `payment_method` varchar(50) NOT NULL DEFAULT 'GOTOWKA',
    ADD COLUMN `payment_status` varchar(50) NOT NULL DEFAULT 'OCZEKUJE_NA_PLATNOSC',
    ADD COLUMN `stripe_checkout_session_id` varchar(255) DEFAULT NULL,
    ADD COLUMN `stripe_payment_intent_id` varchar(255) DEFAULT NULL,
    ADD COLUMN `payment_amount` decimal(10,2) DEFAULT NULL,
    ADD COLUMN `payment_currency` varchar(3) DEFAULT 'PLN';

ALTER TABLE `guest_order`
    ADD COLUMN `payment_method` varchar(50) NOT NULL DEFAULT 'GOTOWKA',
    ADD COLUMN `payment_status` varchar(50) NOT NULL DEFAULT 'OCZEKUJE_NA_PLATNOSC',
    ADD COLUMN `stripe_checkout_session_id` varchar(255) DEFAULT NULL,
    ADD COLUMN `stripe_payment_intent_id` varchar(255) DEFAULT NULL,
    ADD COLUMN `payment_amount` decimal(10,2) DEFAULT NULL,
    ADD COLUMN `payment_currency` varchar(3) DEFAULT 'PLN';

CREATE UNIQUE INDEX `idx_user_order_stripe_checkout_session_id`
    ON `user_order` (`stripe_checkout_session_id`);

CREATE INDEX `idx_user_order_stripe_payment_intent_id`
    ON `user_order` (`stripe_payment_intent_id`);

CREATE UNIQUE INDEX `idx_guest_order_stripe_checkout_session_id`
    ON `guest_order` (`stripe_checkout_session_id`);

CREATE INDEX `idx_guest_order_stripe_payment_intent_id`
    ON `guest_order` (`stripe_payment_intent_id`);