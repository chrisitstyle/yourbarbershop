CREATE TABLE `payment` (
    `id_payment` bigint NOT NULL AUTO_INCREMENT,
    `id_order` bigint DEFAULT NULL,
    `id_guest_order` bigint DEFAULT NULL,

    `payment_method` varchar(50) NOT NULL,
    `payment_status` varchar(50) NOT NULL,

    `stripe_checkout_session_id` varchar(255) DEFAULT NULL,
    `stripe_payment_intent_id` varchar(255) DEFAULT NULL,

    `amount` decimal(10,2) NOT NULL,
    `currency` varchar(3) NOT NULL DEFAULT 'PLN',

    `created_at` datetime(6) NOT NULL,
    `paid_at` datetime(6) DEFAULT NULL,

    PRIMARY KEY (`id_payment`),

    UNIQUE KEY `uk_payment_order` (`id_order`),
    UNIQUE KEY `uk_payment_guest_order` (`id_guest_order`),
    UNIQUE KEY `uk_payment_stripe_checkout_session_id` (`stripe_checkout_session_id`),

    KEY `idx_payment_stripe_payment_intent_id` (`stripe_payment_intent_id`),

    CONSTRAINT `fk_payment_user_order`
        FOREIGN KEY (`id_order`) REFERENCES `user_order` (`id_order`)
            ON DELETE CASCADE,

    CONSTRAINT `fk_payment_guest_order`
        FOREIGN KEY (`id_guest_order`) REFERENCES `guest_order` (`id_guest_order`)
            ON DELETE CASCADE,

    CONSTRAINT `chk_payment_target`
        CHECK (
            (`id_order` IS NOT NULL AND `id_guest_order` IS NULL)
                OR
            (`id_order` IS NULL AND `id_guest_order` IS NOT NULL)
        )
);
