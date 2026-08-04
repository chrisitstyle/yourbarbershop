DELETE FROM guest_order
WHERE email = 'guestjohn.security@example.com';

DELETE user_order
FROM user_order
INNER JOIN user
    ON user.id_user = user_order.id_user
WHERE user.email = 'johndoe@example.com'
  AND user_order.visit_date = '2030-01-16 12:00:00';

DELETE FROM appointment_slot
WHERE visit_date = '2030-01-16 12:00:00';

DELETE FROM offer
WHERE kind = 'Security Test Service';