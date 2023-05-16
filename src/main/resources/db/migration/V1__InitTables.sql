CREATE TABLE address
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    deleted_at       datetime NULL,
    deleted          BIT(1) DEFAULT 0 NOT NULL,
    street           VARCHAR(100)     NOT NULL,
    apartment_number VARCHAR(10) NULL,
    city             VARCHAR(50)      NOT NULL,
    state            VARCHAR(20) NULL,
    zip_code         VARCHAR(10)      NOT NULL,
    country          VARCHAR(50)      NOT NULL,
    CONSTRAINT pk_address PRIMARY KEY (id)
);

CREATE TABLE business
(
    id                BIGINT AUTO_INCREMENT NOT NULL,
    created_at        datetime NULL,
    updated_at        datetime NULL,
    deleted_at        datetime NULL,
    deleted           BIT(1) DEFAULT 0 NOT NULL,
    business_name     VARCHAR(255)     NOT NULL,
    register_code     INT              NOT NULL,
    business_kmkr     VARCHAR(255)     NOT NULL,
    representative_id BIGINT           NOT NULL,
    CONSTRAINT pk_business PRIMARY KEY (id)
);

CREATE TABLE category
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    deleted_at datetime NULL,
    deleted    BIT(1) DEFAULT 0 NOT NULL,
    name       VARCHAR(32)      NOT NULL,
    CONSTRAINT pk_category PRIMARY KEY (id)
);

CREATE TABLE certificate
(
    id               BINARY(16) NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    deleted_at       datetime NULL,
    deleted          BIT(1) DEFAULT 0 NOT NULL,
    greeting         VARCHAR(120)     NOT NULL,
    greeting_text    TEXT             NOT NULL,
    value            DOUBLE           NOT NULL,
    remaining_value  DOUBLE NULL,
    valid_until      date             NOT NULL,
    created_by_admin BIT(1)           NOT NULL,
    user_holder      BIGINT           NOT NULL,
    active           BIT(1)           NOT NULL,
    user_sender      BIGINT NULL,
    payment_id       BINARY(16) NOT NULL,
    CONSTRAINT pk_certificate PRIMARY KEY (id)
);

CREATE TABLE file
(
    id            BINARY(16) NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    deleted_at    datetime NULL,
    deleted       BIT(1) DEFAULT 0 NOT NULL,
    file_type     VARCHAR(20)      NOT NULL,
    file_name     VARCHAR(50)      NOT NULL,
    restaurant_id BIGINT NULL,
    CONSTRAINT pk_file PRIMARY KEY (id)
);

CREATE TABLE login
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    deleted_at datetime NULL,
    deleted    BIT(1) DEFAULT 0 NOT NULL,
    ip         VARCHAR(15)      NOT NULL,
    user_agent TEXT             NOT NULL,
    user_id    BIGINT           NOT NULL,
    CONSTRAINT pk_login PRIMARY KEY (id)
);

CREATE TABLE payment
(
    id                 BINARY(16) NOT NULL,
    created_at         datetime NULL,
    updated_at         datetime NULL,
    deleted_at         datetime NULL,
    deleted            BIT(1) DEFAULT 0 NOT NULL,
    from_email         VARCHAR(120)     NOT NULL,
    from_fullname      VARCHAR(120)     NOT NULL,
    payment_status     INT              NOT NULL,
    merchant_reference VARCHAR(255)     NOT NULL,
    CONSTRAINT pk_payment PRIMARY KEY (id)
);

CREATE TABLE payment_customer
(
    id            BIGINT AUTO_INCREMENT NOT NULL,
    created_at    datetime NULL,
    updated_at    datetime NULL,
    deleted_at    datetime NULL,
    deleted       BIT(1) DEFAULT 0 NOT NULL,
    email         VARCHAR(255)     NOT NULL,
    greeting      VARCHAR(255)     NOT NULL,
    value         DOUBLE           NOT NULL,
    greeting_text VARCHAR(255)     NOT NULL,
    payment_id    BINARY(16) NOT NULL,
    CONSTRAINT pk_payment_customer PRIMARY KEY (id)
);

CREATE TABLE restaurant
(
    id               BIGINT AUTO_INCREMENT NOT NULL,
    created_at       datetime NULL,
    updated_at       datetime NULL,
    deleted_at       datetime NULL,
    deleted          BIT(1) DEFAULT 0 NOT NULL,
    name             VARCHAR(64)      NOT NULL,
    `description`    TEXT             NOT NULL,
    working_hours    VARCHAR(120)     NOT NULL,
    average_bill     INT              NOT NULL,
    address_id       BIGINT           NOT NULL,
    phone            VARCHAR(15)      NOT NULL,
    email            VARCHAR(120)     NOT NULL,
    active           BIT(1)           NOT NULL,
    restaurant_code  VARCHAR(6)       NOT NULL,
    maitsetuur_share INT              NOT NULL,
    manager_id       BIGINT NULL,
    photo_id         BINARY(16) NULL,
    contract_id      BINARY(16) NULL,
    CONSTRAINT pk_restaurant PRIMARY KEY (id)
);

CREATE TABLE restaurant_categories
(
    categories_id BIGINT NOT NULL,
    restaurant_id BIGINT NOT NULL
);

CREATE TABLE `role`
(
    id         BIGINT AUTO_INCREMENT NOT NULL,
    created_at timestamp DEFAULT NOW() NOT NULL,
    updated_at timestamp DEFAULT NOW() NOT NULL,
    role_name  VARCHAR(255)            NOT NULL,
    CONSTRAINT pk_role PRIMARY KEY (id)
);

CREATE TABLE transaction
(
    id         BINARY(16) NOT NULL,
    created_at datetime NULL,
    updated_at datetime NULL,
    deleted_at datetime NULL,
    deleted    BIT(1) DEFAULT 0 NOT NULL,
    value      DOUBLE(6, 2
) NOT NULL,
    waiter_id      BIGINT           NOT NULL,
    restaurant_id  BIGINT           NOT NULL,
    certificate_id BINARY(16)       NOT NULL,
    CONSTRAINT pk_transaction PRIMARY KEY (id)
);

CREATE TABLE user
(
    id              BIGINT AUTO_INCREMENT NOT NULL,
    created_at      datetime NULL,
    updated_at      datetime NULL,
    deleted_at      datetime NULL,
    deleted         BIT(1) DEFAULT 0 NOT NULL,
    password        VARCHAR(60) NULL,
    full_name       VARCHAR(60) NULL,
    email           VARCHAR(120)     NOT NULL,
    phone           VARCHAR(15) NULL,
    address_id      BIGINT NULL,
    personal_code   VARCHAR(11) NULL,
    activated       BIT(1)           NOT NULL,
    activation_code VARCHAR(36) NULL,
    restaurant_id   BIGINT NULL,
    CONSTRAINT pk_user PRIMARY KEY (id)
);

CREATE TABLE user_roles
(
    roles_id BIGINT NOT NULL,
    user_id  BIGINT NOT NULL
);

CREATE TABLE user_transactions
(
    user_id         BIGINT NOT NULL,
    transactions_id BINARY(16) NOT NULL
);

ALTER TABLE user_transactions
    ADD CONSTRAINT uc_user_transactions_transactions UNIQUE (transactions_id);

ALTER TABLE business
    ADD CONSTRAINT FK_BUSINESS_ON_REPRESENTATIVE FOREIGN KEY (representative_id) REFERENCES user (id);

ALTER TABLE certificate
    ADD CONSTRAINT FK_CERTIFICATE_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE certificate
    ADD CONSTRAINT FK_CERTIFICATE_ON_USER_HOLDER FOREIGN KEY (user_holder) REFERENCES user (id);

ALTER TABLE certificate
    ADD CONSTRAINT FK_CERTIFICATE_ON_USER_SENDER FOREIGN KEY (user_sender) REFERENCES user (id);

ALTER TABLE file
    ADD CONSTRAINT FK_FILE_ON_RESTAURANT FOREIGN KEY (restaurant_id) REFERENCES restaurant (id);

ALTER TABLE login
    ADD CONSTRAINT FK_LOGIN_ON_USER FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE payment_customer
    ADD CONSTRAINT FK_PAYMENT_CUSTOMER_ON_PAYMENT FOREIGN KEY (payment_id) REFERENCES payment (id);

ALTER TABLE restaurant
    ADD CONSTRAINT FK_RESTAURANT_ON_ADDRESS FOREIGN KEY (address_id) REFERENCES address (id);

ALTER TABLE restaurant
    ADD CONSTRAINT FK_RESTAURANT_ON_CONTRACT FOREIGN KEY (contract_id) REFERENCES file (id);

ALTER TABLE restaurant
    ADD CONSTRAINT FK_RESTAURANT_ON_MANAGER FOREIGN KEY (manager_id) REFERENCES user (id);

ALTER TABLE restaurant
    ADD CONSTRAINT FK_RESTAURANT_ON_PHOTO FOREIGN KEY (photo_id) REFERENCES file (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_CERTIFICATE FOREIGN KEY (certificate_id) REFERENCES certificate (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_RESTAURANT FOREIGN KEY (restaurant_id) REFERENCES restaurant (id);

ALTER TABLE transaction
    ADD CONSTRAINT FK_TRANSACTION_ON_WAITER FOREIGN KEY (waiter_id) REFERENCES user (id);

ALTER TABLE user
    ADD CONSTRAINT FK_USER_ON_ADDRESS FOREIGN KEY (address_id) REFERENCES address (id);

ALTER TABLE user
    ADD CONSTRAINT FK_USER_ON_RESTAURANT FOREIGN KEY (restaurant_id) REFERENCES restaurant (id);

ALTER TABLE restaurant_categories
    ADD CONSTRAINT fk_rescat_on_category FOREIGN KEY (categories_id) REFERENCES category (id);

ALTER TABLE restaurant_categories
    ADD CONSTRAINT fk_rescat_on_restaurant FOREIGN KEY (restaurant_id) REFERENCES restaurant (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_role FOREIGN KEY (roles_id) REFERENCES `role` (id);

ALTER TABLE user_roles
    ADD CONSTRAINT fk_userol_on_user FOREIGN KEY (user_id) REFERENCES user (id);

ALTER TABLE user_transactions
    ADD CONSTRAINT fk_usetra_on_transaction FOREIGN KEY (transactions_id) REFERENCES transaction (id);

ALTER TABLE user_transactions
    ADD CONSTRAINT fk_usetra_on_user FOREIGN KEY (user_id) REFERENCES user (id);

INSERT INTO role (role_name) VALUES
                                 ('ROLE_CUSTOMER'),
                                 ("ROLE_ADMIN"),
                                 ("ROLE_MANAGER"),
                                 ("ROLE_WAITER"),
                                 ("ROLE_ACCOUNTANT");