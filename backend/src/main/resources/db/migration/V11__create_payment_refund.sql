-- Stores refund history for payments processed through Stripe
CREATE TABLE payment_refund (
id_payment_refund BIGINT NOT NULL AUTO_INCREMENT,
id_payment BIGINT NOT NULL,

stripe_refund_id VARCHAR(255) UNIQUE,
idempotency_key VARCHAR(64) NOT NULL UNIQUE,

amount DECIMAL(12, 2) NOT NULL,
currency VARCHAR(3) NOT NULL,
refund_status VARCHAR(32) NOT NULL,

requested_at DATETIME(6) NOT NULL,
completed_at DATETIME(6),
failure_reason VARCHAR(1000),

version BIGINT NOT NULL DEFAULT 0,

PRIMARY KEY (id_payment_refund),

-- Prevents deleting payments that have associated refund records
CONSTRAINT fk_payment_refund_payment
    FOREIGN KEY (id_payment)
        REFERENCES payment (id_payment)
        ON DELETE RESTRICT
);

-- Improves performance when retrieving refund history for a payment
CREATE INDEX idx_payment_refund_payment
    ON payment_refund (id_payment);
