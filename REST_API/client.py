import requests
import random
import string

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

# ===== User Routes Tests =====
def test_user_routes():
    # Create a user with attached address (via civic/street/city)
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
    # Since user_id is no longer returned, we just check for success.
    # Test exception: missing password
    user_data_missing = user_data.copy()
    user_data_missing.pop("password")
    r = requests.post(BASE_URL + "/createUser", json=user_data_missing)
    print_status("POST /createUser missing password", r, [400])
    # Test exception: missing email
    user_data_missing = user_data.copy()
    user_data_missing.pop("email")
    r = requests.post(BASE_URL + "/createUser", json=user_data_missing)
    print_status("POST /createUser missing email", r, [400])
    
    # ===== connectUser Tests =====
    # Missing email
    login_data = {"password": "password123"}
    r = requests.post(BASE_URL + "/connectUser", json=login_data)
    print_status("POST /connectUser missing email", r, [400])
    # Missing password
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
    
    # ===== modifyUser Tests =====
    # Update only the first_name field
    mod_data = {"email": email, "password": "password123", "first_name": random_name("Johnny")}
    r = requests.post(BASE_URL + "/modifyUser", json=mod_data)
    print_status("POST /modifyUser update first_name", r, [200])
    # Missing credentials
    mod_data = {"first_name": "NoCred"}
    r = requests.post(BASE_URL + "/modifyUser", json=mod_data)
    print_status("POST /modifyUser missing credentials", r, [400])

# ===== Order Endpoints Exception Tests =====
def test_create_order_exceptions():
    # Create a user who will have a delivery address attached (via createUser)
    user_data = {
        "email": random_email(),
        "password": "password123",
        "last_name": random_name("Smith"),
        "first_name": random_name("Alice"),
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
    # Use the same credentials for login and ordering
    email = user_data["email"]
    # Create a shop via admin route to obtain a shop_id
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
    
    # Build a valid order payload (notice: no address fields and no user_id)
    order_payload = {
        "email": email,
        "password": "password123",
        "shop_id": shop_id,
        "items": [{"product_id": 1, "quantity": 1}],  # assuming product id 1 exists
        "payment_method": "credit_card",
        "card_name": random_name("CardHolder"),
        "card_number": ''.join(random.choices(string.digits, k=16)),
        "CVC_card": ''.join(random.choices(string.digits, k=3)),
        "expiry_date_month": "12",
        "expiry_date_year": "2030"
    }
    # Exception: missing required field "items"
    bad_order = order_payload.copy()
    bad_order.pop("items")
    r = requests.post(BASE_URL + "/createOrder", json=bad_order)
    print_status("POST /createOrder missing items", r, [400])
    
    # Exception: missing payment field "card_name"
    bad_order = order_payload.copy()
    bad_order.pop("card_name")
    r = requests.post(BASE_URL + "/createOrder", json=bad_order)
    print_status("POST /createOrder missing payment card_name", r, [400])
    
    # Exception: user without an attached address – create such a user
    user_no_addr = {
        "email": random_email(),
        "password": "password123",
        "last_name": random_name("NoAddr"),
        "first_name": random_name("Bob"),
        "phone_number": random_phone(),
        "user_type": "client"
        # No civic, street, city provided.
    }
    r_no_addr = requests.post(BASE_URL + "/createUser", json=user_no_addr)
    login_no_addr = {"email": user_no_addr["email"], "password": "password123"}
    order_payload_no_addr = order_payload.copy()
    order_payload_no_addr.update(login_no_addr)
    r = requests.post(BASE_URL + "/createOrder", json=order_payload_no_addr)
    print_status("POST /createOrder missing attached delivery address", r, [400])

# ===== Order Cancellation and Update Tests =====
def test_order_cancellation_and_update():
    # For testing cancelOrder and updateOrder, we need an existing order.
    # Create a client user with address.
    user_data = {
        "email": random_email(),
        "password": "password123",
        "last_name": random_name("Cancel"),
        "first_name": random_name("Carol"),
        "phone_number": random_phone(),
        "user_type": "client",
        "civic": random_civic(),
        "street": random_street(),
        "city": random_city(),
        "postal_code": random_postal()
    }
    r = requests.post(BASE_URL + "/createUser", json=user_data)
    if r.status_code not in [200, 201]:
        print("Cannot test order cancellation: Failed to create user")
        return
    email = user_data["email"]

    # Create a shop via admin route
    admin_auth = {"admin_email": "tristan@boozy.com", "admin_password": "adminpass"}
    shop_data = {
        **admin_auth,
        "name": random_name("CancelShop"),
        "civic": random_civic(),
        "street": random_street(),
        "city": random_city(),
        "postal_code": random_postal()
    }
    r_shop = requests.post(BASE_URL + "/admin/createShop", json=shop_data)
    if r_shop.status_code not in [200, 201]:
        print("Cannot test order cancellation: Failed to create shop")
        return
    shop_id = r_shop.json().get("shop_id")

    # Create a valid order for cancellation test
    order_payload = {
        "email": email,
        "password": "password123",
        "shop_id": shop_id,
        "items": [{"product_id": 1, "quantity": 1}],
        "payment_method": "credit_card",
        "card_name": random_name("CardHolder"),
        "card_number": ''.join(random.choices(string.digits, k=16)),
        "CVC_card": ''.join(random.choices(string.digits, k=3)),
        "expiry_date_month": "12",
        "expiry_date_year": "2030"
    }
    r_order = requests.post(BASE_URL + "/createOrder", json=order_payload)
    if r_order.status_code not in [200, 201]:
        print("Cannot test cancellation: Order creation failed")
        return

    # Since /createOrder does not return order_id, we simulate by fetching the list of orders.
    r_orders = requests.get(BASE_URL + "/getOrders")
    if r_orders.status_code != 200:
        print("Cannot test cancellation: Failed to retrieve orders")
        return
    orders = r_orders.json()
    # Find an order (assuming our test order is among those with status 'Searching')
    if not orders:
        print("No orders available for cancellation test")
        return
    order_id = orders[0]["order_id"]

    # Attempt cancellation with wrong credentials
    cancel_payload = {"email": email, "password": "wrongpassword", "order_id": order_id}
    r = requests.post(BASE_URL + "/cancelOrder", json=cancel_payload)
    print_status("POST /cancelOrder with invalid credentials", r, [401])
    # Attempt cancellation with valid credentials
    cancel_payload["password"] = "password123"
    r = requests.post(BASE_URL + "/cancelOrder", json=cancel_payload)
    print_status("POST /cancelOrder valid", r, [200])

    # ===== Update Order Test for Carrier =====
    # For updateOrder, the carrier must have an active order.
    # Since setting up a full order flow for carriers is complex,
    # we test updateOrder expecting no active order.
    update_payload = {"email": "carrier@example.com", "password": "carrierpass", "status": "Shipped"}
    r = requests.post(BASE_URL + "/updateOrder", json=update_payload)
    print_status("POST /updateOrder with no active order", r, [404])

# ===== Admin Routes Tests =====
def test_admin_routes():
    admin_auth = {"admin_email": "tristan@boozy.com", "admin_password": "adminpass"}
    
    # ===== admin/createUser =====
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
    # Exception: missing admin_email
    bad_admin_data = admin_user_data.copy()
    bad_admin_data.pop("admin_email")
    r = requests.post(BASE_URL + "/admin/createUser", json=bad_admin_data)
    print_status("POST /admin/createUser missing admin_email", r, [401])
    
    # ===== admin/createShop =====
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
    # Exception: missing required field "name"
    bad_shop_data = shop_data.copy()
    bad_shop_data.pop("name")
    r = requests.post(BASE_URL + "/admin/createShop", json=bad_shop_data)
    print_status("POST /admin/createShop missing name", r, [400])
    
    # ===== admin/createProduct =====
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
    # Exception: missing field "price"
    bad_product_data = product_data.copy()
    bad_product_data.pop("price")
    r = requests.post(BASE_URL + "/admin/createProduct", json=bad_product_data)
    print_status("POST /admin/createProduct missing price", r, [400])
    
    # ===== admin/modifyAvailability Exception =====
    availability_data = {**admin_auth}
    r = requests.post(BASE_URL + "/admin/modifyAvailability", json=availability_data)
    print_status("POST /admin/modifyAvailability missing fields", r, [400])

# ===== Admin Web Routes Tests =====
def test_admin_web_routes():
    r = requests.get(BASE_URL + "/admin/web", allow_redirects=False)
    print_status("GET /admin/web redirect", r, [301, 302])
    r = requests.get(BASE_URL + "/admin/web/")
    print_status("GET /admin/web/", r, [200])

# ===== Main =====
if __name__ == '__main__':
    print("\n--- Testing Public Routes ---")
    test_public_routes()

    print("\n--- Testing User Routes ---")
    test_user_routes()

    print("\n--- Testing Order Creation Exceptions and Cancel/Update ---")
    test_create_order_exceptions()
    test_order_cancellation_and_update()

    print("\n--- Testing Admin Routes ---")
    test_admin_routes()

    print("\n--- Testing Admin Web Routes ---")
    test_admin_web_routes()

    print("\nAll tests completed.")
