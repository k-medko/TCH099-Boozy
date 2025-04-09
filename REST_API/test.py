import requests
import random
import string
from datetime import datetime

BASE_URL = "http://4.172.255.120:5000"

def random_email():
    return f"test_{''.join(random.choices(string.ascii_lowercase, k=5))}@example.com"

def random_name(prefix):
    return f"{prefix}_{''.join(random.choices(string.ascii_lowercase, k=5))}"

def print_status(description, response, expected_codes):
    status = "\033[92m✔\033[0m" if response.status_code in expected_codes else "\033[91m✘\033[0m"
    print(f"{status} {description} (status: {response.status_code})")
    if response.status_code not in expected_codes:
        print(f"Error: {response.text}")

# Public Routes
def test_public_routes():
    endpoints = ["/", "/favicon.ico", "/getImages/product/1", "/getImages/shop/1", "/getShops", "/getProducts"]
    for endpoint in endpoints:
        r = requests.get(BASE_URL + endpoint)
        print_status(f"GET {endpoint}", r, [200])

# User Routes
def test_user_routes():
    email = random_email()
    user_data = {
        "email": email,
        "password": "password123",
        "last_name": random_name("Doe"),
        "first_name": random_name("John"),
        "phone_number": "1234567890",
        "user_type": "client",
        "civic": "123",
        "street": "Main St",
        "city": "Testville",
        "postal_code": "A1B2C3"
    }
    # Create a new user; the id is auto generated
    r = requests.post(BASE_URL + "/createUser", json=user_data)
    print_status("POST /createUser valid", r, [200, 201])
    if r.status_code in [200, 201]:
        returned_user = r.json()
        created_user_id = returned_user.get("user_id")
        print(f"Created user id: {created_user_id}")
    # Attempt to create a user missing the password field
    user_data_missing = user_data.copy()
    user_data_missing.pop("password")
    r = requests.post(BASE_URL + "/createUser", json=user_data_missing)
    print_status("POST /createUser missing password", r, [400])
    # Connect with correct credentials
    login_data = {"email": email, "password": "password123"}
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser valid", r, [200])
    # Connect with wrong password
    login_data["password"] = "wrongpass"
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser invalid password", r, [401])

# Admin Routes
def test_admin_routes():
    admin_auth = {"admin_email": "tristan@boozy.com", "admin_password": "adminpass"}

    # Create a new user through the admin endpoint. The new id is auto-generated.
    admin_user_data = {**admin_auth,
        "email": random_email(),
        "password": "admin123",
        "last_name": random_name("Admin"),
        "first_name": random_name("Tester"),
        "phone_number": "0000000000",
        "user_type": "client",
        "address": {
            "civic": "1",
            "street": "Admin St",
            "city": "Admintown",
            "postal_code": "ADM123"
        }
    }
    r = requests.post(BASE_URL + "/admin/createUser", json=admin_user_data)
    print_status("POST /admin/createUser valid", r, [200, 201])
    if r.status_code in [200, 201]:
        ret = r.json()
        print(f"New admin-created user id: {ret.get('user_id')}")
    
    # Create a shop through the admin endpoint
    shop_data = {**admin_auth,
        "name": random_name("Shop"),
        "civic": "10",
        "street": "Market St",
        "city": "ShopCity",
        "postal_code": "SHP123"
    }
    r = requests.post(BASE_URL + "/admin/createShop", json=shop_data)
    print_status("POST /admin/createShop valid", r, [200, 201])
    if r.status_code in [200, 201]:
        ret = r.json()
        print(f"New shop id: {ret.get('shop_id')}")
    
    # Create a product through the admin endpoint
    product_data = {**admin_auth,
        "name": random_name("Product"),
        "price": 10.99,
        "category": "Test",
        "alcohol": 0,
        "description": "A test product"
    }
    r = requests.post(BASE_URL + "/admin/createProduct", json=product_data)
    print_status("POST /admin/createProduct valid", r, [200, 201])
    if r.status_code in [200, 201]:
        ret = r.json()
        print(f"New product id: {ret.get('product_id')}")
    
    # Attempt admin modifyAvailability missing required fields
    availability_data = {**admin_auth}
    r = requests.post(BASE_URL + "/admin/modifyAvailability", json=availability_data)
    print_status("POST /admin/modifyAvailability missing fields", r, [400])

# Admin Web Routes
def test_admin_web_routes():
    r = requests.get(BASE_URL + "/admin/web", allow_redirects=False)
    print_status("GET /admin/web redirect", r, [301, 302])
    r = requests.get(BASE_URL + "/admin/web/")
    print_status("GET /admin/web/", r, [200])

if __name__ == '__main__':
    print("\n--- Testing Public Routes ---")
    test_public_routes()

    print("\n--- Testing User Routes ---")
    test_user_routes()

    print("\n--- Testing Admin Routes ---")
    test_admin_routes()

    print("\n--- Testing Admin Web Routes ---")
    test_admin_web_routes()

    print("\nAll tests completed.")
