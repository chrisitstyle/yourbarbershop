DELETE FROM idempotency_request
WHERE idempotency_key ='order-stripe-retry-idempotency-test-key';

DELETE user_order
FROM user_order
INNER JOIN user
    ON user.id_user = user_order.id_user
WHERE user.email = 'johndoe@example.com'
  AND user_order.visit_date = '2033-01-16 12:00:00';

DELETE FROM appointment_slot
WHERE visit_date = '2033-01-16 12:00:00';