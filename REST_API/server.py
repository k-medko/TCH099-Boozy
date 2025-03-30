from flask import Flask, request, jsonify

app = Flask(__name__)


def verifier_auth(nom_utilisateur, mot_de_passe):
    if nom_utilisateur in DB_utilisateurs:
        return DB_utilisateurs[nom_utilisateur]["mot_de_passe"] == mot_de_passe
    return False

@app.route('/')
def accueil():
    return jsonify({"statut": "succès", "message": "Bienvenue!"})

@app.route('/inscription', methods=['POST'])
def inscription():
    d = request.json
    n, c, m = d.get('nom_utilisateur'), d.get('courriel'), d.get('mot_de_passe')
    if not n or not c or not m or n in DB_utilisateurs:
        return jsonify({"statut": "erreur", "message": "Inscription invalide"}), 400
    DB_utilisateurs[n] = {"courriel": c, "mot_de_passe": m, "rang": "client"}
    return jsonify({"statut": "succès", "message": f"Compte créé pour {n}"}), 201

@app.route('/connexion', methods=['POST'])
def connexion():
    d = request.json
    n, m = d.get('nom_utilisateur'), d.get('mot_de_passe')
    if verifier_auth(n, m):
        return jsonify({"statut": "succès", "message": f"Bonjour {n}!"})
    return jsonify({"statut": "erreur", "message": "Connexion invalide"}), 401

@app.route('/magasin', methods=['POST'])
def magasin():
    d = request.json
    mid = d.get('id_magasin')
    if mid in DB_magasins:
        info = DB_magasins[mid]
        return jsonify({"statut": "succès", "message": f"{info['nom']} à {info['adresse']}", "produits": info["produits"]})
    return jsonify({"statut": "erreur", "message": "Magasin invalide"}), 404

@app.route('/produits', methods=['POST'])
def liste_produits():
    d = request.json
    lp = d.get('liste_produits', [])
    rep = []
    for pid in lp:
        if pid in DB_produits:
            rep.append({pid: DB_produits[pid]})
    return jsonify({"statut": "succès", "produits": rep})

@app.route('/commande', methods=['POST'])
def creer_commande():
    d = request.json
    n, m = d.get('nom_utilisateur'), d.get('mot_de_passe')
    if not verifier_auth(n, m):
        return jsonify({"statut": "erreur", "message": "Authentification invalide"}), 401
    mid = d.get('id_magasin')
    lp = d.get('liste_produits', [])
    if mid not in DB_magasins:
        return jsonify({"statut": "erreur", "message": "Magasin inexistant"}), 400
    cid = compteur_commande[0]
    compteur_commande[0] += 1
    DB_commandes[cid] = {"utilisateur": n, "id_magasin": mid, "produits": lp, "statut": "en attente", "livreur": None}
    return jsonify({"statut": "succès", "message": f"Commande #{cid} créée", "commande_id": cid}), 201

@app.route('/commande/statut', methods=['GET'])
def statut_commande():
    cid = request.args.get('commande_id')
    if not cid or not cid.isdigit():
        return jsonify({"statut": "erreur", "message": "commande_id invalide"}), 400
    cid = int(cid)
    cmd = DB_commandes.get(cid)
    if not cmd:
        return jsonify({"statut": "erreur", "message": "Commande introuvable"}), 404
    return jsonify({"statut": "succès", "commande_id": cid, "statut_commande": cmd["statut"]})

@app.route('/commande/annuler', methods=['POST'])
def annuler_commande():
    d = request.json
    n, m, cid = d.get('nom_utilisateur'), d.get('mot_de_passe'), d.get('commande_id')
    if not verifier_auth(n, m):
        return jsonify({"statut": "erreur", "message": "Authentification invalide"}), 401
    if not cid or cid not in DB_commandes:
        return jsonify({"statut": "erreur", "message": "Commande introuvable"}), 404
    cmd = DB_commandes[cid]
    if cmd["utilisateur"] != n or cmd["statut"] != "en attente":
        return jsonify({"statut": "erreur", "message": "Annulation impossible"}), 403
    cmd["statut"] = "annulée"
    return jsonify({"statut": "succès", "message": f"Commande #{cid} annulée"})

@app.route('/admin', methods=['POST'])
def admin():
    d = request.json
    n, m = d.get('nom_utilisateur'), d.get('mot_de_passe')
    reqt = d.get('type_requete')
    if not verifier_auth(n, m) or DB_utilisateurs[n]["rang"] != "admin":
        return jsonify({"statut": "erreur", "message": "Accès refusé"}), 403
    return jsonify({"statut": "succès", "message": f"Requête admin '{reqt}' (non implémentée)"})

# ----------------------- Routes pour Livreur -----------------------
@app.route('/livreur/devenir', methods=['POST'])
def devenir_livreur():
    d = request.json
    n, m = d.get('nom_utilisateur'), d.get('mot_de_passe')
    if not verifier_auth(n, m):
        return jsonify({"statut": "erreur", "message": "Authentification invalide"}), 401
    if DB_utilisateurs[n]["rang"] == "livreur":
        return jsonify({"statut": "erreur", "message": "Vous êtes déjà livreur"}), 400
    DB_utilisateurs[n]["rang"] = "livreur"
    return jsonify({"statut": "succès", "message": f"{n} est maintenant livreur"})

@app.route('/livreur/accepter', methods=['POST'])
def accepter_commande():
    d = request.json
    n, m, cid = d.get('nom_utilisateur'), d.get('mot_de_passe'), d.get('commande_id')
    if not verifier_auth(n, m):
        return jsonify({"statut": "erreur", "message": "Authentification invalide"}), 401
    if DB_utilisateurs[n]["rang"] != "livreur":
        return jsonify({"statut": "erreur", "message": "Vous n'êtes pas livreur"}), 403
    if not cid or cid not in DB_commandes:
        return jsonify({"statut": "erreur", "message": "Commande introuvable"}), 404
    cmd = DB_commandes[cid]
    if cmd["statut"] != "en attente":
        return jsonify({"statut": "erreur", "message": "Commande déjà prise ou annulée"}), 403
    cmd["statut"] = "en livraison"
    cmd["livreur"] = n
    return jsonify({"statut": "succès", "message": f"Commande #{cid} acceptée par {n}"})

@app.route('/livreur/terminer', methods=['POST'])
def terminer_commande():
    d = request.json
    n, m, cid = d.get('nom_utilisateur'), d.get('mot_de_passe'), d.get('commande_id')
    if not verifier_auth(n, m):
        return jsonify({"statut": "erreur", "message": "Authentification invalide"}), 401
    if DB_utilisateurs[n]["rang"] != "livreur":
        return jsonify({"statut": "erreur", "message": "Vous n'êtes pas livreur"}), 403
    if not cid or cid not in DB_commandes:
        return jsonify({"statut": "erreur", "message": "Commande introuvable"}), 404
    cmd = DB_commandes[cid]
    if cmd["livreur"] != n or cmd["statut"] != "en livraison":
        return jsonify({"statut": "erreur", "message": "Impossible de terminer cette commande"}), 403
    cmd["statut"] = "livrée"
    return jsonify({"statut": "succès", "message": f"Commande #{cid} livrée par {n}"})

if __name__ == '__main__':
    app.run(debug=True)
