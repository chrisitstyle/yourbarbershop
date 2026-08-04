DELETE FROM idempotency_request
WHERE idempotency_key IN (
  'guest-order-idempotency-replay-test-key',
  'guest-order-idempotency-conflict-test-key'
    );

DELETE FROM guest_order
WHERE email = 'guest.idempotency@example.com';

DELETE FROM appointment_slot
WHERE visit_date IN (
 '2031-01-16 12:00:00',
 '2031-01-16 13:00:00'
    );