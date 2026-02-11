CREATE TABLE "user" (
                        ID_USER BIGINT AUTO_INCREMENT PRIMARY KEY,
                        FIRSTNAME VARCHAR(200) NOT NULL,
                        LASTNAME VARCHAR(200) NOT NULL,
                        EMAIL VARCHAR(200) NOT NULL,
                        PASSWORD VARCHAR(250) NOT NULL,
                        ROLE VARCHAR(50) NOT NULL
);

CREATE TABLE OFFER (
                       ID_OFFER BIGINT AUTO_INCREMENT PRIMARY KEY,
                       KIND VARCHAR(45) NOT NULL,
                       COST DECIMAL(5,2) NOT NULL
);

CREATE TABLE USER_ORDER (
                            ID_ORDER BIGINT AUTO_INCREMENT PRIMARY KEY,
                            ID_USER BIGINT,
                            ID_OFFER BIGINT,
                            ORDER_DATE TIMESTAMP NOT NULL,
                            VISIT_DATE TIMESTAMP NOT NULL,
                            STATUS VARCHAR(15) NOT NULL,
                            CONSTRAINT FK_CUSTOMER_ORDER_USER FOREIGN KEY (ID_USER) REFERENCES "user"(ID_USER) ON DELETE CASCADE,
                            CONSTRAINT FK_CUSTOMER_ORDER_OFFER FOREIGN KEY (ID_OFFER) REFERENCES OFFER(ID_OFFER) ON DELETE SET NULL
);

CREATE TABLE GUEST_ORDER (
                             ID_GUEST_ORDER BIGINT AUTO_INCREMENT PRIMARY KEY,
                             FIRSTNAME VARCHAR(45) NOT NULL,
                             LASTNAME VARCHAR(45) NOT NULL,
                             PHONENUMBER VARCHAR(45) NOT NULL,
                             EMAIL VARCHAR(45) NOT NULL,
                             ID_OFFER BIGINT,
                             ORDER_DATE TIMESTAMP NOT NULL,
                             VISIT_DATE TIMESTAMP NOT NULL,
                             STATUS VARCHAR(15) NOT NULL,
                             CONSTRAINT FK_GUEST_ORDER_OFFER FOREIGN KEY (ID_OFFER) REFERENCES OFFER(ID_OFFER) ON DELETE SET NULL
);

CREATE TABLE PASSWORD_RESET_TOKEN (
                                      ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      TOKEN VARCHAR(255) NOT NULL,
                                      ID_USER BIGINT NOT NULL,
                                      EXPIRY_DATE TIMESTAMP NOT NULL,
                                      CONSTRAINT PASSWORD_RESET_TOKEN_IBFK_1 FOREIGN KEY (ID_USER) REFERENCES "user"(ID_USER)
);

INSERT INTO OFFER (KIND, COST) VALUES ('dreadlocks', 25.50);
INSERT INTO "user" (FIRSTNAME, LASTNAME, EMAIL, PASSWORD, ROLE) VALUES ('TestAdmin', 'TestAdmin', 'admin@test.com', 'test1234', 'ADMIN');
INSERT INTO "user" (FIRSTNAME, LASTNAME, EMAIL, PASSWORD, ROLE) VALUES ('John', 'Doe', 'johndoe@example.com', 'test1234', 'USER');