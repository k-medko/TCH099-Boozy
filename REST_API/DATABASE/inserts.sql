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