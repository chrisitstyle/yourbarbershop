-- Stores order creation attempts so repeated requests with the same
-- Idempotency-Key return the original result instead of creating another order.

CREATE TABLE idempotency_request (
 id_idempotency_request BIGINT NOT NULL AUTO_INCREMENT,

-- Identifies the protected operation, for example:
-- ORDER_CREATION or GUEST_ORDER_CREATION.
 operation VARCHAR(50) NOT NULL,

-- Key provided by the client and reused when the same request is retried.
 idempotency_key VARCHAR(255) NOT NULL,

-- SHA-256 hash of the request data.
-- Prevents using the same key for a different request payload.
 request_hash CHAR(64) NOT NULL,

-- Identifies the authenticated user who created the request.
-- Remains NULL for guest orders.
 owner_user_id BIGINT DEFAULT NULL,

-- Current processing state:
-- PROCESSING, RESOURCE_CREATED or COMPLETED.
 status VARCHAR(30) NOT NULL,

-- ID of the created order or guest order, depending on the operation
 resource_id BIGINT DEFAULT NULL,

-- ID of the payment created for the order
 payment_id BIGINT DEFAULT NULL,

-- Payment data required to rebuild the original response
-- or resume Stripe Checkout creation after a failure.
 payment_method VARCHAR(50) DEFAULT NULL,
 payment_status VARCHAR(50) DEFAULT NULL,

 amount DECIMAL(10, 2) DEFAULT NULL,
 currency VARCHAR(3) DEFAULT NULL,
 product_name VARCHAR(255) DEFAULT NULL,

-- Stripe Checkout URL returned after successful session creation.
 checkout_url VARCHAR(2048) DEFAULT NULL,

-- Request creation and last update timestamps.
 created_at DATETIME(6) NOT NULL,
 updated_at DATETIME(6) NOT NULL,

-- Timestamp set after the complete operation has finished.
 completed_at DATETIME(6) DEFAULT NULL,

 PRIMARY KEY (id_idempotency_request),

-- A key can be used only once for a specific operation.
 UNIQUE KEY uk_idempotency_operation_key (operation,idempotency_key),

-- One payment can belong to only one idempotency request.
 UNIQUE KEY uk_idempotency_payment (payment_id),

-- One created order can belong to only one idempotency request.
 UNIQUE KEY uk_idempotency_resource (operation,resource_id),

 KEY idx_idempotency_created_at (created_at),

 KEY idx_idempotency_owner_user (owner_user_id),

     CONSTRAINT fk_idempotency_owner_user
         FOREIGN KEY (owner_user_id)
             REFERENCES user (id_user)
             ON DELETE SET NULL
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_0900_ai_ci;