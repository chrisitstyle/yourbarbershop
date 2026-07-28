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

CREATE TABLE PAYMENT (
                         ID_PAYMENT BIGINT AUTO_INCREMENT PRIMARY KEY,
                         ID_ORDER BIGINT,
                         ID_GUEST_ORDER BIGINT,

                         PAYMENT_METHOD VARCHAR(50) NOT NULL,
                         PAYMENT_STATUS VARCHAR(50) NOT NULL,

                         STRIPE_CHECKOUT_SESSION_ID VARCHAR(255),
                         STRIPE_PAYMENT_INTENT_ID VARCHAR(255),

                         AMOUNT DECIMAL(10,2) NOT NULL,
                         CURRENCY VARCHAR(3) NOT NULL DEFAULT 'PLN',

                         CREATED_AT TIMESTAMP NOT NULL,
                         PAID_AT TIMESTAMP,

                         CONSTRAINT UK_PAYMENT_ORDER UNIQUE (ID_ORDER),
                         CONSTRAINT UK_PAYMENT_GUEST_ORDER UNIQUE (ID_GUEST_ORDER),
                         CONSTRAINT UK_PAYMENT_STRIPE_CHECKOUT_SESSION_ID UNIQUE (STRIPE_CHECKOUT_SESSION_ID),

                         CONSTRAINT FK_PAYMENT_USER_ORDER
                             FOREIGN KEY (ID_ORDER) REFERENCES USER_ORDER(ID_ORDER)
                                 ON DELETE CASCADE,

                         CONSTRAINT FK_PAYMENT_GUEST_ORDER
                             FOREIGN KEY (ID_GUEST_ORDER) REFERENCES GUEST_ORDER(ID_GUEST_ORDER)
                                 ON DELETE CASCADE,

                         CONSTRAINT CHK_PAYMENT_TARGET
                             CHECK (
                                 (ID_ORDER IS NOT NULL AND ID_GUEST_ORDER IS NULL)
                                     OR
                                 (ID_ORDER IS NULL AND ID_GUEST_ORDER IS NOT NULL)
                                 )
);

CREATE TABLE PASSWORD_RESET_TOKEN (
                                      ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                                      TOKEN VARCHAR(255) NOT NULL,
                                      ID_USER BIGINT NOT NULL,
                                      EXPIRY_DATE TIMESTAMP NOT NULL,
                                      CONSTRAINT PASSWORD_RESET_TOKEN_IBFK_1 FOREIGN KEY (ID_USER) REFERENCES "user"(ID_USER)
);

CREATE TABLE AUDIT_LOGS (
id BIGINT AUTO_INCREMENT PRIMARY KEY,
timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
actor_email VARCHAR(255) NOT NULL,
action VARCHAR(100) NOT NULL,
entity_type VARCHAR(50) NOT NULL,
entity_id VARCHAR(100),
details JSON
);

INSERT INTO OFFER (KIND, COST) VALUES ('dreadlocks', 25.50);
INSERT INTO "user" (FIRSTNAME, LASTNAME, EMAIL, PASSWORD, ROLE) VALUES ('TestAdmin', 'TestAdmin', 'admin@test.com', 'test1234', 'ADMIN');
INSERT INTO "user" (FIRSTNAME, LASTNAME, EMAIL, PASSWORD, ROLE) VALUES ('John', 'Doe', 'johndoe@example.com', 'test1234', 'USER');