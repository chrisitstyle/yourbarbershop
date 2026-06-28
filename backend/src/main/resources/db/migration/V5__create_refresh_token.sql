CREATE TABLE IF NOT EXISTS `refresh_token` (
   `id_refresh_token` BIGINT NOT NULL AUTO_INCREMENT,
    `token_hash` VARCHAR(64) NOT NULL,
    `id_user` BIGINT NOT NULL,
    `expires_at` DATETIME(6) NOT NULL,
    `revoked_at` DATETIME(6) DEFAULT NULL,
    `created_at` DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    `replaced_by_token_hash` VARCHAR(64) DEFAULT NULL,
    `user_agent` VARCHAR(512) DEFAULT NULL,
    `ip_address` VARCHAR(64) DEFAULT NULL,

    PRIMARY KEY (`id_refresh_token`),
    UNIQUE KEY `uk_refresh_token_hash` (`token_hash`),
    KEY `idx_refresh_token_user` (`id_user`),
    KEY `idx_refresh_token_expires_at` (`expires_at`),

    CONSTRAINT `fk_refresh_token_user`
    FOREIGN KEY (`id_user`)
    REFERENCES `user` (`id_user`)
    ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;