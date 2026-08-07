SET FOREIGN_KEY_CHECKS = 0;

TRUNCATE TABLE `idempotency_request`;
TRUNCATE TABLE `payment`;
TRUNCATE TABLE `audit_logs`;
TRUNCATE TABLE `appointment_slot`;
TRUNCATE TABLE `password_reset_token`;
TRUNCATE TABLE `refresh_token`;
TRUNCATE TABLE `guest_order`;
TRUNCATE TABLE `user_order`;
TRUNCATE TABLE `user`;
TRUNCATE TABLE `offer`;

SET FOREIGN_KEY_CHECKS = 1;