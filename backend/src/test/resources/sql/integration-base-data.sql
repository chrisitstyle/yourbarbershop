-- Base offer required by integration tests.
INSERT INTO `offer` (`kind`, `cost`)
SELECT 'dreadlocks', 25.50
    WHERE NOT EXISTS (
    SELECT 1
    FROM `offer`
    WHERE `kind` = 'dreadlocks'
      AND `cost` = 25.50);

-- Password for integration-test users: test1234
INSERT IGNORE INTO `user` (`firstname`,`lastname`,`email`,`password`,`role`)
VALUES (
    'TestAdmin',
    'TestAdmin',
    'admin@test.com',
    '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi',
    'ADMIN');

INSERT IGNORE INTO `user` (`firstname`,`lastname`,`email`,`password`,`role`)
VALUES (
    'John',
    'Doe',
    'johndoe@example.com',
    '$2a$10$3tg0XIJRF9oeMv.gqElnR.XmmZD4W7FJR.3R8Ms1GZf4T.H694sJi',
    'USER');