-- Insert script for all tables, with mock data and proper foreign keys
-- Make sure the database and tables are already created according to your schema.
-- This script only INSERTs data in the correct order to respect FK constraints.

-- 1) USERACCOUNT
INSERT INTO UserAccount (user_id, email, last_name, first_name, phone_number, user_type, license_plate)
VALUES
(1, 'john.doe@example.com',    'Doe',     'John',    '514-555-0101', 'customer',   NULL),
(2, 'jane.smith@example.com',  'Smith',   'Jane',    '514-555-0102', 'customer',   NULL),
(3, 'mark.rider@example.com',  'Rider',   'Mark',    '438-555-0103', 'deliverer',  'ABC-1234'),
(4, 'lucy.driver@example.com', 'Driver',  'Lucy',    '438-555-0104', 'deliverer',  'XYZ-9876'),
(5, 'admin.one@example.com',   'Admin',   'One',     '514-555-0105', 'admin',      NULL),
(6, 'alice.jones@example.com', 'Jones',   'Alice',   '514-555-0106', 'customer',   NULL),
(7, 'bob.martin@example.com',  'Martin',  'Bob',     '514-555-0107', 'customer',   NULL),
(8, 'admin.two@example.com',   'Manager', 'Julie',   '514-555-0108', 'admin',      NULL);

-- 2) CUSTOMERADDRESS
-- Addresses for users (some for customers, some can be for deliverers if you wish).
INSERT INTO CustomerAddress (address_id, street, postal_code, city, user_id)
VALUES
(1, '123 Maple Street',  'H2A 1B1', 'Montreal',    1),
(2, '456 Oak Avenue',    'H2B 2C2', 'Montreal',    2),
(3, '789 Pine Road',     'H2C 3D3', 'Laval',       6),
(4, '321 Birch Lane',    'H2E 5G8', 'Longueuil',   7),
(5, '100 Admin Blvd',    'H2G 9Y1', 'Montreal',    5),
(6, '55 Rider Place',    'H2H 1J1', 'Montreal',    3),
(7, '22 Driver Path',    'H2Q 7P2', 'Montreal',    4),
(8, '999 Manager Way',   'H2X 8Z2', 'Montreal',    8);

-- 3) STORELOCATION
-- Each store references an address. Choose addresses that are not necessarily used by users,
-- or reuse some for simplicity. We'll reuse #4 and #5 for store addresses, for instance.
INSERT INTO StoreLocation (store_id, name, address_id)
VALUES
(1, 'Downtown Store',     4),
(2, 'Laval Supermarket',  5),
(3, 'Longueuil Depot',    8);

-- 4) PRODUCT
INSERT INTO Product (product_id, name, description, price, category, is_available)
VALUES
(1, 'Heineken Beer',      'Popular imported beer',          2.99,  'Beverage',  TRUE),
(2, 'Smirnoff Vodka',     'Vodka with 40% alcohol',         25.99, 'Spirits',   TRUE),
(3, 'Corona Extra',       'Mexican beer',                   3.49,  'Beverage',  TRUE),
(4, 'Jack Daniels',       'Tennessee whiskey',              29.99, 'Spirits',   TRUE),
(5, 'Red Wine Merlot',    'Smooth red wine',                15.49, 'Wine',      TRUE),
(6, 'White Wine Riesling','Crisp white wine',               13.99, 'Wine',      TRUE),
(7, 'Captain Morgan Rum', 'Spiced rum',                     24.99, 'Spirits',   TRUE),
(8, 'Baileys Irish Cream','Cream liqueur',                  21.99, 'Liqueur',   TRUE),
(9, 'Gin Tonic Mix',      'Premixed gin & tonic can',       2.79,  'Beverage',  TRUE),
(10,'Bacardi Superior',   'White rum 40% alcohol',          26.99, 'Spirits',   TRUE);

-- 5) PROMOTIONALOFFER (references product_id)
INSERT INTO PromotionalOffer (promo_code, description, discount, expiration, product_id)
VALUES
('SUMMER2025', 'Summer discount on Corona',     0.50,  '2025-12-31',  3),
('WINE10',     '10% off on any red wine',       1.50,  '2025-10-01',  5),
('SPIRIT5',    '5% off selected spirit',        1.30,  '2026-01-15',  4);

-- 6) PRODUCTINVENTORY (store_id, product_id)
-- We'll stock each store with various products in random quantities
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(1, 1, 50),
(1, 2, 10),
(1, 3, 60),
(1, 4, 8),
(1, 5, 25),
(2, 2, 12),
(2, 3, 90),
(2, 6, 30),
(2, 7, 15),
(2, 10,40),
(3, 1, 100),
(3, 4, 14),
(3, 5, 20),
(3, 8, 18),
(3, 9, 44);

-- 7) CUSTOMERORDER (references address_id, store_id, user_id)
INSERT INTO CustomerOrder (order_id, creation_date, status, total_amount, address_id, store_id, user_id)
VALUES
(1, '2025-03-28 10:15:00', 'pending',   32.48, 1, 1, 1),
(2, '2025-03-28 10:30:00', 'pending',   50.00, 2, 2, 2),
(3, '2025-03-28 11:00:00', 'delivered', 64.99, 3, 1, 6),
(4, '2025-03-28 12:05:00', 'pending',   70.47, 1, 1, 7),
(5, '2025-03-28 14:20:00', 'pending',   18.99, 2, 3, 2);

-- 8) ORDERITEMS (references CustomerOrder(order_id), Product(product_id))
INSERT INTO OrderItems (order_id, product_id, quantity)
VALUES
(1, 1,  3),
(1, 4,  1),
(2, 2,  2),
(2, 3,  3),
(2, 7,  1),
(3, 5,  2),
(3, 1,  4),
(4, 1,  2),
(4, 2,  2),
(5, 9,  6),
(5, 10, 2);

-- 9) PAYMENTTRANSACTION (references CustomerOrder(order_id), UserAccount(user_id))
INSERT INTO PaymentTransaction (payment_id, payment_method, amount, payment_date, is_completed, order_id, user_id)
VALUES
(1, 'credit_card', 32.48, '2025-03-28 10:20:00', TRUE,  1, 1),
(2, 'cash',        64.99, '2025-03-28 11:10:00', TRUE,  3, 6),
(3, 'credit_card', 50.00, '2025-03-28 10:35:00', FALSE, 2, 2),
(4, 'paypal',      70.47, '2025-03-28 12:15:00', FALSE, 4, 7),
(5, 'credit_card', 18.99, '2025-03-28 14:25:00', TRUE,  5, 2);
