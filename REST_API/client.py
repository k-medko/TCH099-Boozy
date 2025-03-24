import requests
url_inscription = "http://localhost:5000/inscription_client"
url_connexion = "http://localhost:5000/connexion_client"

data = {
    "username": "emile",
    "password": "emiemi123"
}

response_inscription = requests.post(url_inscription, json=data)

print("Status Code:", response_inscription.status_code)
print("Réponse du serveur:", response_inscription.json())


response_connexion = requests.post(url_connexion, json=data)

print("Status Code:", response_connexion.status_code)
print("Réponse du serveur:", response_connexion.json())

# TEST SCRIPT IN TERMINAL> python REST_API\client.py