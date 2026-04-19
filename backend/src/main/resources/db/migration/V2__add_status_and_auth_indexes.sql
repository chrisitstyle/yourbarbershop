-- Migration V2: Adding indexes for authentication and status filtering based on current service implementations

-- 1. Unique indexes for authentication and security (used during login and password reset)
CREATE UNIQUE INDEX `idx_user_email` ON `user` (`email`);
CREATE UNIQUE INDEX `idx_token` ON `password_reset_token` (`token`);

-- 2. Indexes for order statuses (actively used by getOrdersByStatus and getGuestOrdersByStatus methods)
CREATE INDEX `idx_user_order_status` ON `user_order` (`status`);
CREATE INDEX `idx_guest_order_status` ON `guest_order` (`status`);