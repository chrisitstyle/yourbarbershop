-- --------------------------------------------------------
-- Host:                         127.0.0.1
-- Server version:               8.1.0 - MySQL Community Server - GPL
-- Server OS:                    Win64
-- HeidiSQL Version:             12.7.0.6850
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

-- Dumping structure for table barbershop-with-roles.guest_order
CREATE TABLE IF NOT EXISTS `guest_order` (
  `id_guest_order` int NOT NULL AUTO_INCREMENT,
  `firstname` varchar(45) NOT NULL,
  `lastname` varchar(45) NOT NULL,
  `phonenumber` varchar(45) NOT NULL,
  `email` varchar(45) NOT NULL,
  `id_offer` int DEFAULT NULL,
  `order_date` datetime NOT NULL,
  `visit_date` datetime NOT NULL,
  `status` char(15) NOT NULL,
  PRIMARY KEY (`id_guest_order`),
  KEY `fk_guest_order_offer_idx` (`id_offer`),
  CONSTRAINT `fk_guest_order_offer` FOREIGN KEY (`id_offer`) REFERENCES `offer` (`id_offer`) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Dumping structure for table barbershop-with-roles.offer
CREATE TABLE IF NOT EXISTS `offer` (
  `id_offer` int NOT NULL AUTO_INCREMENT,
  `kind` varchar(45) NOT NULL,
  `cost` decimal(5,2) NOT NULL,
  PRIMARY KEY (`id_offer`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

INSERT INTO `offer` (`id_offer`,`kind`,`cost`) VALUES (1,'dreadlocks',25.50);

-- Dumping structure for table barbershop-with-roles.password_reset_token
CREATE TABLE IF NOT EXISTS `password_reset_token` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `token` varchar(255) NOT NULL,
  `id_user` int NOT NULL,
  `expiry_date` timestamp NOT NULL,
  PRIMARY KEY (`id`),
  KEY `password_reset_token_ibfk_1` (`id_user`),
  CONSTRAINT `password_reset_token_ibfk_1` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


-- Dumping structure for table barbershop-with-roles.user
CREATE TABLE IF NOT EXISTS `user` (
  `id_user` int NOT NULL AUTO_INCREMENT,
  `firstname` varchar(200) NOT NULL,
  `lastname` varchar(200) NOT NULL,
  `e-mail` varchar(200) NOT NULL,
  `password` varchar(250) NOT NULL,
  `role` varchar(50) NOT NULL,
  PRIMARY KEY (`id_user`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- Insert for sample admin and user
INSERT INTO `user` (`id_user`, `firstname`, `lastname`, `e-mail`, `password`, `role`) VALUES (73, 'TestAdmin', 'TestAdmin', 'admin@test.com', '$2a$10$50CMRbm4BFvmQfOcukUjCeFrZIZWGiLr9RYO6ON5zI.xv/a3ltS2S', 'ADMIN');
INSERT INTO `user` (`id_user`, `firstname`, `lastname`, `e-mail`, `password`, `role`) VALUES (74, 'John', 'Doe', 'johndoe@example.com', '$2a$10$HjId9JiXQUUmmQEwcoJNG.DynNWrKFFvv0kP3WxdlTd6/3vXDgOlG', 'USER');
/* password for both  is "test123"*/


-- Dumping structure for table barbershop-with-roles.user_order
CREATE TABLE IF NOT EXISTS `user_order` (
  `id_order` int NOT NULL AUTO_INCREMENT,
  `id_user` int DEFAULT NULL,
  `id_offer` int DEFAULT NULL,
  `order_date` datetime NOT NULL,
  `visit_date` datetime NOT NULL,
  `status` char(15) NOT NULL,
  PRIMARY KEY (`id_order`),
  KEY `fk_customer_order_user` (`id_user`),
  KEY `fk_customer_order_offer` (`id_offer`),
  CONSTRAINT `fk_customer_order_offer` FOREIGN KEY (`id_offer`) REFERENCES `offer` (`id_offer`) ON DELETE SET NULL,
  CONSTRAINT `fk_customer_order_user` FOREIGN KEY (`id_user`) REFERENCES `user` (`id_user`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;


/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
