-- First drop all tables in the correct order (respecting foreign key constraints)
DROP TABLE IF EXISTS OrderItems;
DROP TABLE IF EXISTS ProductInventory;
DROP TABLE IF EXISTS PromotionalOffer;
DROP TABLE IF EXISTS PaymentTransaction;
DROP TABLE IF EXISTS CustomerOrder;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS StoreLocation;
DROP TABLE IF EXISTS CustomerAddress;
DROP TABLE IF EXISTS UserAccount;

-- Now create tables with alternative names avoiding reserved words
CREATE TABLE UserAccount (
    user_id          INT(10) PRIMARY KEY,
    email            VARCHAR(255) UNIQUE,
    last_name        VARCHAR(255) NOT NULL,
    first_name       VARCHAR(255) NOT NULL,
    phone_number     VARCHAR(15),
    user_type        ENUM('customer', 'deliverer', 'admin') NOT NULL,
    license_plate    VARCHAR(25) NOT NULL
);

CREATE TABLE CustomerAddress (
    address_id    INT(10) PRIMARY KEY,
    house_number  INT(5) NOT NULL,
    street        VARCHAR(255) NOT NULL,
    postal_code   VARCHAR(25) NOT NULL,
    city          VARCHAR(255) NOT NULL,
    user_id       INT(10) NOT NULL,
    CONSTRAINT fk_address_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE StoreLocation (
    store_id     INT(10) PRIMARY KEY,
    name         VARCHAR(255) NOT NULL,
    address_id   INT(10) NOT NULL,
    CONSTRAINT fk_store_address_id FOREIGN KEY (address_id) REFERENCES CustomerAddress(address_id)
);

CREATE TABLE Product (
    product_id     INT(10) PRIMARY KEY,
    name           VARCHAR(255) NOT NULL,
    description    VARCHAR(1000),
    price          DECIMAL(10,2) NOT NULL,
    category       VARCHAR(100) NOT NULL,
    is_available   BOOLEAN  NOT NULL
);

CREATE TABLE CustomerOrder (
    order_id        INT(10) PRIMARY KEY,
    creation_date   DATETIME NOT NULL,
    status          VARCHAR(15) NOT NULL,
    total_amount    DECIMAL(10,2) NOT NULL,
    address_id      INT(10) NOT NULL,
    store_id        INT(10) NOT NULL,
    user_id         INT(10) NOT NULL,
    CONSTRAINT fk_customerorder_address_id FOREIGN KEY (address_id) REFERENCES CustomerAddress(address_id),
    CONSTRAINT fk_customerorder_store_id FOREIGN KEY (store_id) REFERENCES StoreLocation(store_id),
    CONSTRAINT fk_customerorder_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE OrderItems (
    order_id      INT(10) NOT NULL,
    product_id    INT(10) NOT NULL,
    quantity      INT(5) NOT NULL,
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_orderitems_order_id FOREIGN KEY (order_id) REFERENCES CustomerOrder(order_id),
    CONSTRAINT fk_orderitems_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE ProductInventory (
    store_id       INT(10) NOT NULL,
    product_id     INT(10) NOT NULL,
    quantity       INT(5) NOT NULL,
    CONSTRAINT pk_productinventory_primary_keys PRIMARY KEY (store_id, product_id),
    CONSTRAINT fk_productinventory_store_id FOREIGN KEY (store_id) REFERENCES StoreLocation(store_id),
    CONSTRAINT fk_productinventory_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE PaymentTransaction (
    payment_id      INT(10) PRIMARY KEY,
    payment_method  VARCHAR(50) NOT NULL,
    amount          DECIMAL(10,2) NOT NULL,
    payment_date    DATETIME NOT NULL,
    is_completed    BOOLEAN NOT NULL,
    order_id        INT(10) NOT NULL,
    user_id         INT(10) NOT NULL,
    CONSTRAINT fk_paymenttransaction_order_id FOREIGN KEY (order_id) REFERENCES CustomerOrder(order_id),
    CONSTRAINT fk_paymenttransaction_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE PromotionalOffer (
    promo_code    VARCHAR(25) PRIMARY KEY,
    description   VARCHAR(500),
    discount      DECIMAL(10,2) NOT NULL,
    expiration    DATE NOT NULL,
    product_id    INT(10) NOT NULL,
    CONSTRAINT fk_promotionaloffer_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);
