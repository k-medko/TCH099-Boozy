import requests
import json
import random
import string
from datetime import datetime

# ANSI color codes for terminal output
GREEN = "\033[92m"
RED = "\033[91m"
RESET = "\033[0m"

# API Base URL
BASE_URL = "http://4.172.252.189:5000"

# -------------------------------
# Helper Functions for Random Data
# -------------------------------
def random_string(length=10):
    """Generate a random string of fixed length."""
    return ''.join(random.choice(string.ascii_lowercase) for _ in range(length))

def random_phone():
    """Generate a random phone number."""
    return f"514{random.randint(100, 999)}{random.randint(1000, 9999)}"

def random_email():
    """Generate a random email address."""
    return f"{random_string(8)}@{random_string(5)}.com"

def random_license_plate():
    """Generate a random license plate."""
    letters = ''.join(random.choice(string.ascii_uppercase) for _ in range(3))
    numbers = ''.join(random.choice(string.digits) for _ in range(3))
    return f"{letters}{numbers}"

def random_civic():
    """Generate a random civic address number."""
    return random.randint(100, 9999)

def random_postal_code():
    """Generate a random postal code."""
    return f"{random.choice(string.ascii_uppercase)}{random.randint(1, 9)}{random.choice(string.ascii_uppercase)} {random.randint(1, 9)}{random.choice(string.ascii_uppercase)}{random.randint(1, 9)}"

def random_card_number():
    """Generate a random card number (16 digits)."""
    return ''.join(random.choice(string.digits) for _ in range(16))

def random_cvc():
    """Generate a random CVC (3 digits)."""
    return random.randint(100, 999)

# -------------------------------
# Global Unique Parameters (non-admin)
# -------------------------------
# Admin credentials remain constant as they are already stored in the database.
ADMIN_CREDS = {
    "admin_email": "tristan@boozy.com",
    "admin_password": "adminpass"
}

# Unique Shop details
SHOP_NAME    = f"Test Shop {random_string(5)}"
SHOP_CIVIC   = random_civic()
SHOP_STREET  = f"{random_string(8)} Street"
SHOP_CITY    = "Montreal"
SHOP_POSTAL  = random_postal_code()

# Unique Product details
PRODUCT_NAME        = f"Test Wine {random_string(5)}"
PRODUCT_DESCRIPTION = "A fine wine for testing"
PRODUCT_PRICE       = 19.99
PRODUCT_CATEGORY    = "wine"
PRODUCT_ALCOHOL     = 12.5

# Unique Client user details
CLIENT_EMAIL    = random_email()
CLIENT_PASSWORD = random_string(12)
CLIENT_LASTNAME = random_string(6)
CLIENT_FIRSTNAME= random_string(6)
CLIENT_PHONE    = random_phone()
CLIENT_CIVIC    = random_civic()
CLIENT_STREET   = f"{random_string(8)} Avenue"
CLIENT_CITY     = "Montreal"
CLIENT_POSTAL   = random_postal_code()

# Unique Carrier user details
CARRIER_EMAIL         = random_email()
CARRIER_PASSWORD      = random_string(12)
CARRIER_LASTNAME      = random_string(6)
CARRIER_FIRSTNAME     = random_string(6)
CARRIER_PHONE         = random_phone()
CARRIER_LICENSE_PLATE = random_license_plate()
CARRIER_CAR_BRAND     = random_string(8)

# -------------------------------
# Global Variables to Store IDs from API Responses
# -------------------------------
test_shop = None
test_product = None
test_client = None
test_carrier = None
order_id = None

# -------------------------------
# Helper function for testing endpoints
# -------------------------------
def test_endpoint(method, endpoint, data=None, expected_status=200, description="valid"):
    url = f"{BASE_URL}{endpoint}"
    try:
        if method.upper() == "GET":
            response = requests.get(url, params=data)
        else:
            response = requests.post(url, json=data)
        if response.status_code == expected_status:
            print(f"{GREEN}✓ {method} {endpoint} {description} (status: {response.status_code}){RESET}")
            return response.json() if response.text else None
        else:
            print(f"{RED}✗ {method} {endpoint} {description} (expected: {expected_status}, got: {response.status_code}){RESET}")
            print(f"{RED}  Response: {response.text}{RESET}")
            return None
    except Exception as e:
        print(f"{RED}✗ {method} {endpoint} {description} - Exception: {str(e)}{RESET}")
        return None

# -------------------------------
# Test Suite
# -------------------------------
def run_tests():
    global test_shop, test_product, test_client, test_carrier, order_id

    print("\n===== Testing Public Routes =====")
    
    # Home endpoint
    test_endpoint("GET", "/", expected_status=200, description="home endpoint")
    
    # Get shops
    test_endpoint("GET", "/getShops", expected_status=200, description="all shops")
    test_endpoint("GET", "/getShops", data={"shop_id": "999999"}, expected_status=200, description="nonexistent shop_id")
    
    # Get products
    test_endpoint("GET", "/getProducts", expected_status=200, description="all products")
    test_endpoint("GET", "/getProducts", data={"product_id": "999999"}, expected_status=200, description="nonexistent product_id")
    test_endpoint("GET", "/getProducts", data={"category": "wine"}, expected_status=200, description="with category filter")
    
    # Get availability
    test_endpoint("GET", "/getAvailability", expected_status=200, description="all availability")
    test_endpoint("GET", "/getAvailability", data={"shop_id": "999999"}, expected_status=200, description="nonexistent shop_id")
    test_endpoint("GET", "/getAvailability", data={"product_id": "999999"}, expected_status=200, description="nonexistent product_id")
    test_endpoint("GET", "/getAvailability", data={"in_stock": "10"}, expected_status=200, description="with in_stock filter")
    
    # Get orders
    test_endpoint("GET", "/getOrders", expected_status=200, description="available orders")
    
    print("\n===== Testing Admin Routes =====")
    
    # --- Create Shop via Admin Endpoint ---
    shop_data = {
        **ADMIN_CREDS,
        "name": SHOP_NAME,
        "civic": SHOP_CIVIC,
        "street": SHOP_STREET,
        "city": SHOP_CITY,
        "postal_code": SHOP_POSTAL
    }
    shop_result = test_endpoint("POST", "/admin/createShop", data=shop_data, expected_status=200, description="create shop")
    if shop_result and "shop_id" in shop_result:
        test_shop = shop_result["shop_id"]
        # Modify the shop name with new random data
        modify_shop_data = {
            **ADMIN_CREDS,
            "shop_id": test_shop,
            "name": f"Updated {SHOP_NAME}"
        }
        test_endpoint("POST", "/admin/modifyShop", data=modify_shop_data, expected_status=200, description="modify shop")
    
    # --- Create Product via Admin Endpoint ---
    product_data = {
        **ADMIN_CREDS,
        "name": PRODUCT_NAME,
        "description": PRODUCT_DESCRIPTION,
        "price": PRODUCT_PRICE,
        "category": PRODUCT_CATEGORY,
        "alcohol": PRODUCT_ALCOHOL
    }
    product_result = test_endpoint("POST", "/admin/createProduct", data=product_data, expected_status=200, description="create product")
    if product_result and "product_id" in product_result:
        test_product = product_result["product_id"]
        # Modify product with a new random price
        modify_product_data = {
            **ADMIN_CREDS,
            "product_id": test_product,
            "price": PRODUCT_PRICE + 5.00
        }
        test_endpoint("POST", "/admin/modifyProduct", data=modify_product_data, expected_status=200, description="modify product")
    
    # --- Add Product to Shop Inventory via Admin Endpoint ---
    if test_shop and test_product:
        inventory_data = {
            **ADMIN_CREDS,
            "shop_id": test_shop,
            "products": [
                {
                    "product_id": test_product,
                    "quantity": 50
                }
            ]
        }
        test_endpoint("POST", "/admin/modifyAvailability", data=inventory_data, expected_status=200, description="add product to shop")
    
    # --- Get Users via Admin Endpoint ---
    test_endpoint("POST", "/admin/getUsers", data=ADMIN_CREDS, expected_status=200, description="get all users")
    
    print("\n===== Testing User Routes =====")
    
    # --- Create Client User ---
    client_data = {
        "email": CLIENT_EMAIL,
        "password": CLIENT_PASSWORD,
        "last_name": CLIENT_LASTNAME,
        "first_name": CLIENT_FIRSTNAME,
        "phone_number": CLIENT_PHONE,
        "user_type": "client",
        "civic": CLIENT_CIVIC,
        "street": CLIENT_STREET,
        "city": CLIENT_CITY,
        "postal_code": CLIENT_POSTAL
    }
    test_endpoint("POST", "/createUser", data=client_data, expected_status=200, description="create client user")
    
    # --- Create Carrier User ---
    carrier_data = {
        "email": CARRIER_EMAIL,
        "password": CARRIER_PASSWORD,
        "last_name": CARRIER_LASTNAME,
        "first_name": CARRIER_FIRSTNAME,
        "phone_number": CARRIER_PHONE,
        "user_type": "carrier",
        "license_plate": CARRIER_LICENSE_PLATE,
        "car_brand": CARRIER_CAR_BRAND
    }
    test_endpoint("POST", "/createUser", data=carrier_data, expected_status=200, description="create carrier user")
    
    # --- Test Login for Client ---
    client_login_data = {
        "email": CLIENT_EMAIL,
        "password": CLIENT_PASSWORD
    }
    client_login = test_endpoint("POST", "/connectUser", data=client_login_data, expected_status=200, description="client login")
    if client_login and "user" in client_login:
        test_client = client_login["user"]
    
    # --- Test Login for Carrier ---
    carrier_login_data = {
        "email": CARRIER_EMAIL,
        "password": CARRIER_PASSWORD
    }
    carrier_login = test_endpoint("POST", "/connectUser", data=carrier_login_data, expected_status=200, description="carrier login")
    if carrier_login and "user" in carrier_login:
        test_carrier = carrier_login["user"]
    
    # --- Modify Client User ---
    modify_client_data = {
        "email": CLIENT_EMAIL,
        "password": CLIENT_PASSWORD,
        "first_name": f"Updated{CLIENT_FIRSTNAME}",
        "phone_number": random_phone()
    }
    test_endpoint("POST", "/modifyUser", data=modify_client_data, expected_status=200, description="modify client user")
    
    print("\n===== Testing Order Routes =====")
    
    # --- Create an Order as Client ---
    if test_client and test_shop and test_product:
        order_data = {
            "email": CLIENT_EMAIL,
            "password": CLIENT_PASSWORD,
            "shop_id": test_shop,
            "items": [
                {
                    "product_id": test_product,
                    "quantity": 2
                }
            ],
            "payment_method": "credit_card",
            "card_name": f"{CLIENT_FIRSTNAME} {CLIENT_LASTNAME}",
            "card_number": random_card_number(),
            "CVC_card": random_cvc(),
            "expiry_date_month": random.randint(1, 12),
            "expiry_date_year": random.randint(25, 30)
        }
        test_endpoint("POST", "/createOrder", data=order_data, expected_status=200, description="create order")
        
        # Fetch orders to obtain our order ID
        orders = test_endpoint("GET", "/getOrders", expected_status=200, description="get available orders")
        if orders:
            for order in orders:
                if order["shop_id"] == test_shop:
                    order_id = order["order_id"]
                    break
        
        # --- Carrier Takes the Order ---
        if test_carrier and order_id:
            take_order_data = {
                "email": CARRIER_EMAIL,
                "password": CARRIER_PASSWORD,
                "order_id": order_id
            }
            test_endpoint("POST", "/takeOrder", data=take_order_data, expected_status=200, description="carrier takes order")
            
            # --- Carrier Updates Order Status to Shipping ---
            update_order_data = {
                "email": CARRIER_EMAIL,
                "password": CARRIER_PASSWORD,
                "status": "Shipping"
            }
            test_endpoint("POST", "/updateOrder", data=update_order_data, expected_status=200, description="update order to Shipping")
            
            # --- Carrier Updates Order Status to Completed ---
            complete_order_data = {
                "email": CARRIER_EMAIL,
                "password": CARRIER_PASSWORD,
                "status": "Completed"
            }
            test_endpoint("POST", "/updateOrder", data=complete_order_data, expected_status=200, description="update order to Completed")
            
            # Verify carrier earnings updated by logging in again
            carrier_verify = test_endpoint("POST", "/connectUser", data=carrier_login_data, expected_status=200, description="verify carrier earnings")
            if carrier_verify and "user" in carrier_verify:
                earnings = carrier_verify["user"].get("total_earnings", 0)
                if earnings > 0:
                    print(f"{GREEN}✓ Carrier earnings updated: {earnings}{RESET}")
                else:
                    print(f"{RED}✗ Carrier earnings not updated{RESET}")
        
        # --- Create another Order for Cancellation Test ---
        test_endpoint("POST", "/createOrder", data=order_data, expected_status=200, description="create order for cancellation")
        new_orders = test_endpoint("GET", "/getOrders", expected_status=200, description="get available orders for cancellation")
        if new_orders:
            for order in new_orders:
                if order["shop_id"] == test_shop:
                    cancel_order_id = order["order_id"]
                    cancel_data = {
                        "email": CLIENT_EMAIL,
                        "password": CLIENT_PASSWORD,
                        "order_id": cancel_order_id
                    }
                    test_endpoint("POST", "/cancelOrder", data=cancel_data, expected_status=200, description="cancel order")
                    break
    
    print("\n===== Testing Admin Delete Routes =====")
    
    # --- Delete Product ---
    if test_product:
        delete_product_data = { **ADMIN_CREDS, "product_id": test_product }
        test_endpoint("POST", "/admin/deleteProduct", data=delete_product_data, expected_status=200, description="delete product")
        test_endpoint("POST", "/admin/deleteProduct", data=delete_product_data, expected_status=404, description="delete non-existent product")
    
    # --- Delete Shop ---
    if test_shop:
        delete_shop_data = { **ADMIN_CREDS, "shop_id": test_shop }
        test_endpoint("POST", "/admin/deleteShop", data=delete_shop_data, expected_status=200, description="delete shop")
        test_endpoint("POST", "/admin/deleteShop", data=delete_shop_data, expected_status=404, description="delete non-existent shop")
    
    # --- Delete Users via Admin Endpoint ---
    users = test_endpoint("POST", "/admin/getUsers", data=ADMIN_CREDS, expected_status=200, description="get users for cleanup")
    if users and "users" in users:
        client_id = None
        carrier_id = None
        for user in users["users"]:
            if user["email"] == CLIENT_EMAIL:
                client_id = user["user_id"]
            elif user["email"] == CARRIER_EMAIL:
                carrier_id = user["user_id"]
        if client_id:
            delete_client_data = { **ADMIN_CREDS, "user_id": client_id }
            test_endpoint("POST", "/admin/deleteUser", data=delete_client_data, expected_status=200, description="delete client user")
        if carrier_id:
            delete_carrier_data = { **ADMIN_CREDS, "user_id": carrier_id }
            test_endpoint("POST", "/admin/deleteUser", data=delete_carrier_data, expected_status=200, description="delete carrier user")
            test_endpoint("POST", "/admin/deleteUser", data=delete_carrier_data, expected_status=404, description="delete non-existent user")

if __name__ == "__main__":
    print("Starting Boozy API Test Suite")
    print(f"Testing against API at: {BASE_URL}")
    run_tests()
    print("\nTest suite completed")
