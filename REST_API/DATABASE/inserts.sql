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
  (231, 'Peel', 'Montreal', 'H3C 2G6'), -- 8
  (2685, 'Masson', 'Montreal', 'H1Y 1W3'), -- 9
  (2100, 'Beaubien', 'Montreal', 'H2G 1M6'); -- 10




-- Insert 6 SAQ stores linked to the addresses above (assuming auto-increment produces address_id 1 to 6)
INSERT INTO Shop (name, address_id)
VALUES
  ('SAQ Sélection-Marché Atwater', 3),
  ('SAQ Express', 4),
  ('SAQ Sélection-De la Montagne', 5),
  ('SAQ Centre Eaton', 6),
  ('SAQ Sélection', 7),
  ('SAQ Griffintown', 8),
  ('SAQ Classique Rue Masson', 9),
  ('SAQ Express-Beaubien', 10);





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
('Tanaka 1789 X Chartier Junmai Daiginjo', '« Un saké junmai daiginjo d''une grande finesse, aux notes florales et de melon, obtenu par un polissage du riz à 45 %. Idéal servi frais pour accompagner sushis et poissons. »', 39.50, 'Saké (Japon)', 15),
('Mouton Cadet Bordeaux', '« Mouton Cadet demeure à ce jour une valeur sûre pour tout amateur en quête d’’un rouge d’’une grande polyvalence à table. »', 18.20, 'Vin rouge', 14.5),
('Salentein Reserve Malbec 2021', '« Excellent malbec argentin à 20$. Goûteux mais assez souple avec de légères notes d''épices. Parfait pour les viandes rouges! »', 19.95, 'Vin rouge', 14),
('Ruffino Chianti', '« Un bon petit vin passe-partout, avec de beaux arômes fruités. Il accompagne bien les pâtes à la sauce tomate et le poulet grillé. »', 15.95, 'Vin rouge', 13),
('Kim Crawford Sauvignon Blanc Marlborough', '« Un des vins blancs chouchous des Québécois et du réputé magazine américain Wine Spectator. Un match parfait avec les rouleaux de printemps, les poissons et les fruits de mer. »', 20.70, 'Vin blanc', 12.5),
('Château d’Esclans Whispering Angel 2023', '« Côtes de Provence produit au Château d''Esclans, Whispering Angel est un rosé de grenache principalement. Vendangés la nuit et le matin uniquement pour une fraîcheur optimale, les raisins sont ensuite triés par optique, permettant de retirer les grains dont la qualité est jugée insuffisante. »', 30.75, 'Vin rosé', 13),
('Veuve Clicquot Ponsardin Brut', '« Un champagne classique qui fait toujours plaisir. Notes d''amande grillée, de brioche et de vanille. Une bouche ample et beaucoup de fraîcheur. »', 86.25, 'Champagne', 12),
('Smirnoff No. 21', '« Appréciée à travers le monde, la vodka Smirnoff No. 21 est un classique de longue date. Distillée trois fois et filtrée 10 fois pour une douceur exceptionnelle, c’’est un passe-partout idéal pour vos cocktails préférés. L’’entreprise est aussi soucieuse de profiter de son rayonnement pour mettre de l’’avant des valeurs de diversité, d’égalité et d’’inclusion. Car comme elle l’’affirme, “On ne devient pas la vodka numéro 1 au monde sans inviter tout le monde à la fête.” »', 27.30, 'Vodka', 40),
('Ungava', '« Ungava révèle un style à la fois vif, moelleux, frais et floral. Donnant à ce gin sa couleur particulière, la baie d''églantier est associée à la camarine noire, le thé du Labrador, la ronce petit-mûrier, le mélange de l''Arctique et le genévrier nordique afin de former une sélection unique d''herbes et de baies, 100 % naturelles procurant à ce gin ses arômes envoûtants. »', 38.75, 'Dry gin', 40),
('El Dorado 12 ans Demerara', '« Si tu cherches à découvrir un rhum bien âgé avec des saveurs complexes et une belle rondeur, El Dorado 12 ans est une excellente option. C’’est un rhum très riche en saveurs, on perçoit des arômes de vanille, de miel et de fruits mûrs. »', 41.75, 'Rhum brun', 40),
('Ardbeg 10 Ans Islay Single Malt', '« Puissant et tourbé, ce whisky iconique d’’Islay offre une explosion de fumée, de sel et d’’épices. Pour les amateurs de sensations fortes ! »', 119.75, 'Whisky écossais', 46),
('Courvoisier V.S.O.P.', '« Plus mature, avec une belle richesse aromatique, mariant fruits secs, noisette et épices douces. »', 102.75, 'Cognac', 40),
('Cherry River Tequila Silver', '« La Tequila Cherry River est produite à partir de sirop cru d’’agave bleu de la région de Jalisco au Mexique. Ce sirop tiré de la sève du cœur du plant est ensuite fermenté pour être ensuite distillé dans un alambic en cuivre afin d’’affiner sa saveur. Il en ressort un caractère aromatique propre au Mexique avec de discrètes notes de poivre, d’’agrumes et d’’herbes. Élixir riche et sophistiqué, la tequila se déguste seule, mais elle se prête aussi parfaitement à l’’élaboration de cocktails sensationnels. »', 41.75, 'Téquila (blanco)', 40),
('Baileys l’Originale', '« Renommé pour ses arômes riches et envoûtants, le classique Baileys est toujours fièrement produit en Irlande avec de la crème, du whiskey, du cacao et de la vanille. Volupté incarnée, cette crème ravira ceux à la dent sucrée. »', 34.25, 'Boisson à la crème (crème irlandaise)', 17),
('Coureur des Bois Crème d’érable', '« Faite de sirop d’’érable québécois pur de première qualité, de la plus fraîche des crèmes ainsi que d’’un assemblage d’’alcool de grains et de rhum, la crème d’’érable Coureur des Bois est un produit authentiquement d’’ici. Les arômes de cacao, de cannelle et d’’amaretto se marient avec l’’érable, créant une puissante synergie de saveurs, qui se prolonge en une finale cacaotée et s’’exprime dans une texture vaporeuse et aérienne. »', 35.00, 'Boisson à la crème (érable)', 15),
('Chimay Bleue Grande Réserve', '« Les moines trappistes cisterciens de Chimay brassent de la bière depuis 1862. La Chimay bleue, ou “Grande Réserve”, est brassée depuis 1954, alors qu''on voulait créer une bière de Noël. Avec ses accents de fruits confits, de caramel et d''épices, c''est une des bières belges les plus prisées pour sa complexité et sa richesse, et elle peut se bonifier sur quelques années lorsque conservée dans de bonnes conditions. »', 5.65, 'Bière brune de type Ale', 9),
('Lindemans Kriek Lambic', '« Cette Kriek belge élaborée à partir d’’une base de lambic à laquelle est ajouté 25 % de filtrat de griottes fraîches, marie acidité et douceur à la perfection. Légèrement trouble et d’’un rouge éclatant, la robe laisse place à une bouche ronde et riche, marquée par la cerise confite et le bonbon acidulé. »', 6.25, 'Bière rouge de type Spontanée', 3.5),
('Famille Fabre Sauvignon Blanc/Viognier 2022', '« Belle longueur en bouche avec des notes de fruits tropicaux tels la mangue, le fruit de la passion, fleur blanche et légèrement herbacé. Belle découverte! »', 14.70, 'Vin blanc', 13),
('Château Ste. Michelle Riesling Columbia Valley 2022', '« Ce riesling d''origine américaine dévoile des arômes de citron, de pêche blanche et une pointe minérale. Idéal à l''apéritif comme avec des poissons grillés. »', 18.50, 'Vin blanc', 12),
('Orin Swift Abstract Red Blend 2021', '« Assemblage californien audacieux mêlant cabernet sauvignon, petit verdot et syrah, avec des notes de mûre et de cacao. Puissant et élégant. »', 45.30, 'Vin rouge', 14.5),
('La Choulette Ambrée', '« Bière ambrée nordiste, relevée de notes de caramel, de malt toasté et d’une légère amertume. À déguster à 10 °C. »', 3.50, 'Bière ambrée', 6),
('Goose Island IPA', '« India Pale Ale américaine aux arômes d’agrumes et de résine de pin, offrant une amertume franche et une finale sèche. »', 4.75, 'Bière IPA', 5.9),
('Kronenbourg 1664 Blanc', '« Bière blanche française légère, parfumée à la coriandre et au zeste d’orange, parfaite pour l’été. »', 4.25, 'Bière blanche', 5),
('Domaine Tempier Bandol Rosé 2023', '« Icône de Provence, ce rosé dévoile des arômes de fraise des bois et d’agrumes, avec une belle structure. »', 36.50, 'Vin rosé', 13),
('The Botanist Islay Dry Gin', '« Gin écossais distillé à partir de 22 plantes locales, offrant une complexité herbacée et une belle fraîcheur en bouche. »', 49.95, 'Dry gin', 46),
('Belvedere Vodka', '« Vodka polonaise de qualité supérieure, distillée à partir de seigle Dankowskie, d’une grande pureté et d’une finale soyeuse. »', 39.50, 'Vodka', 40),
('Clase Azul Plata Tequila', '« Tequila blanco mexicaine premium, embouteillée dans une jarre artisanale peinte à la main, aux notes d’agave frais et d’agrumes. »', 89.95, 'Téquila (blanco)', 40),
('Hibiki Harmony Japanese Blend', '« Whisky japonais harmonieux, assemblage de malts et de grain, dévoilant des notes de fleur d’’oranger et de miel. »', 59.75, 'Whisky japonais', 43),
('Rémy Martin XO', '« Cognac intense et velouté, mariant fruits confits, épices douces et une pointe de bois de santal. »', 139.75, 'Cognac', 40),
('Taylors Vintage Port 2017', '« Porto millésimé riche et tannique, aux arômes de fruits noirs, de cacao et d’épices, à carafer avant dégustation. »', 49.80, 'Porto', 20),
('Taylor Fladgate 10 Years Tawny Port', '« Porto tawny vieilli 10 ans en fûts, offrant des notes de noix, de caramel et de figue séchée. »', 35.25, 'Porto', 20),
('Martini & Rossi Rosso', '« Vermouth italien classique, aux arômes d’herbes et d’épices, parfait en cocktail ou sur glace avec une tranche d’orange. »', 19.50, 'Vermouth', 15),
('Campari', '« Apéritif italien à base de plantes et d’agrumes amers, indispensable pour vos Negroni et Spritz. »', 28.60, 'Apéritif', 25),
('Jägermeister', '« Liqueur allemande à base de 56 plantes, épices et racines, réputée pour sa douceur, ses notes de réglisse et une finale herbacée. »', 32.40, 'Liqueur', 35),
('Grey Goose L’Orange', '« Vodka aromatisée à l’orange, distillée en France, idéale pour apporter une touche fruitée à vos cocktails. »', 34.50, 'Vodka aromatisée', 40),
('St-Germain', '« Liqueur de fleurs de sureau récoltées à la main en France, révélant des arômes délicats et gourmands. »', 29.99, 'Liqueur de fleur de sureau', 20),
('Gin Vine Floraison Gin', '« Gin français innovant distillé à base de raisins de vigne et de fleurs de vigne, frais et floral. »', 44.50, 'Gin', 40),
('Eric Bordelet Poiré Granit', '« Poiré artisanal élaboré à partir de poires du verger familial, offrant des notes beurrées et une belle fraîcheur. »', 24.80, 'Cidre de poire', 6.5);


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
