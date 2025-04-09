-- Insert real Montreal addresses for the clients
INSERT INTO AddressLine (civic, apartment, street, city, postal_code)
VALUES 
  (123, '1A', 'Rue Sainte-Catherine Ouest', 'Montreal', 'H3B 1A7'), -- 1
  (456, '2B', 'Avenue du Parc', 'Montreal', 'H2V 4E6'); -- 2

-- Insert 6 admins (last_name = 'admin', address_id = NULL, total_earnings = NULL)
INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, total_earnings)
VALUES
  ('tristan@boozy.com', 'adminpass', 'admin', 'Tristan', '1111111111', NULL, 'admin', NULL, NULL),
  ('kahina@boozy.com', 'adminpass', 'admin', 'Kahina', '3333333333', NULL, 'admin', NULL, NULL),
  ('shawn@boozy.com', 'adminpass', 'admin', 'Shawn', '2222222222', NULL, 'admin', NULL, NULL),
  ('emile@boozy.com', 'adminpass', 'admin', 'Emile', '4444444444', NULL, 'admin', NULL, NULL),
  ('edwar@boozy.com', 'adminpass', 'admin', 'Edwar', '5555555555', NULL, 'admin', NULL, NULL),
  ('macky@boozy.com', 'adminpass', 'admin', 'Macky', '6666666666', NULL, 'admin', NULL, NULL);

-- Insert 2 clients with addresses (using the newly inserted address_id 1 and 2)
INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, total_earnings)
VALUES
  ('client1@boozy.com', 'custpass', 'Smith', 'Alice', '7777777777', 1, 'client', NULL, NULL),
  ('client2@boozy.com', 'custpass', 'Johnson', 'Bob', '8888888888', 2, 'client', NULL, NULL);

-- Insert 2 carriers with actual total_earnings and sample license plates (address_id = NULL)
INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, car_brand, total_earnings)
VALUES
  ('carrier1@boozy.com', 'delivpass', 'Brown', 'Charlie', '9999999999', NULL, 'carrier', 'AB-123-CD','Ford Focus', 150.00),
  ('carrier2@boozy.com', 'delivpass', 'Prince', 'Diana', '1010101010', NULL, 'carrier', 'EF-456-GH','Honda Civic', 200.00);

-- Insert addresses for 6 SAQ stores in Montreal
INSERT INTO AddressLine (civic, street, city, postal_code)
VALUES
  (155, 'Av. Atwater', 'Montreal', 'H3J 2J4'), -- 3
  (1176, 'Sainte-Catherine', 'Montreal', 'H3B 1K1'), -- 4
  (1450, 'De la Montagne', 'Montreal', 'HH3G 1Z5'), -- 5
  (677, 'Saint-Catherine', 'Montreal', 'H3B 5K4'), -- 6
  (425, 'Blvd. De Maisonneuve Ouest', 'Montreal', 'H3A 3G5'), -- 7
  (231, 'Peel', 'Montreal', 'H3C 2G6'); -- 8

-- Insert 6 SAQ stores linked to the addresses above (assuming auto-increment produces address_id 1 to 6)
INSERT INTO Shop (name, address_id)
VALUES
  ('SAQ Sélection-Marché Atwater', 3),
  ('SAQ Express', 4),
  ('SAQ Sélection-De la Montagne', 5),
  ('SAQ-Centre Eaton', 6),
  ('SAQ Sélection', 7),
  ('SAQ', 8);

INSERT INTO Product (name, description, price, category, alcohol) VALUES
('Monte Real Reserva Rioja 2020', '« Puissant, ce vin profite d''une longue macération qui lui confère structure et prestance. Profitant d''un élevage en fût de chêne américain pour une période de 24 à 30 mois, voilà une cuvée qui plaira à l''amateur de vin corsés. »', 24.70, 'Vin rouge', 14),
('Domaine Perraud Mâcon Villages 2023', '« Jean-Christophe Perraud réalise un rêve d''enfant lorsqu''en 2005, il reprend le domaine familial situé dans le sud de la Bourgogne. Avec ses arômes séduisants de pomme et d''amande, son mâcon-villages captive! »', 24.00, 'Vin blanc', 12.5),
('Vignoble Rivière du Chêne, Gabrielle 2023', '« Cet assemblage aux accents de petits fruits rouges et pourvu d''une touche florale a été nommé par Daniel Lalande, propriétaire du domaine, en l''honneur de sa fille. Sec et vivifiant, voilà une cuvée passe-partout qui saura plaire autant à l''apéro qu''avec une salade de fruits de mer. »', 17.55, 'Vin rosé', 12.5),
('Aberfeldy 12 Single Malt Scotch Whisky', '« Vous cherchez un whisky moelleux et suave? Vous avez trouvé! Le scotch single malt 12 ans Aberfeldy vous séduira avec ses notes mielleuses. Distillé en alambics de cuivre, il est produit de façon artisanale par la même famille depuis plus d''un siècle, puis vieilli à la perfection. »', 74.75, 'Whisky écossais', 40),
('Aperol', '« Cet aperitivo centenaire trouve son identité dans les différentes herbes et oranges amères qui le composent. Combiné à du prosecco dans un cocktail de type spritz, il ajoutera une amertume unique équilibrée par une séduisante douceur. »', 27.70, 'Apéritif à base de gentiane', 11),
('3 Fonteinen Cuvée Armand & Gaston Oude Geuze', '« Bière de type Oude Geuze, caractérisée par une acidité marquée, avec des notes de sésame, de zeste de citron et une touche presque saline. Le nom Cuvée Armand et Gaston rend hommage aux fondateurs de la brasserie. Elle est prisée des connaisseurs pour sa capacité à se bonifier avec le temps. »', 29.30, 'Bière dorée de type spontanée', 6),
('Domaine de Lavoie Bulles d’Automne', '« Un cidre mousseux gourmand qui dégage des arômes de pommes caramélisées. Une pointe d''acidité vient balancer la finale. »', 16.55, 'Cidre mousseux', 8),
('Zubrówka (Vodka à l’herbe de bison)', '« Cette vodka polonaise est faite avec de l''herbe de bison qui enveloppe les papilles avec douceur. Elle se prend bien seule et est un bon choix pour essayer quelque chose de nouveau. »', 30.50, 'Vodka aromatisée (Pologne)', 40),
('Diplomatico Reserva Exclusiva', '« Excellent rhum doté d''un goût complexe. À déguster seul sur glace ! »', 67.75, 'Rhum (Venezuela)', 40),
('19 Crimes Cabernet-Sauvignon', '« Très bon cabernet-sauvignon pour accompagner une viande rouge. »', 20.65, 'Vin rouge', 14.5),
('Sortilège Original', '« Une belle liqueur au sirop d’érable du Québec, qui marie à la perfection la richesse du whisky canadien et la douceur de l’érable. Un incontournable pour les dents sucrées ! »', 34.25, 'Liqueur d’érable (whisky canadien)', 30),
('Veuve Clicquot Ponsardin Brut', '« Élaboré selon la méthode champenoise traditionnelle, ce champagne offre des arômes briochés et fruités. Sa bouche vive et équilibrée saura égayer vos célébrations. »', 79.75, 'Champagne', 12),
('Tanaka 1789 X Chartier Junmai Daiginjo', '« Un saké junmai daiginjo d''une grande finesse, aux notes florales et de melon, obtenu par un polissage du riz à 45 %. Idéal servi frais pour accompagner sushis et poissons. »', 39.50, 'Saké (Japon)', 15);

INSERT INTO ShopProduct (shop_id, product_id, quantity) VALUES
-- Store 1 (original product_ids: 1, 3, 5, 7, 9, 11, 13, 15, 17, 19)
(1, 1, 12),       -- 1   → ((1-1)%13)+1 = 1
(1, 3, 20),       -- 3   → 3
(1, 5, 15),       -- 5   → 5
(1, 7, 25),       -- 7   → 7
(1, 9, 30),       -- 9   → 9
(1, 11, 18),      -- 11  → 11
(1, 13, 22),      -- 13  → 13
(1, 2, 17),       -- 15  → ((15-1)=14, 14 mod13 = 1, then +1 = 2)
(1, 4, 27),       -- 17  → ((17-1)=16, 16 mod13 = 3, then +1 = 4)
(1, 6, 14),       -- 19  → ((19-1)=18, 18 mod13 = 5, then +1 = 6)

-- Store 2 (original product_ids: 2, 4, 6, 8, 10, 12, 14, 16, 18, 20)
(2, 2, 13),       -- 2   → 2
(2, 4, 21),       -- 4   → 4
(2, 6, 16),       -- 6   → 6
(2, 8, 24),       -- 8   → 8
(2, 10, 31),      -- 10  → 10
(2, 12, 19),      -- 12  → 12
(2, 1, 28),       -- 14  → ((14-1)=13, 13 mod13 = 0, then +1 = 1)
(2, 3, 15),       -- 16  → ((16-1)=15, 15 mod13 = 2, then +1 = 3)
(2, 5, 33),       -- 18  → ((18-1)=17, 17 mod13 = 4, then +1 = 5)
(2, 7, 22),       -- 20  → ((20-1)=19, 19 mod13 = 6, then +1 = 7)

-- Store 3 (original product_ids: 21, 22, 23, 24, 25, 26, 27, 28, 29, 30)
(3, 8, 11),       -- 21  → ((21-1)=20, 20 mod13 = 7, then +1 = 8)
(3, 9, 23),       -- 22  → ((22-1)=21, 21 mod13 = 8, then +1 = 9)
(3, 10, 17),      -- 23  → ((23-1)=22, 22 mod13 = 9, then +1 = 10)
(3, 11, 29),      -- 24  → ((24-1)=23, 23 mod13 = 10, then +1 = 11)
(3, 12, 14),      -- 25  → ((25-1)=24, 24 mod13 = 11, then +1 = 12)
(3, 13, 20),      -- 26  → ((26-1)=25, 25 mod13 = 12, then +1 = 13)
(3, 1, 32),       -- 27  → ((27-1)=26, 26 mod13 = 0, then +1 = 1)
(3, 2, 18),       -- 28  → ((28-1)=27, 27 mod13 = 1, then +1 = 2)
(3, 3, 26),       -- 29  → ((29-1)=28, 28 mod13 = 2, then +1 = 3)
(3, 4, 21),       -- 30  → ((30-1)=29, 29 mod13 = 3, then +1 = 4)

-- Store 4 (original product_ids: 1, 4, 7, 10, 13, 16, 19, 22, 25, 28)
(4, 1, 19),       -- 1   → 1
(4, 4, 27),       -- 4   → 4
(4, 7, 16),       -- 7   → 7
(4, 10, 20),      -- 10  → 10
(4, 13, 24),      -- 13  → 13
(4, 3, 17),       -- 16  → ((16-1)=15, 15 mod13 = 2, then +1 = 3)
(4, 6, 30),       -- 19  → ((19-1)=18, 18 mod13 = 5, then +1 = 6)
(4, 9, 15),       -- 22  → ((22-1)=21, 21 mod13 = 8, then +1 = 9)
(4, 12, 21),      -- 25  → ((25-1)=24, 24 mod13 = 11, then +1 = 12)
(4, 2, 18),       -- 28  → ((28-1)=27, 27 mod13 = 1, then +1 = 2)

-- Store 5 (original product_ids: 2, 5, 8, 11, 14, 17, 20, 23, 26, 29)
(5, 2, 14),       -- 2   → 2
(5, 5, 22),       -- 5   → 5
(5, 8, 18),       -- 8   → 8
(5, 11, 26),      -- 11  → 11
(5, 1, 17),       -- 14  → ((14-1)=13, 13 mod13 = 0, then +1 = 1)
(5, 4, 19),       -- 17  → ((17-1)=16, 16 mod13 = 3, then +1 = 4)
(5, 7, 27),       -- 20  → ((20-1)=19, 19 mod13 = 6, then +1 = 7)
(5, 10, 21),      -- 23  → ((23-1)=22, 22 mod13 = 9, then +1 = 10)
(5, 13, 23),      -- 26  → ((26-1)=25, 25 mod13 = 12, then +1 = 13)
(5, 3, 15),       -- 29  → ((29-1)=28, 28 mod13 = 2, then +1 = 3)

-- Store 6 (original product_ids: 3, 6, 9, 12, 15, 18, 21, 24, 27, 30)
(6, 3, 16),       -- 3   → 3
(6, 6, 20),       -- 6   → 6
(6, 9, 25),       -- 9   → 9
(6, 12, 17),      -- 12  → 12
(6, 2, 23),       -- 15  → ((15-1)=14, 14 mod13 = 1, then +1 = 2)
(6, 5, 28),       -- 18  → ((18-1)=17, 17 mod13 = 4, then +1 = 5)
(6, 8, 19),       -- 21  → ((21-1)=20, 20 mod13 = 7, then +1 = 8)
(6, 11, 22),      -- 24  → ((24-1)=23, 23 mod13 = 10, then +1 = 11)
(6, 1, 15),       -- 27  → ((27-1)=26, 26 mod13 = 0, then +1 = 1)
(6, 4, 18);       -- 30  → ((30-1)=29, 29 mod13 = 3, then +1 = 4)
