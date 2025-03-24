from flask import Flask, request, jsonify

app = Flask(__name__)

connection_list = {
    "tristan": "tritri123",
    "shawn":   "shosho123"
}


@app.route('/inscription_client', methods=['POST'])
def inscription_client():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    if not username in connection_list:
        connection_list[username] = password
        return jsonify({"status": "success", "message": "Compte créé"})
    else:
        return jsonify({"status": "error", "message": "Compte existe déjà"}), 401

@app.route('/connexion_client', methods=['POST'])
def connexion_client():
    data = request.json
    username = data.get('username')
    password = data.get('password')
    print(username,password)
    if username in connection_list:
        properPassword = connection_list[username]
        if password == properPassword:
            return jsonify({"status": "success", "message": f"Bonjour {username}!"})
        else:
            return jsonify({"status": "error", "message": "Connexion invalide"}), 401
'''
@app.route('/connexion_livreur')
def connexion_livreur():
    return 'AAAAAA'

@app.route('/connexion_admin')
def connexion_admin():
    return 'AAAAAA'
'''
if __name__ == '__main__':
    app.run(debug=True)
