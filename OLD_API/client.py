import requests

BASE_URL = "https://attitude-queen-striking-bookmark.trycloudflare.com/createUser"

# USERS - Create a new user
def create_user(email, password, lastName, firstName, phoneNumber, userType, licensePlate=None):
    url = f"{BASE_URL}/createUser"
    payload = {
        "email": email,
        "password": password,
        "lastName": lastName,
        "firstName": firstName,
        "phoneNumber": phoneNumber,
        "userType": userType,
    }
    if userType == "deliverer" and licensePlate:
        payload["licensePlate"] = licensePlate
    response = requests.post(url, json=payload)
    print("Create User Response:", response.status_code, response.json())

# USERS - Connect (log in) a user
def connect_user(email, password):
    url = f"{BASE_URL}/connectUser"
    payload = {"email": email, "password": password}
    response = requests.post(url, json=payload)
    print("Connect User Response:", response.status_code, response.json())

# STORES - Get store(s)
def get_stores(storeId=None):
    url = f"{BASE_URL}/getStores"
    params = {}
    if storeId:
        params["storeId"] = storeId
    response = requests.get(url, params=params)
    print("Get Stores Response:", response.status_code, response.json())

# PRODUCTS - Get product(s)
def get_products(productId=None):
    url = f"{BASE_URL}/getProducts"
    params = {}
    if productId:
        params["productId"] = productId
    response = requests.get(url, params=params)
    print("Get Products Response:", response.status_code, response.json())

# AVAILABILITY - Get product availability
def get_availability(storeId=None, productId=None):
    url = f"{BASE_URL}/getAvailability"
    params = {}
    if storeId:
        params["storeId"] = storeId
    if productId:
        params["productId"] = productId
    response = requests.get(url, params=params)
    print("Get Availability Response:", response.status_code, response.json())

if __name__ == '__main__':
    # Test creating users
    create_user("custome1r@example.com", "cust123", "Doe", "John", "111-222-3333", "customer")
    create_user("deliver1er@example.com", "deliv123", "Smith", "Alice", "444-555-6666", "deliverer", "XYZ789")
    
    # Test connecting users
    connect_user("custome1r@example.com", "cust123")
    connect_user("deliver1er@example.com", "deliv123")
    
    # Test retrieving data
    get_stores()
    get_products()
    get_availability()
