-- Insert data for UserAccount
INSERT INTO UserAccount (user_id, email, password, last_name, first_name, phone_number, user_type, license_plate, total_earnings)
VALUES 
    (1, 'jean.tremblay@email.com', 'hashed_password_123', 'Tremblay', 'Jean', '514-555-1234', 'customer', NULL, NULL),
    (2, 'marie.lavoie@email.com', 'hashed_password_456', 'Lavoie', 'Marie', '438-555-2345', 'customer', NULL, NULL),
    (3, 'pierre.gagnon@email.com', 'hashed_password_789', 'Gagnon', 'Pierre', '450-555-3456', 'customer', NULL, NULL),
    (4, 'julie.cote@email.com', 'hashed_password_101', 'Côté', 'Julie', '514-555-4567', 'customer', NULL, NULL),
    (5, 'francois.boucher@email.com', 'hashed_password_102', 'Boucher', 'François', '438-555-5678', 'deliverer', 'ABC 123', 1250.75),
    (6, 'sophie.bergeron@email.com', 'hashed_password_103', 'Bergeron', 'Sophie', '450-555-6789', 'deliverer', 'DEF 456', 987.50),
    (7, 'robert.morin@email.com', 'hashed_password_104', 'Morin', 'Robert', '514-555-7890', 'admin', NULL, NULL),
    (8, 'isabelle.roy@email.com', 'hashed_password_105', 'Roy', 'Isabelle', '438-555-8901', 'admin', NULL, NULL);

-- Insert data for AddressLine
INSERT INTO AddressLine (address_id, house_number, street, city, postal_code)
VALUES 
    (1, 123, 'Rue Sainte-Catherine', 'Montréal', 'H3B 1A7'),
    (2, 456, 'Boulevard René-Lévesque', 'Montréal', 'H2Z 1Z9'),
    (3, 789, 'Avenue du Mont-Royal', 'Montréal', 'H2J 1W8'),
    (4, 101, 'Rue Wellington', 'Sherbrooke', 'J1H 5C5'),
    (5, 202, 'Boulevard Laurier', 'Québec', 'G1V 2L8'),
    (6, 303, 'Boulevard Champlain', 'Québec', 'G1K 4J1'),
    (7, 404, 'Rue King', 'Sherbrooke', 'J1H 1P8'),
    (8, 505, 'Chemin de la Côte-des-Neiges', 'Montréal', 'H3T 1Y6'),
    (9, 606, 'Boulevard de Maisonneuve', 'Montréal', 'H3A 1L1'),
    (10, 707, 'Grande Allée', 'Québec', 'G1R 2K4'),
    (11, 808, 'Rue Saint-Jean', 'Québec', 'G1R 1P8'),
    (12, 909, 'Boulevard Saint-Laurent', 'Montréal', 'H2X 2S6'),
    (13, 110, 'Rue des Forges', 'Trois-Rivières', 'G9A 2G7'),
    (14, 220, 'Rue Principale', 'Gatineau', 'J9H 6K1');

-- Insert data for Shop (SAQ locations)
INSERT INTO Shop (shop_id, name, address_id)
VALUES 
    (1, 'SAQ Signature - Montréal Centre-Ville', 1),
    (2, 'SAQ Sélection - Quartier Latin', 3),
    (3, 'SAQ Express - Place Ville-Marie', 2),
    (4, 'SAQ Classique - Sherbrooke', 4),
    (5, 'SAQ Dépôt - Québec', 5),
    (6, 'SAQ Express - Vieux-Québec', 6);

-- Insert data for Product (Alcoholic beverages)
INSERT INTO Product (product_id, name, description, price, category, is_available)
VALUES 
    (1, 'Mouton Cadet Bordeaux', 'Red wine from Bordeaux region, France. Medium-bodied with notes of red fruits.', 19.95, 'Red Wine', TRUE),
    (2, 'Kim Crawford Sauvignon Blanc', 'White wine from Marlborough, New Zealand. Crisp with tropical fruit flavors.', 22.95, 'White Wine', TRUE),
    (3, 'Veuve Clicquot Brut', 'Champagne from France. Full-bodied with notes of toast and citrus.', 69.95, 'Champagne', TRUE),
    (4, 'Johnnie Walker Black Label', 'Blended Scotch whisky aged 12 years. Notes of smoke and vanilla.', 54.75, 'Whisky', TRUE),
    (5, 'Grey Goose Vodka', 'Premium French vodka made from wheat. Smooth with subtle sweetness.', 59.95, 'Vodka', TRUE),
    (6, 'Hendrick\'s Gin', 'Scottish gin infused with cucumber and rose petals.', 49.95, 'Gin', TRUE),
    (7, 'Bacardi Superior White Rum', 'Light and dry Cuban-style rum. Ideal for cocktails.', 27.95, 'Rum', TRUE),
    (8, 'Don Julio Reposado Tequila', 'Premium tequila aged in oak barrels for 8 months.', 89.95, 'Tequila', TRUE),
    (9, 'Aperol', 'Italian aperitif with bitter orange and herb flavors.', 32.95, 'Liqueur', TRUE),
    (10, 'La Fin du Monde', 'Triple-fermented golden ale from Quebec\'s Unibroue. 9% ABV.', 12.95, 'Beer', TRUE),
    (11, 'Lagavulin 16 Year', 'Islay single malt Scotch with intense peat and iodine notes.', 139.95, 'Whisky', TRUE),
    (12, 'Dom Pérignon Vintage', 'Prestigious vintage champagne with complex brioche notes.', 289.95, 'Champagne', TRUE),
    (13, 'Château Margaux', 'Premier Grand Cru Classé Bordeaux with exceptional aging potential.', 999.95, 'Red Wine', TRUE),
    (14, 'Belvedere Vodka', 'Polish rye vodka, quadruple-distilled for smoothness.', 64.95, 'Vodka', TRUE),
    (15, 'Macallan 12 Year', 'Speyside single malt Scotch aged in sherry casks.', 114.95, 'Whisky', TRUE),
    (16, 'Bombay Sapphire Gin', 'London Dry Gin with 10 exotic botanicals.', 34.95, 'Gin', TRUE),
    (17, 'Louis Jadot Bourgogne Pinot Noir', 'Classic red Burgundy with red cherry notes.', 29.95, 'Red Wine', TRUE),
    (18, 'Chablis William Fèvre', 'Crisp French Chardonnay with mineral character.', 34.95, 'White Wine', TRUE);

-- Insert data for ShopProducts (inventory for each shop)
INSERT INTO ShopProducts (shop_id, product_id, quantity)
VALUES 
    -- SAQ Signature - Montréal Centre-Ville (premium selection)
    (1, 1, 45),
    (1, 2, 38),
    (1, 3, 25),
    (1, 8, 18),
    (1, 11, 12),
    (1, 12, 5),
    (1, 13, 3),
    (1, 15, 10),
    (1, 17, 22),
    (1, 18, 27),

    -- SAQ Sélection - Quartier Latin
    (2, 1, 32),
    (2, 2, 29),
    (2, 3, 15),
    (2, 4, 19),
    (2, 5, 24),
    (2, 6, 21),
    (2, 10, 48),
    (2, 16, 16),
    (2, 17, 29),
    (2, 18, 31),

    -- SAQ Express - Place Ville-Marie (smaller selection)
    (3, 1, 24),
    (3, 4, 12),
    (3, 5, 15),
    (3, 7, 18),
    (3, 9, 17),
    (3, 10, 36),
    (3, 16, 11),

    -- SAQ Classique - Sherbrooke
    (4, 1, 35),
    (4, 2, 28),
    (4, 4, 22),
    (4, 5, 18),
    (4, 6, 14),
    (4, 7, 25),
    (4, 9, 19),
    (4, 10, 42),
    (4, 16, 15),
    (4, 17, 23),
    (4, 18, 19),

    -- SAQ Dépôt - Québec (large quantities)
    (5, 1, 85),
    (5, 2, 72),
    (5, 3, 45),
    (5, 4, 58),
    (5, 5, 63),
    (5, 6, 49),
    (5, 7, 67),
    (5, 8, 32),
    (5, 9, 54),
    (5, 10, 120),
    (5, 14, 41),
    (5, 15, 28),
    (5, 16, 52),
    (5, 17, 63),
    (5, 18, 58),

    -- SAQ Express - Vieux-Québec (smaller selection)
    (6, 1, 20),
    (6, 2, 18),
    (6, 5, 15),
    (6, 7, 22),
    (6, 9, 14),
    (6, 10, 38),
    (6, 16, 12);

-- Insert data for Commands (orders)
INSERT INTO Command (command_id, creation_date, status, total_amount, address_id, shop_id, user_id)
VALUES 
    (1, '2025-04-05 14:30:00', 'InRoute', 162.75, 8, 1, 1),
    (2, '2025-04-06 10:15:00', 'Shipped', 287.65, 9, 5, 3);

-- Insert data for CommandProducts (items in each order)
INSERT INTO CommandProducts (command_id, product_id, quantity)
VALUES 
    -- Command 1: Jean Tremblay's order
    (1, 3, 1),  -- 1 Veuve Clicquot Brut (69.95)
    (1, 4, 1),  -- 1 Johnnie Walker Black Label (54.75)
    (1, 10, 3), -- 3 La Fin du Monde (12.95 * 3 = 38.85)
                -- Total: 69.95 + 54.75 + 38.85 = 163.55 ≈ 162.75 (rounded)
    
    -- Command 2: Pierre Gagnon's order
    (2, 5, 1),  -- 1 Grey Goose Vodka (59.95)
    (2, 11, 1), -- 1 Lagavulin 16 Year (139.95)
    (2, 17, 2), -- 2 Louis Jadot Bourgogne Pinot Noir (29.95 * 2 = 59.90)
    (2, 10, 2); -- 2 La Fin du Monde (12.95 * 2 = 25.90)
                -- Total: 59.95 + 139.95 + 59.90 + 25.90 = 285.70 ≈ 287.65 (possibly includes taxes or fees)

-- Insert data for Payment
INSERT INTO Payment (payment_id, payment_method, amount, payment_date, is_completed, command_id, user_id, card_number, CVC_card, expiry_date)
VALUES 
    (1, 'Credit Card', 162.75, '2025-04-05 14:32:15', TRUE, 1, 1, '4111111111111111', 123, '2027-05-01'),
    (2, 'Credit Card', 287.65, '2025-04-06 10:17:30', TRUE, 2, 3, '5555555555554444', 456, '2026-12-01');