'''
88888888888 8888888888 .d8888b. 88888888888 
    888     888       d88P  Y88b    888     
    888     888       Y88b.         888     
    888     8888888    "Y888b.      888     
    888     888           "Y88b.    888     
    888     888             "888    888     
    888     888       Y88b  d88P    888     
    888     8888888888 "Y8888P"     888
'''


import requests
url_inscription = "http://localhost:5000/inscription"
url_connexion = "http://localhost:5000/connexion"

data = {
    "username": "emile",
    "email": "emile@gmail.com",
    "password": "emiemi123"
}

response_inscription = requests.post(url_inscription, json=data)

print("Status Code:", response_inscription.status_code)
print("Réponse du serveur:", response_inscription.json())


response_connexion = requests.post(url_connexion, json=data)

print("Status Code:", response_connexion.status_code)
print("Réponse du serveur:", response_connexion.json())

# TEST SCRIPT IN TERMINAL> python REST_API\client.py

magasin_data = {"magasin":10001}
print(requests.post("http://localhost:5000/magasins", json=data))