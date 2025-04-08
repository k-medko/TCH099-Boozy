-- SQLBook: Code

-- First drop all tables in the correct order (respecting foreign key constraints)
DROP TABLE IF EXISTS CommandProducts;
DROP TABLE IF EXISTS ShopProducts;
DROP TABLE IF EXISTS PromotionalOffer;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS Command;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Shop;
DROP TABLE IF EXISTS AddressLine;
DROP TABLE IF EXISTS UserAccount;

-- Now create tables with alternative names avoiding reserved words

CREATE TABLE UserAccount (
    user_id          INT(10) PRIMARY KEY,
    email            VARCHAR(255) UNIQUE,
    password         VARCHAR(255) NOT NULL,
    last_name        VARCHAR(255) NOT NULL,
    first_name       VARCHAR(255) NOT NULL,
    phone_number     VARCHAR(15),
    user_type        ENUM('customer', 'deliverer', 'admin') NOT NULL,
    license_plate    VARCHAR(25),
    total_earnings   DECIMAL(10,2)
);

CREATE TABLE AddressLine (
    address_id    INT(10) PRIMARY KEY,
    house_number  INT(5) NOT NULL,
    street        VARCHAR(255) NOT NULL,
    city          VARCHAR(255) NOT NULL,
    postal_code   VARCHAR(25) NOT NULL
);

CREATE TABLE Shop (
    shop_id       INT(10) PRIMARY KEY,
    name          VARCHAR(255) NOT NULL,
    address_id    INT(10) NOT NULL,
    CONSTRAINT fk_shop_address_id FOREIGN KEY (address_id) REFERENCES AddressLine(address_id)
);

CREATE TABLE Product (
    product_id     INT(10) PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(1000),
    price          DECIMAL(10,2) NOT NULL,
    category       VARCHAR(100) NOT NULL,
    is_available   BOOLEAN NOT NULL,
    alcohol        DECIMAL(4,2) NOT NULL
);

CREATE TABLE Command (
    command_id      INT(10) PRIMARY KEY,
    creation_date   DATETIME NOT NULL,
    status          ENUM('Searching','InRoute','Shipped','Cancelled','Completed') NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    address_id      INT(10) NOT NULL,
    shop_id         INT(10) NOT NULL,
    user_id         INT(10) NOT NULL,
    CONSTRAINT fk_command_address_id FOREIGN KEY (address_id) REFERENCES AddressLine(address_id),
    CONSTRAINT fk_command_shop_id FOREIGN KEY (shop_id) REFERENCES Shop(shop_id),
    CONSTRAINT fk_command_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE CommandProducts (
    command_id    INT(10) NOT NULL,
    product_id    INT(10) NOT NULL,
    quantity      INT(5) NOT NULL,
    PRIMARY KEY (command_id, product_id),
    CONSTRAINT fk_commandproducts_command_id FOREIGN KEY (command_id) REFERENCES Command(command_id),
    CONSTRAINT fk_commandproducts_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE ShopProducts (
    shop_id     INT(10) NOT NULL,
    product_id  INT(10) NOT NULL,
    quantity    INT(5) NOT NULL,
    PRIMARY KEY (shop_id, product_id),
    CONSTRAINT fk_shopproducts_shop_id FOREIGN KEY (shop_id) REFERENCES Shop(shop_id),
    CONSTRAINT fk_shopproducts_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE Payment (
    payment_id      INT(10) PRIMARY KEY,
    payment_method  VARCHAR(50) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    payment_date    DATETIME NOT NULL,
    is_completed    BOOLEAN NOT NULL,
    command_id      INT(10) NOT NULL,
    user_id         INT(10) NOT NULL,
    card_number     VARCHAR(20) NOT NULL,
    CVC_card        INT(3) NOT NULL,
    expiry_date     DATE NOT NULL,
    CONSTRAINT fk_payment_command_id FOREIGN KEY (command_id) REFERENCES Command(command_id),
    CONSTRAINT fk_payment_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);