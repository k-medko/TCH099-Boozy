
-- 1) Insert into UserAccount (10 accounts)
INSERT INTO UserAccount (user_id, email, last_name, first_name, phone_number, user_type, license_plate)
VALUES
(1, 'john.doe@example.com',    'Doe',    'John',   '514-555-0101', 'customer',   NULL),
(2, 'jane.smith@example.com',  'Smith',  'Jane',   '514-555-0102', 'customer',   NULL),
(3, 'mark.rider@example.com',  'Rider',  'Mark',   '438-555-0103', 'deliverer',  'ABC-1234'),
(4, 'lucy.driver@example.com', 'Driver', 'Lucy',   '438-555-0104', 'deliverer',  'XYZ-9876'),
(5, 'admin.one@example.com',   'Admin',  'One',    '514-555-0105', 'admin',      NULL),
(6, 'alice.jones@example.com', 'Jones',  'Alice',  '514-555-0106', 'customer',   NULL),
(7, 'bob.martin@example.com',  'Martin', 'Bob',    '514-555-0107', 'customer',   NULL),
(8, 'admin.two@example.com',   'Manager','Julie',  '514-555-0108', 'admin',      NULL),
(9, 'sam.green@example.com',   'Green',  'Sam',    '514-555-0109', 'customer',   NULL),
(10, 'lisa.brown@example.com',  'Brown',  'Lisa',   '514-555-0110', 'customer',   NULL);

-- 2) Insert into CustomerAddress for user accounts (10 rows)
INSERT INTO CustomerAddress (address_id, house_number, street, postal_code, city, user_id)
VALUES
(1, 123, 'Maple Street',    'H2A 1B1', 'Montreal', 1),
(2, 456, 'Oak Avenue',      'H2B 2C2', 'Montreal', 2),
(3, 789, 'Pine Road',       'H2C 3D3', 'Laval',    3),
(4, 321, 'Birch Lane',      'H2E 5G8', 'Longueuil',4),
(5, 100, 'Admin Blvd',      'H2G 9Y1', 'Montreal', 5),
(6, 55,  'Rider Place',     'H2H 1J1', 'Montreal', 6),
(7, 22,  'Driver Path',     'H2Q 7P2', 'Montreal', 7),
(8, 999, 'Manager Way',     'H2X 8Z2', 'Montreal', 8),
(9, 77,  'Sunset Boulevard','H3Z 2K1', 'Montreal', 9),
(10, 88, 'Lakeside Drive',  'H4B 3C5', 'Quebec',   10);

-- 3) Insert additional CustomerAddress rows for store locations (10 rows)
-- We assign these addresses to the admin (user_id = 5) for simplicity.
INSERT INTO CustomerAddress (address_id, house_number, street, postal_code, city, user_id)
VALUES
(11, 10, 'Central Street',  'H1A 2B3', 'Montreal', 5),
(12, 20, 'North Avenue',    'H1B 3C4', 'Montreal', 5),
(13, 30, 'South Road',      'H1C 4D5', 'Montreal', 5),
(14, 40, 'East Boulevard',  'H1D 5E6', 'Montreal', 5),
(15, 50, 'West Lane',       'H1E 6F7', 'Montreal', 5),
(16, 60, 'Uptown Street',   'H1F 7G8', 'Montreal', 5),
(17, 70, 'Downtown Ave',    'H1G 8H9', 'Montreal', 5),
(18, 80, 'Market Road',     'H1H 9I0', 'Montreal', 5),
(19, 90, 'Garden Path',     'H1I 0J1', 'Montreal', 5),
(20, 100,'Harbor Way',      'H1J 1K2', 'Montreal', 5);

-- 4) Insert into StoreLocation (10 stores)
INSERT INTO StoreLocation (store_id, name, address_id)
VALUES
(1, 'Downtown Store',     11),
(2, 'Uptown Market',      12),
(3, 'Central Store',      13),
(4, 'Northside Grocery',  14),
(5, 'Southside Shop',     15),
(6, 'East End Mart',      16),
(7, 'West End Outlet',    17),
(8, 'Garden Store',       18),
(9, 'Harbor Market',      19),
(10, 'City Center Shop',  20);

-- 5) Insert into Product (55 products)
INSERT INTO Product (product_id, name, description, price, category, is_available)
VALUES
(1,  'Heineken Beer',           'Popular imported beer',                2.99,  'Beverage', TRUE),
(2,  'Smirnoff Vodka',          'Vodka with 40% alcohol',               25.99, 'Spirits',  TRUE),
(3,  'Corona Extra',            'Mexican beer',                         3.49,  'Beverage', TRUE),
(4,  'Jack Daniels',            'Tennessee whiskey',                    29.99, 'Spirits',  TRUE),
(5,  'Red Wine Merlot',         'Smooth red wine',                      15.49, 'Wine',     TRUE),
(6,  'White Wine Riesling',     'Crisp white wine',                     13.99, 'Wine',     TRUE),
(7,  'Captain Morgan Rum',      'Spiced rum',                           24.99, 'Spirits',  TRUE),
(8,  'Baileys Irish Cream',     'Cream liqueur',                        21.99, 'Liqueur',  TRUE),
(9,  'Gin Tonic Mix',           'Premixed gin & tonic can',             2.79,  'Beverage', TRUE),
(10, 'Bacardi Superior',        'White rum 40% alcohol',                26.99, 'Spirits',  TRUE),
(11, 'Budweiser',               'American lager beer',                  2.49,  'Beverage', TRUE),
(12, 'Stella Artois',           'Belgian pilsner',                      3.29,  'Beverage', TRUE),
(13, 'Coors Light',             'Light American beer',                  2.19,  'Beverage', TRUE),
(14, 'Miller Lite',             'American light beer',                  2.19,  'Beverage', TRUE),
(15, 'Chivas Regal',            'Blended Scotch whisky',                35.99, 'Spirits',  TRUE),
(16, 'Jameson Irish Whiskey',   'Smooth Irish whiskey',                 28.99, 'Spirits',  TRUE),
(17, 'Glenfiddich 12',          'Single malt Scotch whisky',            40.99, 'Spirits',  TRUE),
(18, 'Moet & Chandon',          'Champagne',                            49.99, 'Wine',     TRUE),
(19, 'Veuve Clicquot',          'Premium Champagne',                    59.99, 'Wine',     TRUE),
(20, 'Dom Perignon',            'Luxury Champagne',                     199.99,'Wine',     TRUE),
(21, 'Sierra Nevada Pale Ale',  'Craft beer',                           3.99,  'Beverage', TRUE),
(22, 'Lagunitas IPA',           'American IPA beer',                    4.29,  'Beverage', TRUE),
(23, 'Blue Moon',               'Belgian-style wheat beer',             3.59,  'Beverage', TRUE),
(24, 'Dos Equis Lager',         'Mexican lager',                        2.99,  'Beverage', TRUE),
(25, 'Dos Equis Amber',         'Mexican amber ale',                    3.49,  'Beverage', TRUE),
(26, 'Guinness Draught',        'Irish stout',                          4.49,  'Beverage', TRUE),
(27, 'Pilsner Urquell',         'Czech pilsner',                        3.19,  'Beverage', TRUE),
(28, 'Rothschild Cabernet',     'Full-bodied red wine',                 22.99, 'Wine',     TRUE),
(29, 'Sauvignon Blanc',         'Crisp white wine',                     18.99, 'Wine',     TRUE),
(30, 'Merlot Reserve',          'Aged merlot wine',                     24.99, 'Wine',     TRUE),
(31, 'Chardonnay Classic',      'Classic white wine',                   19.99, 'Wine',     TRUE),
(32, 'Zinfandel',               'Robust red wine',                      21.49, 'Wine',     TRUE),
(33, 'Pinot Noir',              'Elegant red wine',                     27.99, 'Wine',     TRUE),
(34, 'Riesling Sweet',          'Sweet white wine',                     16.99, 'Wine',     TRUE),
(35, 'Cognac VS',               'Entry-level cognac',                   34.99, 'Spirits',  TRUE),
(36, 'Cognac VSOP',             'Premium cognac',                       44.99, 'Spirits',  TRUE),
(37, 'Absinthe',                'Strong herbal spirit',                 39.99, 'Spirits',  TRUE),
(38, 'Sambuca',                 'Italian anise-flavored liqueur',       29.99, 'Liqueur',  TRUE),
(39, 'Triple Sec',              'Orange-flavored liqueur',              19.99, 'Liqueur',  TRUE),
(40, 'Cointreau',               'Premium orange liqueur',               34.99, 'Liqueur',  TRUE),
(41, 'Pisco',                   'South American brandy',                24.49, 'Spirits',  TRUE),
(42, 'Mezcal',                  'Smoky agave spirit',                   31.99, 'Spirits',  TRUE),
(43, 'Tequila Blanco',          'Unaged tequila',                       27.99, 'Spirits',  TRUE),
(44, 'Tequila Reposado',        'Aged tequila',                         32.99, 'Spirits',  TRUE),
(45, 'Añejo Rum',               'Aged Caribbean rum',                   28.99, 'Spirits',  TRUE),
(46, 'Sour Apple Schnapps',     'Fruity schnapps',                      14.99, 'Liqueur',  TRUE),
(47, 'Butterscotch Liqueur',    'Sweet butterscotch flavor',            15.99, 'Liqueur',  TRUE),
(48, 'Coffee Liqueur',          'Rich coffee flavor',                   17.99, 'Liqueur',  TRUE),
(49, 'Hazelnut Liqueur',        'Nutty liqueur',                        16.99, 'Liqueur',  TRUE),
(50, 'Mint Liqueur',            'Refreshing mint flavor',               13.99, 'Liqueur',  TRUE),
(51, 'Lemon Vodka',             'Citrus-infused vodka',                 26.49, 'Spirits',  TRUE),
(52, 'Peach Schnapps',          'Sweet peach flavor',                   15.49, 'Liqueur',  TRUE),
(53, 'Cranberry Juice',         'Fresh cranberry juice',                2.99,  'Beverage', TRUE),
(54, 'Orange Juice',            'Fresh squeezed orange juice',          3.49,  'Beverage', TRUE),
(55, 'Pineapple Juice',         'Tropical juice',                       3.99,  'Beverage', TRUE);

-- 6) Insert into PromotionalOffer (4 offers)
INSERT INTO PromotionalOffer (promo_code, description, discount, expiration, product_id)
VALUES
('SUMMER2025', 'Summer discount on Corona Extra', 0.50, '2025-12-31', 3),
('WINE10',     '10% off on Red Wine Merlot',       1.50, '2025-10-01', 5),
('SPIRIT5',    '5% off on Jack Daniels',           1.30, '2026-01-15', 4),
('BEER20',     '20% off on Budweiser',             0.50, '2025-08-31', 11);

-- 7) Insert into ProductInventory (Stock for each store)
-- Store 1 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(1, 1, 50),
(1, 2, 40),
(1, 3, 60),
(1, 4, 30),
(1, 5, 20);

-- Store 2 stocks 5 different products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(2, 6, 35),
(2, 7, 25),
(2, 8, 15),
(2, 9, 45),
(2, 10, 55);

-- Store 3 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(3, 11, 100),
(3, 12, 90),
(3, 13, 80),
(3, 14, 70),
(3, 15, 60);

-- Store 4 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(4, 16, 50),
(4, 17, 45),
(4, 18, 40),
(4, 19, 35),
(4, 20, 30);

-- Store 5 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(5, 21, 20),
(5, 22, 25),
(5, 23, 30),
(5, 24, 35),
(5, 25, 40);

-- Store 6 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(6, 26, 50),
(6, 27, 55),
(6, 28, 60),
(6, 29, 65),
(6, 30, 70);

-- Store 7 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(7, 31, 15),
(7, 32, 20),
(7, 33, 25),
(7, 34, 30),
(7, 35, 35);

-- Store 8 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(8, 36, 40),
(8, 37, 45),
(8, 38, 50),
(8, 39, 55),
(8, 40, 60);

-- Store 9 stocks 5 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(9, 41, 65),
(9, 42, 70),
(9, 43, 75),
(9, 44, 80),
(9, 45, 85);

-- Store 10 stocks 10 products:
INSERT INTO ProductInventory (store_id, product_id, quantity)
VALUES
(10, 46, 20),
(10, 47, 25),
(10, 48, 30),
(10, 49, 35),
(10, 50, 40),
(10, 51, 45),
(10, 52, 50),
(10, 53, 55),
(10, 54, 60),
(10, 55, 65);