import requests

url = "http://4.172.255.120:5000/createUser"

payload = {
    "email": "deliverer@example.com",
    "password": "securePass123",
    "lastName": "Smith",
    "firstName": "Jane",
    "phoneNumber": "555-1234",
    "userType": "deliverer",        # Must be one of: customer, deliverer, admin
    "licensePlate": "XYZ123",       # Required for deliverer
    "carBrand": "Toyota",           # Optional field for deliverer
    "address": {                    # Optional address in dictionary format
        "civic": "100",
        "apartment": "5B",
        "street": "Main Street",
        "city": "Townsville",
        "postalCode": "98765"
    }
}

response = requests.post(url, json=payload)
print("Response text:", response.text)
