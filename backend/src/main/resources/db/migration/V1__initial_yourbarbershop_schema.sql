-- Dumping structure for table barbershop-with-roles.offer
CREATE TABLE IF NOT EXISTS `offer` (
    `id_offer` bigint NOT NULL AUTO_INCREMENT,
    `kind` varchar(45) NOT NULL,
    `cost` decimal(5,2) NOT NULL,
    PRIMARY KEY (`id_offer`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping structure for table barbershop-with-roles.user
CREATE TABLE IF NOT EXISTS `user` (
    `id_user` bigint NOT NULL AUTO_INCREMENT,
    `firstname` varchar(200) NOT NULL,
    `lastname` varchar(200) NOT NULL,
    `email` varchar(200) NOT NULL,
    `password` varchar(250) NOT NULL,
    `role` varchar(50) NOT NULL,
    PRIMARY KEY (`id_user`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Dumping structure for table barbershop-with-roles.guest_order
CREATE TABLE IF NOT EXISTS `guest_order` (
    `id_guest_order` bigint NOT NULL AUTO_INCREMENT,
    `firstname` varchar(45) NOT NULL,
    `lastname` varchar(45) NOT NULL,
    `phonenumber` varchar(45) NOT NULL,
    `email` varchar(45) NOT NULL,
    `id_offer` bigint DEFAULT NULL,
    `order_date` datetime NOT NULL,
    `visit_date` datetime NOT NULL,
    `order_status` varchar(255) NOT NULL,
    PRIMARY KEY (`id_guest_order`),
    KEY `fk_guest_order_offer_idx` (`id_offer`),
    CONSTRAINT `fk_guest_order_offer` FOREIGN KEY (`id_offer`) REFERENCES `offer` (`id_offer`) ON DELETE SET NULL
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Dumping structure for table barbershop-with-roles.user_order
CREATE TABLE IF NOT EXISTS `user_order` (
    `id_order` bigint NOT NULL AUTO_INCREMENT,
    `id_user` bigint DEFAULT NULL,
    `id_offer` bigint DEFAULT NULL,
    `order_date` datetime NOT NULL,
    `visit_date` datetime NOT NULL,
    `order_status` varchar(255) NOT NULL,
    PRIMARY KEY (`id_order`),
    KEY `fk_customer_order_user` (`id_user`),
    KEY `fk_customer_order_offer` (`id_offer`),
    CONSTRAINT `fk_customer_order_offer` FOREIGN KEY (`id_offer`) REFERENCES `offer` (`id_offer`) ON DELETE SET NULL,
    CONSTRAINT `fk_customer_order_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Dumping structure for table barbershop-with-roles.password_reset_token
CREATE TABLE IF NOT EXISTS `password_reset_token` (
    `id` bigint NOT NULL AUTO_INCREMENT,
    `token` varchar(255) NOT NULL,
    `id_user` bigint NOT NULL,
    `expiry_date` timestamp NOT NULL,
    PRIMARY KEY (`id`),
    KEY `password_reset_token_ibfk_1` (`id_user`),
    CONSTRAINT `password_reset_token_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
