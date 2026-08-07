ALTER TABLE payment
    ADD COLUMN stripe_checkout_idempotency_key VARCHAR(64) DEFAULT NULL,
    ADD UNIQUE KEY uk_payment_stripe_checkout_idempotency_key
        (stripe_checkout_idempotency_key);

UPDATE payment
SET stripe_checkout_idempotency_key = UUID()
WHERE payment_method = 'KARTA_ONLINE'
  AND stripe_checkout_idempotency_key IS NULL;