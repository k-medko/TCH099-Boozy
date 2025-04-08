-- Insert real Montreal addresses for the customers
INSERT INTO AddressLine (civic, apartment, street, city, postal_code)
VALUES 
  (123, '1A', 'Rue Sainte-Catherine Ouest', 'Montreal', 'H3B 1A7'),
  (456, '2B', 'Avenue du Parc', 'Montreal', 'H2V 4E6');

-- Insert 6 admins (last_name = 'admin', address_id = NULL, total_earnings = NULL)
INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, total_earnings)
VALUES
  ('tristan@boozy.com', 'adminpass', 'admin', 'Tristan', '1111111111', NULL, 'admin', NULL, NULL),
  ('kahina@boozy.com', 'adminpass', 'admin', 'Kahina', '3333333333', NULL, 'admin', NULL, NULL),
  ('shawn@boozy.com', 'adminpass', 'admin', 'Shawn', '2222222222', NULL, 'admin', NULL, NULL),
  ('emile@boozy.com', 'adminpass', 'admin', 'Emile', '4444444444', NULL, 'admin', NULL, NULL),
  ('edwar@boozy.com', 'adminpass', 'admin', 'Edwar', '5555555555', NULL, 'admin', NULL, NULL),
  ('macky@boozy.com', 'adminpass', 'admin', 'Macky', '6666666666', NULL, 'admin', NULL, NULL);

-- Insert 2 customers with addresses (using the newly inserted address_id 1 and 2)
INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, total_earnings)
VALUES
  ('customer1@boozy.com', 'custpass', 'Smith', 'Alice', '7777777777', 1, 'customer', NULL, NULL),
  ('customer2@boozy.com', 'custpass', 'Johnson', 'Bob', '8888888888', 2, 'customer', NULL, NULL);

-- Insert 2 deliverers with actual total_earnings and sample license plates (address_id = NULL)
INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, total_earnings)
VALUES
  ('deliverer1@boozy.com', 'delivpass', 'Brown', 'Charlie', '9999999999', NULL, 'deliverer', 'AB-123-CD', 150.00),
  ('deliverer2@boozy.com', 'delivpass', 'Prince', 'Diana', '1010101010', NULL, 'deliverer', 'EF-456-GH', 200.00);

-- Insert addresses for 6 SAQ stores in Montreal
INSERT INTO AddressLine (civic, apartment, street, city, postal_code)
VALUES
  (100, NULL, 'Rue Sainte-Catherine Ouest', 'Montreal', 'H3B 1A9'),
  (200, NULL, 'Rue Côte-des-Neiges', 'Montreal', 'H3S 2J4'),
  (300, NULL, 'Boulevard Saint-Laurent', 'Montreal', 'H2W 1Z8'),
  (400, NULL, 'Avenue Papineau', 'Montreal', 'H1X 2V3'),
  (500, NULL, 'Rue Sherbrooke Est', 'Montreal', 'H2L 1N2'),
  (600, NULL, 'Avenue de l''Acadie', 'Montreal', 'H1S 1T1');

-- Insert 6 SAQ stores linked to the addresses above (assuming auto-increment produces address_id 1 to 6)
INSERT INTO Shop (name, address_id)
VALUES
  ('SAQ Centre-Ville', 3),
  ('SAQ Côte-des-Neiges', 4),
  ('SAQ Plateau', 5),
  ('SAQ Hochelaga', 6),
  ('SAQ Outremont', 7),
  ('SAQ Saint-Hubert', 8);

INSERT INTO Product (name, description, price, category, alcohol) VALUES
  ('Heineken Lager Beer', 'Popular Dutch pale lager', 3.50, 'Beer', 5.00),
  ('Corona Extra Beer', 'Mexican pale lager with a light, refreshing taste', 4.00, 'Beer', 4.60),
  ('Budweiser', 'American lager with a classic taste', 3.20, 'Beer', 5.00),
  ('Guinness Draught', 'Irish dry stout with a creamy head', 5.00, 'Beer', 4.20),
  ('Stella Artois', 'Belgian pale lager known for its crisp taste', 4.50, 'Beer', 5.00),
  ('Samuel Adams Boston Lager', 'American amber lager with a rich flavor', 4.50, 'Beer', 5.00),
  ('Sapporo Premium Beer', 'Japanese lager with a smooth finish', 4.00, 'Beer', 5.00),
  ('Leffe Blonde', 'Belgian abbey beer with a fruity aroma', 4.50, 'Beer', 6.60),
  ('Chimay Blue', 'Belgian strong dark ale with complex flavors', 6.00, 'Beer', 9.00),
  ('Hoegaarden White', 'Belgian witbier with a refreshing citrus taste', 4.50, 'Beer', 4.90),
  ('Yellow Tail Shiraz', 'Australian red wine, fruity and vibrant', 10.00, 'Wine', 13.50),
  ('Barefoot Pinot Grigio', 'Light and crisp white wine with subtle flavors', 9.00, 'Wine', 12.00),
  ('Jacobs Creek Classic Cabernet Sauvignon', 'Full-bodied red wine from Australia with rich tannins', 15.00, 'Wine', 14.00),
  ('Kim Crawford Sauvignon Blanc', 'New Zealand white wine with tropical fruit notes', 16.00, 'Wine', 13.00),
  ('Cloudy Bay Pinot Noir', 'Elegant red wine with soft, velvety tannins', 30.00, 'Wine', 13.50),
  ('La Crema Chardonnay', 'California Chardonnay with a balanced oak finish', 25.00, 'Wine', 14.50),
  ('Jack Daniels Old No. 7', 'Iconic Tennessee whiskey with smooth flavors', 30.00, 'Whiskey', 40.00),
  ('Jameson Irish Whiskey', 'Smooth Irish whiskey with a gentle finish', 28.00, 'Whiskey', 40.00),
  ('Johnnie Walker Black Label', 'Premium blended Scotch whisky with a rich aroma', 45.00, 'Whiskey', 40.00),
  ('Glenfiddich 12 Year Old', 'Single malt Scotch whisky with floral and fruity notes', 50.00, 'Whiskey', 40.00),
  ('Absolut Vodka', 'Classic Swedish vodka known for its purity', 25.00, 'Vodka', 40.00),
  ('Smirnoff No. 21 Vodka', 'Widely popular Russian vodka with a crisp taste', 20.00, 'Vodka', 40.00),
  ('Grey Goose Vodka', 'Premium French vodka with a smooth finish', 45.00, 'Vodka', 41.00),
  ('Bombay Sapphire Gin', 'London dry gin infused with aromatic botanicals', 35.00, 'Gin', 40.00),
  ('Tanqueray London Dry Gin', 'Classic gin with a crisp, juniper flavor', 32.00, 'Gin', 43.00),
  ('Hendricks Gin', 'Unique gin infused with cucumber and rose petals', 38.00, 'Gin', 41.00),
  ('Bacardi Superior Rum', 'Light and refreshing white rum', 22.00, 'Rum', 40.00),
  ('Captain Morgan Spiced Rum', 'Rich, spicy rum with a smooth finish', 25.00, 'Rum', 35.00),
  ('Mount Gay Eclipse Rum', 'Classic Barbados rum with hints of spice', 28.00, 'Rum', 40.00),
  ('Jose Cuervo Especial', 'Iconic tequila with a smooth, balanced taste', 20.00, 'Tequila', 38.00);


INSERT INTO ShopProduct (shop_id, product_id, quantity) VALUES
-- Store 1: Products 1, 3, 5, 7, 9, 11, 13, 15, 17, 19
(1, 1, 12),
(1, 3, 20),
(1, 5, 15),
(1, 7, 25),
(1, 9, 30),
(1, 11, 18),
(1, 13, 22),
(1, 15, 17),
(1, 17, 27),
(1, 19, 14),

-- Store 2: Products 2, 4, 6, 8, 10, 12, 14, 16, 18, 20
(2, 2, 13),
(2, 4, 21),
(2, 6, 16),
(2, 8, 24),
(2, 10, 31),
(2, 12, 19),
(2, 14, 28),
(2, 16, 15),
(2, 18, 33),
(2, 20, 22),

-- Store 3: Products 21, 22, 23, 24, 25, 26, 27, 28, 29, 30
(3, 21, 11),
(3, 22, 23),
(3, 23, 17),
(3, 24, 29),
(3, 25, 14),
(3, 26, 20),
(3, 27, 32),
(3, 28, 18),
(3, 29, 26),
(3, 30, 21),

-- Store 4: Products 1, 4, 7, 10, 13, 16, 19, 22, 25, 28
(4, 1, 19),
(4, 4, 27),
(4, 7, 16),
(4, 10, 20),
(4, 13, 24),
(4, 16, 17),
(4, 19, 30),
(4, 22, 15),
(4, 25, 21),
(4, 28, 18),

-- Store 5: Products 2, 5, 8, 11, 14, 17, 20, 23, 26, 29
(5, 2, 14),
(5, 5, 22),
(5, 8, 18),
(5, 11, 26),
(5, 14, 17),
(5, 17, 19),
(5, 20, 27),
(5, 23, 21),
(5, 26, 23),
(5, 29, 15),

-- Store 6: Products 3, 6, 9, 12, 15, 18, 21, 24, 27, 30
(6, 3, 16),
(6, 6, 20),
(6, 9, 25),
(6, 12, 17),
(6, 15, 23),
(6, 18, 28),
(6, 21, 19),
(6, 24, 22),
(6, 27, 15),
(6, 30, 18);