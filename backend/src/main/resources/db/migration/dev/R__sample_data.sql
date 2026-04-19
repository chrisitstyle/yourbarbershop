-- INSERTS (SEED DATA)

INSERT INTO `offer` (`kind`,`cost`) VALUES
('dreadlocks',25.50),
('Strzyżenie brody + gorący ręcznik', 45.00),
('Strzyżenie męskie klasyczne', 50.00),
('Combo (Włosy + Broda)', 85.00),
('Strzyżenie długich włosów', 70.00),
('Cover siwizny (Włosy)', 60.00),
('Cover siwizny (Broda)', 40.00),
('Golenie głowy brzytwą', 55.00),
('Golenie twarzy brzytwą', 50.00),
('Regulacja brwi nitką', 20.00),
('Pielęgnacja twarzy (Peeling + Maska)', 40.00),
('Strzyżenie dziecięce (do lat 12)', 40.00),
('Woskowanie uszu i nosa', 15.00),
('Modelowanie włosów (Stylizacja)', 30.00),
('Przedłużanie brody (zagęszczanie)', 120.00),
('Pakiet VIP (Komplet usług + drink)', 150.00);

-- Insert for sample admin and user
INSERT INTO `user` (`firstname`, `lastname`, `email`, `password`, `role`) VALUES
( 'TestAdmin', 'TestAdmin', 'admin@test.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'ADMIN'),
('John', 'Doe', 'johndoe@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER');
/* password for both  is "test1234"*/


-- USERS
INSERT INTO `user` (`firstname`, `lastname`, `email`, `password`, `role`) VALUES
('Liam', 'Smith', 'liam.smith@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Noah', 'Johnson', 'noah.j@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Oliver', 'Williams', 'oliver.w@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('James', 'Brown', 'james.b@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('William', 'Jones', 'will.jones@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Benjamin', 'Garcia', 'ben.garcia@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Lucas', 'Miller', 'lucas.m@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Henry', 'Davis', 'henry.d@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Alexander', 'Rodriguez', 'alex.r@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Michael', 'Martinez', 'mike.m@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Ethan', 'Hernandez', 'ethan.h@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Daniel', 'Lopez', 'dan.lopez@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Jacob', 'Gonzalez', 'jacob.g@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Logan', 'Wilson', 'logan.w@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER'),
('Jackson', 'Anderson', 'jackson.a@example.com', '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi', 'USER');


-- ORDERS FOR SPECIFIC USER (id_user=2)

INSERT INTO `user_order` (`id_user`, `id_offer`, `order_date`, `visit_date`, `status`) VALUES
(2, 1,  '2026-04-01 09:00:00', '2026-04-05 10:00:00', 'ZREALIZOWANE'),
(2, 3,  '2026-04-03 14:30:00', '2026-04-07 12:00:00', 'ZREALIZOWANE'),
(2, 5,  '2026-04-05 11:00:00', '2026-04-10 15:30:00', 'ANULOWANE'),
(2, 2,  '2026-04-07 16:45:00', '2026-04-12 11:00:00', 'ZREALIZOWANE'),
(2, 8,  '2026-04-10 10:15:00', '2026-04-15 09:00:00', 'ZREALIZOWANE'),
(2, 12, '2026-04-12 13:00:00', '2026-04-18 14:00:00', 'NOWE'),
(2, 4,  '2026-04-14 08:30:00', '2026-04-20 16:00:00', 'NOWE'),
(2, 15, '2026-04-15 17:00:00', '2026-04-22 10:00:00', 'NOWE'),
(2, 6,  '2026-04-16 12:20:00', '2026-04-25 13:00:00', 'NOWE'),
(2, 10, '2026-04-17 09:10:00', '2026-04-28 15:30:00', 'NOWE'),
(2, 7,  '2026-04-18 14:00:00', '2026-04-29 11:00:00', 'NOWE'),
(2, 1,  '2026-04-18 20:00:00', '2026-04-30 17:00:00', 'NOWE');

-- SIGNED UP USERS' ORDERS
INSERT INTO `user_order` (`id_user`, `id_offer`, `order_date`, `visit_date`, `status`) VALUES
(3, 2,  '2026-04-10 10:00:00', '2026-04-20 12:00:00', 'ZREALIZOWANE'),
(4, 3,  '2026-04-11 11:30:00', '2026-04-21 14:00:00', 'NOWE'),
(5, 4,  '2026-04-12 09:00:00', '2026-04-22 10:30:00', 'NOWE'),
(6, 1,  '2026-04-13 15:45:00', '2026-04-23 16:00:00', 'NOWE'),
(7, 8,  '2026-04-14 12:00:00', '2026-04-24 11:00:00', 'ANULOWANE'),
(8, 5,  '2026-04-15 14:20:00', '2026-04-25 13:00:00', 'NOWE'),
(9, 6,  '2026-04-16 08:15:00', '2026-04-26 09:00:00', 'NOWE'),
(10, 7, '2026-04-17 19:00:00', '2026-04-27 17:30:00', 'NOWE'),
(11, 10, '2026-04-18 10:10:00', '2026-04-28 12:30:00', 'NOWE'),
(12, 11, '2026-04-19 13:05:00', '2026-04-29 15:00:00', 'NOWE'),
(13, 12, '2026-04-20 16:40:00', '2026-04-30 18:00:00', 'NOWE'),
(14, 15, '2026-04-21 11:20:00', '2026-05-01 10:00:00', 'NOWE'),
(15, 3,  '2026-04-22 14:55:00', '2026-05-02 12:30:00', 'NOWE'),
(16, 2,  '2026-04-23 17:30:00', '2026-05-03 14:00:00', 'NOWE'),
(17, 1,  '2026-04-24 09:45:00', '2026-05-04 16:30:00', 'NOWE');


-- GUEST ORDERS
INSERT INTO `guest_order` (`firstname`, `lastname`, `phonenumber`, `email`, `id_offer`, `order_date`, `visit_date`, `status`) VALUES
('Sophia', 'Smith', '555-0101', 'sophia.s@example.com', 3, '2026-04-10 08:00:00', '2026-04-20 09:00:00', 'ZREALIZOWANE'),
('Charlotte', 'Jones', '555-0102', 'char.j@example.com', 5, '2026-04-11 12:00:00', '2026-04-21 10:00:00', 'NOWE'),
('Amelia', 'Brown', '555-0103', 'amelia.b@example.com', 16, '2026-04-12 14:00:00', '2026-04-22 11:00:00', 'NOWE'),
('Evelyn', 'Miller', '555-0104', 'eve.miller@example.com', 4, '2026-04-13 16:00:00', '2026-04-23 12:00:00', 'NOWE'),
('Abigail', 'Davis', '555-0105', 'abi.d@example.com', 2, '2026-04-14 10:00:00', '2026-04-24 13:00:00', 'ANULOWANE'),
('Mia', 'Garcia', '555-0106', 'mia.g@example.com', 7, '2026-04-15 15:00:00', '2026-04-25 14:00:00', 'NOWE'),
('Elizabeth', 'Rodriguez', '555-0107', 'liz.r@example.com', 10, '2026-04-16 11:00:00', '2026-04-26 15:00:00', 'NOWE'),
('Sofia', 'Martinez', '555-0108', 'sofia.m@example.com', 13, '2026-04-17 18:00:00', '2026-04-27 16:00:00', 'NOWE'),
('Avery', 'Hernandez', '555-0109', 'avery.h@example.com', 15, '2026-04-18 09:00:00', '2026-04-28 17:00:00', 'NOWE'),
('Scarlett', 'Lopez', '555-0110', 'scar.l@example.com', 8, '2026-04-19 20:00:00', '2026-04-29 08:00:00', 'NOWE'),
('Emily', 'Gonzalez', '555-0111', 'emily.g@example.com', 1, '2026-04-20 13:00:00', '2026-04-30 09:30:00', 'NOWE'),
('Aria', 'Wilson', '555-0112', 'aria.w@example.com', 12, '2026-04-21 07:00:00', '2026-05-01 10:30:00', 'NOWE'),
('Penelope', 'Anderson', '555-0113', 'penn.a@example.com', 11, '2026-04-22 12:00:00', '2026-05-02 11:30:00', 'NOWE'),
('Chloe', 'Thomas', '555-0114', 'chloe.t@example.com', 14, '2026-04-23 14:30:00', '2026-05-03 12:30:00', 'NOWE'),
('Layla', 'Taylor', '555-0115', 'layla.t@example.com', 6, '2026-04-24 16:30:00', '2026-05-04 13:30:00', 'NOWE');