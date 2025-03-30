"""
88888888888 8888888888 .d8888b. 88888888888 
    888     888       d88P  Y88b    888     
    888     888       Y88b.         888     
    888     8888888    "Y888b.      888     
    888     888           "Y88b.    888     
    888     888             "888    888     
    888     888       Y88b  d88P    888     
    888     8888888888 "Y8888P"     888
Script de test pour simuler la création et le suivi d'une commande.
"""
import requests, time

base_url = "https://issue-volleyball-versus-scroll.trycloudflare.com/"
url_inscription = base_url + "inscription"
url_connexion = base_url + "connexion"
url_commande = base_url + "commande"
url_statut = base_url + "commande/statut"
url_annuler = base_url + "commande/annuler"
url_devenir_livreur = base_url + "livreur/devenir"
url_accepter = base_url + "livreur/accepter"
url_terminer = base_url + "livreur/terminer"

client_data_inscription = {
    "nom_utilisateur": "paul",
    "courriel": "paul@gmail.com",
    "mot_de_passe": "paul123"
}
r = requests.post(url_inscription, json=client_data_inscription)
print("Inscription client:", r.status_code, r.json())

client_data_connexion = {
    "nom_utilisateur": "paul",
    "mot_de_passe": "paul123"
}
r = requests.post(url_connexion, json=client_data_connexion)
print("Connexion client:", r.status_code, r.json())

commande_data = {
    "nom_utilisateur": "paul",
    "mot_de_passe": "paul123",
    "id_magasin": 10001,
    "liste_produits": [20001, 20003]
}
r = requests.post(url_commande, json=commande_data)
print("Création commande:", r.status_code, r.json())
commande_id = None
if r.status_code == 201:
    commande_id = r.json().get("commande_id")

if commande_id:
    for i in range(3):
        time.sleep(5)
        st = requests.get(url_statut, params={"commande_id": commande_id})
        print("Vérification statut", i+1, ":", st.status_code, st.json())

livreur_data_inscription = {
    "nom_utilisateur": "kevin",
    "courriel": "kevin@gmail.com",
    "mot_de_passe": "kevkev123"
}
r = requests.post(url_inscription, json=livreur_data_inscription)
print("Inscription livreur:", r.status_code, r.json())

livreur_data_connexion = {
    "nom_utilisateur": "kevin",
    "mot_de_passe": "kevkev123"
}
r = requests.post(url_connexion, json=livreur_data_connexion)
print("Connexion livreur:", r.status_code, r.json())

devenir_data = {
    "nom_utilisateur": "kevin",
    "mot_de_passe": "kevkev123"
}
r = requests.post(url_devenir_livreur, json=devenir_data)
print("Devenir livreur:", r.status_code, r.json())

if commande_id:
    accepter_data = {
        "nom_utilisateur": "kevin",
        "mot_de_passe": "kevkev123",
        "commande_id": commande_id
    }
    r = requests.post(url_accepter, json=accepter_data)
    print("Accepter commande:", r.status_code, r.json())
    for i in range(2):
        time.sleep(5)
        st = requests.get(url_statut, params={"commande_id": commande_id})
        print("Vérification statut (livreur)", i+1, ":", st.status_code, st.json())

    terminer_data = {
        "nom_utilisateur": "kevin",
        "mot_de_passe": "kevkev123",
        "commande_id": commande_id
    }
    r = requests.post(url_terminer, json=terminer_data)
    print("Terminer commande:", r.status_code, r.json())
    st = requests.get(url_statut, params={"commande_id": commande_id})
    print("Vérification statut final:", st.status_code, st.json())
