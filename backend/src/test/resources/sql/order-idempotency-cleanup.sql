DELETE FROM idempotency_request
WHERE idempotency_key IN (
  'order-idempotency-replay-test-key',
  'order-idempotency-request-conflict-test-key',
  'order-idempotency-owner-conflict-test-key'
    );

DELETE user_order
FROM user_order
INNER JOIN user
    ON user.id_user = user_order.id_user
WHERE user.email IN (
    'johndoe@example.com',
    'second.user.idempotency@example.com'
)
AND user_order.visit_date IN (
    '2032-01-16 12:00:00',
    '2032-01-17 12:00:00',
    '2032-01-17 13:00:00',
    '2032-01-18 12:00:00'
);

DELETE FROM appointment_slot
WHERE visit_date IN (
 '2032-01-16 12:00:00',
 '2032-01-17 12:00:00',
 '2032-01-17 13:00:00',
 '2032-01-18 12:00:00'
    );

DELETE FROM user
WHERE email = 'second.user.idempotency@example.com';