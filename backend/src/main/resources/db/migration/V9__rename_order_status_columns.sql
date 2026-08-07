-- renames order lifecycle columns to match the orderStatus entity property.
-- existing indexes created in V2 remain attached to the renamed columns.
ALTER TABLE `user_order`
    RENAME COLUMN `status` TO `order_status`;

ALTER TABLE `guest_order`
    RENAME COLUMN `status` TO `order_status`;