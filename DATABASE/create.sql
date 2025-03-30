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
    last_name        VARCHAR(255),
    first_name       VARCHAR(255),
    phone_number     VARCHAR(15),
    user_type        ENUM('customer', 'deliverer', 'admin'),
    license_plate    VARCHAR(25)
);

CREATE TABLE CustomerAddress (
    address_id    INT(10) PRIMARY KEY,
    house_number  INT(5),
    street        VARCHAR(255),
    postal_code   VARCHAR(25),
    city          VARCHAR(255),
    user_id       INT(10),
    CONSTRAINT fk_address_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE StoreLocation (
    store_id     INT(10) PRIMARY KEY,
    name         VARCHAR(255),
    address_id   INT(10),
    CONSTRAINT fk_store_address_id FOREIGN KEY (address_id) REFERENCES CustomerAddress(address_id)
);

CREATE TABLE Product (
    product_id     INT(10) PRIMARY KEY,
    name           VARCHAR(255),
    description    VARCHAR(1000),
    price          DECIMAL(10,2),
    category       VARCHAR(100),
    is_available   BOOLEAN
);

CREATE TABLE CustomerOrder (
    order_id        INT(10) PRIMARY KEY,
    creation_date   DATETIME,
    status          VARCHAR(15),
    total_amount    DECIMAL(10,2),
    address_id      INT(10),
    store_id        INT(10),
    user_id         INT(10),
    CONSTRAINT fk_customerorder_address_id FOREIGN KEY (address_id) REFERENCES CustomerAddress(address_id),
    CONSTRAINT fk_customerorder_store_id FOREIGN KEY (store_id) REFERENCES StoreLocation(store_id),
    CONSTRAINT fk_customerorder_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE OrderItems (
    order_id      INT(10),
    product_id    INT(10),
    quantity      INT(5),
    PRIMARY KEY (order_id, product_id),
    CONSTRAINT fk_orderitems_order_id FOREIGN KEY (order_id) REFERENCES CustomerOrder(order_id),
    CONSTRAINT fk_orderitems_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE ProductInventory (
    store_id       INT(10),
    product_id     INT(10),
    quantity       INT(5),
    CONSTRAINT pk_productinventory_primary_keys PRIMARY KEY (store_id, product_id),
    CONSTRAINT fk_productinventory_store_id FOREIGN KEY (store_id) REFERENCES StoreLocation(store_id),
    CONSTRAINT fk_productinventory_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);

CREATE TABLE PaymentTransaction (
    payment_id      INT(10) PRIMARY KEY,
    payment_method  VARCHAR(50),
    amount          DECIMAL(10,2),
    payment_date    DATETIME,
    is_completed    BOOLEAN,
    order_id        INT(10),
    user_id         INT(10),
    CONSTRAINT fk_paymenttransaction_order_id FOREIGN KEY (order_id) REFERENCES CustomerOrder(order_id),
    CONSTRAINT fk_paymenttransaction_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
);

CREATE TABLE PromotionalOffer (
    promo_code    VARCHAR(25) PRIMARY KEY,
    description   VARCHAR(500),
    discount      DECIMAL(10,2),
    expiration    DATE,
    product_id    INT(10),
    CONSTRAINT fk_promotionaloffer_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
);