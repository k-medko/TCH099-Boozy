DROP TABLE IF EXISTS ClientOrderProduct;
DROP TABLE IF EXISTS ShopProduct;
DROP TABLE IF EXISTS Payment;
DROP TABLE IF EXISTS ClientOrder;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Shop;
DROP TABLE IF EXISTS UserAccount;
DROP TABLE IF EXISTS AddressLine;

-- 1. Create AddressLine first, so it's available for other tables
CREATE TABLE AddressLine (
    address_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    civic INT(5) NOT NULL,
    apartment VARCHAR(10),
    street VARCHAR(255) NOT NULL,
    city VARCHAR(255) NOT NULL,
    postal_code VARCHAR(25) NOT NULL
);

-- 2. Create UserAccount; address_id is optional
CREATE TABLE UserAccount (
    user_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    phone_number VARCHAR(15) NOT NULL,
    address_id INT(10),
    user_type ENUM('client','carrier','admin') NOT NULL,
    license_plate VARCHAR(25) UNIQUE,
    car_brand VARCHAR(25),
    total_earnings DECIMAL(10,2),
    CONSTRAINT fk_user_address_id FOREIGN KEY (address_id) REFERENCES AddressLine(address_id)
        ON DELETE SET NULL
);

-- 3. Create Shop (requires an address from AddressLine)
CREATE TABLE Shop (
    shop_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address_id INT(10) NOT NULL UNIQUE,
    CONSTRAINT fk_shop_address_id FOREIGN KEY (address_id) REFERENCES AddressLine(address_id)
        ON DELETE CASCADE
);

-- 4. Create Product
CREATE TABLE Product (
    product_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    price DECIMAL(10,2) NOT NULL,
    category VARCHAR(100) NOT NULL,
    alcohol DECIMAL(4,2) NOT NULL
);

-- 5. Create ClientOrder with separate client and carrier references
CREATE TABLE ClientOrder (
    client_order_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    creation_date DATETIME NOT NULL,
    status ENUM('Searching','InRoute','Shipping','Cancelled','Completed') NOT NULL,
    total_amount DECIMAL(10,2) NOT NULL,
    address_id INT(10) NOT NULL,
    shop_id INT(10) NOT NULL,
    client_id INT(10) NOT NULL,
    carrier_id INT(10) NOT NULL,
    CONSTRAINT fk_clientorder_address_id FOREIGN KEY (address_id) REFERENCES AddressLine(address_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_clientorder_shop_id FOREIGN KEY (shop_id) REFERENCES Shop(shop_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_clientorder_client_id FOREIGN KEY (client_id) REFERENCES UserAccount(user_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_clientorder_carrier_id FOREIGN KEY (carrier_id) REFERENCES UserAccount(user_id)
        ON DELETE CASCADE
);

-- 6. Create ClientOrderProduct to link orders and products
CREATE TABLE ClientOrderProduct (
    client_order_id INT(10) NOT NULL,
    product_id INT(10) NOT NULL,
    quantity INT(5) NOT NULL,
    PRIMARY KEY (client_order_id, product_id),
    CONSTRAINT fk_clientorderproduct_client_order_id FOREIGN KEY (client_order_id) REFERENCES ClientOrder(client_order_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_clientorderproduct_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
        ON DELETE CASCADE
);

-- 7. Create ShopProduct to link shops and products
CREATE TABLE ShopProduct (
    shop_id INT(10) NOT NULL,
    product_id INT(10) NOT NULL,
    quantity INT(5) NOT NULL,
    PRIMARY KEY (shop_id, product_id),
    CONSTRAINT fk_shopproduct_shop_id FOREIGN KEY (shop_id) REFERENCES Shop(shop_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_shopproduct_product_id FOREIGN KEY (product_id) REFERENCES Product(product_id)
        ON DELETE CASCADE
);

-- 8. Create Payment with an additional card_name string column
CREATE TABLE Payment (
    payment_id INT(10) AUTO_INCREMENT PRIMARY KEY,
    payment_method VARCHAR(50) NOT NULL,
    amount DECIMAL(10,2) NOT NULL,
    payment_date DATETIME NOT NULL,
    is_completed BOOLEAN NOT NULL,
    client_order_id INT(10) NOT NULL,
    user_id INT(10) NOT NULL,
    card_name VARCHAR(255) NOT NULL,
    card_number VARCHAR(20) NOT NULL,
    CVC_card INT(3) NOT NULL,
    expiry_date_month INT(2) NOT NULL,
    expiry_date_year INT(2) NOT NULL,
    CONSTRAINT fk_payment_client_order_id FOREIGN KEY (client_order_id) REFERENCES ClientOrder(client_order_id)
        ON DELETE CASCADE,
    CONSTRAINT fk_payment_user_id FOREIGN KEY (user_id) REFERENCES UserAccount(user_id)
        ON DELETE CASCADE
);
