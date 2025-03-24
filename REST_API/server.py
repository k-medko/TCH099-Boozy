'''
 .d8888b.  8888888888 8888888b.  888     888 8888888888 8888888b.  
d88P  Y88b 888        888   Y88b 888     888 888        888   Y88b 
Y88b.      888        888    888 888     888 888        888    888 
 "Y888b.   8888888    888   d88P Y88b   d88P 8888888    888   d88P 
    "Y88b. 888        8888888P"   Y88b d88P  888        8888888P"  
      "888 888        888 T88b     Y88o88P   888        888 T88b   
Y88b  d88P 888        888  T88b     Y888P    888        888  T88b  
 "Y8888P"  8888888888 888   T88b     Y8P     8888888888 888   T88b
'''

from flask import Flask, request, jsonify

app = Flask(__name__)

DB_utilisateur = { # email | password | rank #
    "tristan":{"email":"testemail@gmail.com","password":"tritri123","rank":"admin"}
}

DB_magasins = {
    10001: {
        "name": "SAQ EXPRESS MONTRÉAL",
        "adress": "1060 rue Peel, Montréal",
        "products": [20001, 20003, 20005, 20007, 20009, 20011, 20012]
    },
    10002: {
        "name": "SAQ DÉPOT MONTRÉAL",
        "adress": "960 rue Williams, Montréal",
        "products": [20002, 20004, 20006, 20008, 20010, 20011, 20012, 20001]
    },
    10003: {
        "name": "SAQ SÉLECTION LONGUEUIL",
        "adress": "825 rue Saint-Laurent Ouest, Longueuil",
        "products": [20001, 20002, 20003, 20004, 20005, 20006]
    },
    10004: {
        "name": "SAQ CLASSIQUE LAVAL",
        "adress": "1999 Boulevard Saint-Martin Ouest, Laval",
        "products": [20007, 20008, 20009, 20010, 20011, 20012, 20002, 20004, 20006]
    }
}



DB_produits = {
    20001: {"name": "Smirnoff Ice", "alcohol": 7, "price": 2.99},
    20002: {"name": "Heineken", "alcohol": 5, "price": 2.49},
    20003: {"name": "Corona Extra", "alcohol": 4.5, "price": 2.79},
    20004: {"name": "Jack Daniel's", "alcohol": 40, "price": 29.99},
    20005: {"name": "Captain Morgan", "alcohol": 35, "price": 27.99},
    20006: {"name": "Bacardi Superior", "alcohol": 40, "price": 26.99},
    20007: {"name": "Grey Goose", "alcohol": 40, "price": 39.99},
    20008: {"name": "Absolut Vodka", "alcohol": 40, "price": 24.99},
    20009: {"name": "Baileys Irish Cream", "alcohol": 17, "price": 21.99},
    20010: {"name": "Jagermeister", "alcohol": 35, "price": 25.99},
    20011: {"name": "Red Wine Merlot", "alcohol": 13, "price": 15.99},
    20012: {"name": "White Wine Chardonnay", "alcohol": 12.5, "price": 14.99}
}


'''
dP 888888ba  .d88888b   a88888b.  888888ba  dP  888888ba  d888888P dP  .88888.  888888ba  
88 88    `8b 88.    "' d8'   `88  88    `8b 88  88    `8b    88    88 d8'   `8b 88    `8b 
88 88     88 `Y88888b. 88        a88aaaa8P' 88 a88aaaa8P'    88    88 88     88 88     88 
88 88     88       `8b 88         88   `8b. 88  88           88    88 88     88 88     88 
88 88     88 d8'   .8P Y8.   .88  88     88 88  88           88    88 Y8.   .8P 88     88 
dP dP     dP  Y88888P   Y88888P'  dP     dP dP  dP           dP    dP  `8888P'  dP     dP
'''
@app.route('/inscription', methods=['POST'])
def inscription():
    data = request.json
    username = data.get('username')
    email = data.get('email')
    password = data.get('password')
    if not username in DB_utilisateur and username != "" and email != "" and password != "":
        DB_utilisateur[username] = {"email":email,"password":password,"rank":"client"}
        return jsonify({"status": "success", "message": f"Création du compte pour {username} est complétée"})
    else:
        return jsonify({"status": "error", "message": "Inscription invalide"}), 401



'''
 a88888b.  .88888.  888888ba  888888ba   88888888b dP    dP dP  .88888.  888888ba  
d8'   `88 d8'   `8b 88    `8b 88    `8b  88        Y8.  .8P 88 d8'   `8b 88    `8b 
88        88     88 88     88 88     88 a88aaaa     Y8aa8P  88 88     88 88     88 
88        88     88 88     88 88     88  88        d8'  `8b 88 88     88 88     88 
Y8.   .88 Y8.   .8P 88     88 88     88  88        88    88 88 Y8.   .8P 88     88 
 Y88888P'  `8888P'  dP     dP dP     dP  88888888P dP    dP dP  `8888P'  dP     dP
 '''
@app.route('/connexion', methods=['POST'])
def connexion():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    print(username,password)
    if username in DB_utilisateur:
        properPassword = DB_utilisateur[username]["password"]
        if password == properPassword:
            return jsonify({"status": "success", "message": f"Bonjour {username}!"})
        else:
            return jsonify({"status": "error", "message": "Connexion invalide"}), 401




'''
8888ba.88ba   .d888888   .88888.   .d888888  .d88888b  dP 888888ba  .d88888b  
88  `8b  `8b d8'    88  d8'   `88 d8'    88  88.    "' 88 88    `8b 88.    "' 
88   88   88 88aaaaa88a 88        88aaaaa88a `Y88888b. 88 88     88 `Y88888b. 
88   88   88 88     88  88   YP88 88     88        `8b 88 88     88       `8b 
88   88   88 88     88  Y8.   .88 88     88  d8'   .8P 88 88     88 d8'   .8P 
dP   dP   dP 88     88   `88888'  88     88   Y88888P  dP dP     dP  Y88888P
'''
@app.route('/magasins', methods=['POST'])
def magasins():
    data = request.json
    idMagasin = data.get('magasin')
    if idMagasin in DB_magasins:
        magasin = DB_magasins[idMagasin]
        return jsonify({"status": "success", "message": f"Votre magasin se nomme {magasin["name"]} et se trouve au {magasin["adress"]}"})
    else:
        return jsonify({"status": "error", "message": "Magasin invalide"}), 401

'''
 888888ba   888888ba   .88888.  888888ba  dP     dP dP d888888P .d88888b  
 88    `8b  88    `8b d8'   `8b 88    `8b 88     88 88    88    88.    "' 
a88aaaa8P' a88aaaa8P' 88     88 88     88 88     88 88    88    `Y88888b. 
 88         88   `8b. 88     88 88     88 88     88 88    88          `8b 
 88         88     88 Y8.   .8P 88    .8P Y8.   .8P 88    88    d8'   .8P 
 dP         dP     dP  `8888P'  8888888P  `Y88888P' dP    dP     Y88888P
'''
@app.route('/produits', methods=['POST'])
def produits():
    data = request.json
    productList = data.get('products')
    response = []
    for product in productList:
        if product in DB_produits:
            response.append([product,DB_produits[product]])
    return jsonify({"status": "success", "message": response})

'''
888888ba   .d888888  d888888P  .d888888   888888ba   .d888888  .d88888b   88888888b 
88    `8b d8'    88     88    d8'    88   88    `8b d8'    88  88.    "'  88        
88     88 88aaaaa88a    88    88aaaaa88a a88aaaa8P' 88aaaaa88a `Y88888b. a88aaaa    
88     88 88     88     88    88     88   88   `8b. 88     88        `8b  88        
88    .8P 88     88     88    88     88   88    .88 88     88  d8'   .8P  88        
8888888P  88     88     dP    88     88   88888888P 88     88   Y88888P   88888888P
'''
#En attente du MYSQL

'''
 .d888888  888888ba  8888ba.88ba  dP 888888ba  
d8'    88  88    `8b 88  `8b  `8b 88 88    `8b 
88aaaaa88a 88     88 88   88   88 88 88     88 
88     88  88     88 88   88   88 88 88     88 
88     88  88    .8P 88   88   88 88 88     88 
88     88  8888888P  dP   dP   dP dP dP     dP
'''

@app.route('/admin', methods=['POST'])
def admin():
    data = request.json
    reqType = data.get("reqType") #modifyStore,deleteStore,modifyProduct,deleteProduct,modifyUser,deleteUser


'''
8888ba.88ba    .d888888  dP  888888ba  
88  `8b  `8b  d8'    88  88  88    `8b 
88   88   88  88aaaaa88a 88  88     88 
88   88   88  88     88  88  88     88 
88   88   88  88     88  88  88     88 
dP   dP   dP  88     88  dP  dP     dP
'''
if __name__ == '__main__':
    app.run(debug=True)




