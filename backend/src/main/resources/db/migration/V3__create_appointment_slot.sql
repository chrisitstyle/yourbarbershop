CREATE TABLE IF NOT EXISTS `appointment_slot` (
`id_appointment_slot` bigint NOT NULL AUTO_INCREMENT,
`visit_date` datetime NOT NULL,
PRIMARY KEY (`id_appointment_slot`),
UNIQUE KEY `uq_appointment_slot_visit_date` (`visit_date`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
