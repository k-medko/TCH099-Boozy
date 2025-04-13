import requests
import random
import string
from datetime import datetime

BASE_URL = "http://4.172.252.189:5000"

# ===== Random Data Generators =====
def random_email():
    return f"test_{''.join(random.choices(string.ascii_lowercase, k=8))}@example.com"

def random_name(prefix):
    return f"{prefix}_{''.join(random.choices(string.ascii_lowercase, k=6))}"

def random_phone():
    return ''.join(random.choices(string.digits, k=10))

def random_license_plate():
    letters = lambda n: ''.join(random.choices(string.ascii_uppercase, k=n))
    digits = lambda n: ''.join(random.choices(string.digits, k=n))
    return f"{letters(2)}-{digits(3)}-{letters(2)}"

def random_civic():
    return str(random.randint(1, 9999))

def random_apartment():
    return random.choice([f"{random.randint(1, 100)}A", ""])

def random_street():
    return f"{random.choice(['Main', 'First', 'Second', 'Third', 'Elm', 'Maple'])} St"

def random_city():
    return f"City_{''.join(random.choices(string.ascii_letters, k=5))}"

def random_postal():
    letters = lambda: random.choice(string.ascii_uppercase)
    digit = lambda: random.choice(string.digits)
    return f"{letters()}{digit()}{letters()} {digit()}{letters()}{digit()}"

# ===== Helper Function =====
def print_status(description, response, expected_codes):
    status = "\033[92m✔\033[0m" if response.status_code in expected_codes else "\033[91m✘\033[0m"
    print(f"{status} {description} (status: {response.status_code})")
    if response.status_code not in expected_codes:
        print(f"Error: {response.text}")

# ===== Public Routes Tests =====
def test_public_routes():
    endpoints = ["/", "/favicon.ico", "/getImages/product/1", "/getImages/shop/1", "/getShops", "/getProducts"]
    for endpoint in endpoints:
        r = requests.get(BASE_URL + endpoint)
        print_status(f"GET {endpoint}", r, [200])

# ===== User Routes Positive Tests =====
def test_user_routes():
    email = random_email()
    user_data = {
        "email": email,
        "password": "password123",
        "last_name": random_name("Doe"),
        "first_name": random_name("John"),
        "phone_number": random_phone(),
        "user_type": "client",
        "civic": random_civic(),
        "street": random_street(),
        "city": random_city(),
        "postal_code": random_postal()
    }
    r = requests.post(BASE_URL + "/createUser", json=user_data)
    print_status("POST /createUser valid", r, [200, 201])
    if r.status_code in [200, 201]:
        created_user = r.json()
        print(f"Created user id: {created_user.get('user_id')}")

    # ===== Exception Cases for createUser =====
    # Missing password
    user_data_missing = user_data.copy()
    user_data_missing.pop("password")
    r = requests.post(BASE_URL + "/createUser", json=user_data_missing)
    print_status("POST /createUser missing password", r, [400])

    # Missing email
    user_data_missing = user_data.copy()
    user_data_missing.pop("email")
    r = requests.post(BASE_URL + "/createUser", json=user_data_missing)
    print_status("POST /createUser missing email", r, [400])

    # ===== Exception Cases for connectUser =====
    # Missing email or password in login
    login_data = {"password": "password123"}
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser missing email", r, [400])
    login_data = {"email": email}
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser missing password", r, [400])
    # Invalid credentials
    login_data = {"email": email, "password": "wrongpass"}
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser invalid credentials", r, [401])
    # Valid login
    login_data = {"email": email, "password": "password123"}
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser valid", r, [200])

# ===== Admin Routes Positive and Exception Tests =====
def test_admin_routes():
    # Fixed admin credentials assumed to be correct:
    admin_auth = {"admin_email": "tristan@boozy.com", "admin_password": "adminpass"}
    
    # ===== Successful call for admin/createUser =====
    admin_user_data = {
        **admin_auth,
        "email": random_email(),
        "password": "admin123",
        "last_name": random_name("Admin"),
        "first_name": random_name("Tester"),
        "phone_number": random_phone(),
        "user_type": "client",
        "address": {
            "civic": random_civic(),
            "street": random_street(),
            "city": random_city(),
            "postal_code": random_postal()
        }
    }
    r = requests.post(BASE_URL + "/admin/createUser", json=admin_user_data)
    print_status("POST /admin/createUser valid", r, [200, 201])
    if r.status_code in [200, 201]:
        ret = r.json()
        print(f"Admin-created user id: {ret.get('user_id')}")
        
    # Exception: missing admin credentials for admin/createUser
    bad_admin_data = admin_user_data.copy()
    bad_admin_data.pop("admin_email")
    r = requests.post(BASE_URL + "/admin/createUser", json=bad_admin_data)
    print_status("POST /admin/createUser missing admin_email", r, [401])
    
    # ===== Successful call for admin/createShop =====
    shop_data = {
        **admin_auth,
        "name": random_name("Shop"),
        "civic": random_civic(),
        "street": random_street(),
        "city": random_city(),
        "postal_code": random_postal()
    }
    r = requests.post(BASE_URL + "/admin/createShop", json=shop_data)
    print_status("POST /admin/createShop valid", r, [200, 201])
    if r.status_code in [200, 201]:
        ret = r.json()
        shop_id = ret.get("shop_id")
        print(f"New shop id: {shop_id}")
    # Exception: missing required field "name" for createShop
    bad_shop_data = shop_data.copy()
    bad_shop_data.pop("name")
    r = requests.post(BASE_URL + "/admin/createShop", json=bad_shop_data)
    print_status("POST /admin/createShop missing name", r, [400])
    
    # ===== Successful call for admin/createProduct =====
    product_data = {
        **admin_auth,
        "name": random_name("Product"),
        "price": round(random.uniform(5.0, 50.0), 2),
        "category": random.choice(["Test", "Demo", "Sample"]),
        "alcohol": round(random.uniform(0, 40), 1),
        "description": "Randomly generated product for testing."
    }
    r = requests.post(BASE_URL + "/admin/createProduct", json=product_data)
    print_status("POST /admin/createProduct valid", r, [200, 201])
    if r.status_code in [200, 201]:
        ret = r.json()
        print(f"New product id: {ret.get('product_id')}")
    # Exception: missing field "price"
    bad_product_data = product_data.copy()
    bad_product_data.pop("price")
    r = requests.post(BASE_URL + "/admin/createProduct", json=bad_product_data)
    print_status("POST /admin/createProduct missing price", r, [400])
    
    # ===== Exception: modifyAvailability missing required fields =====
    availability_data = {**admin_auth}
    r = requests.post(BASE_URL + "/admin/modifyAvailability", json=availability_data)
    print_status("POST /admin/modifyAvailability missing fields", r, [400])

# ===== Admin Web Routes Tests =====
def test_admin_web_routes():
    r = requests.get(BASE_URL + "/admin/web", allow_redirects=False)
    print_status("GET /admin/web redirect", r, [301, 302])
    r = requests.get(BASE_URL + "/admin/web/")
    print_status("GET /admin/web/", r, [200])

# ===== Additional Exception Tests for createOrder =====
def test_create_order_exceptions():
    # Create a valid user first
    user_data = {
        "email": random_email(),
        "password": "password123",
        "last_name": random_name("Doe"),
        "first_name": random_name("John"),
        "phone_number": random_phone(),
        "user_type": "client",
        "civic": random_civic(),
        "street": random_street(),
        "city": random_city(),
        "postal_code": random_postal()
    }
    r = requests.post(BASE_URL + "/createUser", json=user_data)
    if r.status_code not in [200, 201]:
        print("Cannot test createOrder exceptions: Failed to create user")
        return
    user_id = r.json().get("user_id")
    
    # Create a valid shop via admin route so we have a shop id
    admin_auth = {"admin_email": "tristan@boozy.com", "admin_password": "adminpass"}
    shop_data = {
        **admin_auth,
        "name": random_name("Shop"),
        "civic": random_civic(),
        "street": random_street(),
        "city": random_city(),
        "postal_code": random_postal()
    }
    r_shop = requests.post(BASE_URL + "/admin/createShop", json=shop_data)
    if r_shop.status_code not in [200, 201]:
        print("Cannot test createOrder exceptions: Failed to create shop")
        return
    shop_id = r_shop.json().get("shop_id")
    
    # Build a valid order payload
    order_payload = {
        "user_id": user_id,
        "shop_id": shop_id,
        "items": [{"product_id": 1, "quantity": 1}],  # assuming product id 1 exists
        "payment_method": "credit_card",
        "card_name": random_name("CardHolder"),
        "card_number": ''.join(random.choices(string.digits, k=16)),
        "CVC_card": ''.join(random.choices(string.digits, k=3)),
        "expiry_date_month": "12",
        "expiry_date_year": "2030"
    }
    # Missing required field: items
    bad_order = order_payload.copy()
    bad_order.pop("items")
    r = requests.post(BASE_URL + "/createOrder", json=bad_order)
    print_status("POST /createOrder missing items", r, [400])
    
    # Missing payment field: card_name
    bad_order = order_payload.copy()
    bad_order.pop("card_name")
    r = requests.post(BASE_URL + "/createOrder", json=bad_order)
    print_status("POST /createOrder missing payment card_name", r, [400])
    
    # Missing address info when none is provided and none exists for the user
    # Remove civic, street, city, and ensure the user has no address
    order_payload_no_addr = order_payload.copy()
    for k in ["civic", "street", "city", "postal_code"]:
        order_payload_no_addr.pop(k, None)
    # Force removal of address from user by sending an empty string if it exists
    r = requests.post(BASE_URL + "/createOrder", json=order_payload_no_addr)
    # This should fail if no delivery address is set; expected error code is 400
    print_status("POST /createOrder missing delivery address", r, [400])
    
# ===== Main =====
if __name__ == '__main__':
    print("\n--- Testing Public Routes ---")
    test_public_routes()

    print("\n--- Testing User Routes ---")
    test_user_routes()

    print("\n--- Testing Admin Routes ---")
    test_admin_routes()

    print("\n--- Testing Admin Web Routes ---")
    test_admin_web_routes()

    print("\n--- Testing Create Order Exceptions ---")
    test_create_order_exceptions()

    print("\nAll tests completed.")
