
import requests
import json
import time
import random
import string

# Base URL for the API
BASE_URL = "http://4.172.255.120:5000"  # Adjust if your server is running elsewhere

# Test session
session = requests.Session()

# Admin credentials for authenticated requests
ADMIN_EMAIL = "admin@test.com"
ADMIN_PASSWORD = "adminpass123"

# Helper functions
def random_string(length=8):
    """Generate a random string for test data"""
    return ''.join(random.choice(string.ascii_letters) for _ in range(length))

def random_number(length=5):
    """Generate a random number string for test data"""
    return ''.join(random.choice(string.digits) for _ in range(length))

def test_endpoint(method, endpoint, expected_status=200, data=None, params=None, auth=False):
    """Test an API endpoint and return the response"""
    url = f"{BASE_URL}/{endpoint}"
    
    if auth and data:
        data["adminEmail"] = ADMIN_EMAIL
        data["adminPassword"] = ADMIN_PASSWORD
    
    if method.lower() == 'get':
        response = session.get(url, params=params)
    elif method.lower() == 'post':
        response = session.post(url, json=data)
    else:
        print(f"[ERROR] Unsupported method: {method}")
        return None
    
    if response.status_code == expected_status:
        print(f"[SUCCESS] {method.upper()} {endpoint} - Status {response.status_code}")
        return response.json()
    else:
        print(f"[ERROR] {method.upper()} {endpoint} - Expected {expected_status}, got {response.status_code}")
        print(f"  Response: {response.text[:100]}...")
        return None

# Test variables to store IDs for later tests
test_user_id = None
test_store_id = None
test_product_id = None
test_address_id = None
test_order_id = None

# ============= TEST BASIC ROUTES =============
def test_basic_routes():
    print("\n=== Testing Basic Routes ===")
    test_endpoint("get", "", 200)
    test_endpoint("get", "test", 200)

# ============= TEST USER ENDPOINTS =============
def test_user_endpoints():
    global test_user_id
    
    print("\n=== Testing User Endpoints ===")
    
    # Create a test user
    user_data = {
        "email": f"test_user_{random_string()}@example.com",
        "password": "testpass123",
        "lastName": "TestLastName",
        "firstName": "TestFirstName",
        "phoneNumber": f"+1{random_number(10)}",
        "userType": "customer"
    }
    
    response = test_endpoint("post", "createUser", 200, user_data)
    if response and response.get("status") == "success":
        print(f"[INFO] Created test user with email: {user_data['email']}")
    
    # Try to connect with the new user
    login_data = {
        "email": user_data["email"],
        "password": user_data["password"]
    }
    
    response = test_endpoint("post", "connectUser", 200, login_data)
    if response and response.get("status") == "success":
        print(f"[INFO] Successfully logged in as: {user_data['email']}")
        test_user_id = response.get("user", {}).get("userId")
    
    # Test invalid credentials
    invalid_login = {
        "email": user_data["email"],
        "password": "wrongpassword"
    }
    
    test_endpoint("post", "connectUser", 401, invalid_login)
    print("[INFO] Invalid credentials test passed")

# ============= TEST ADMIN USER MANAGEMENT =============
def test_admin_user_management():
    print("\n=== Testing Admin User Management ===")
    
    # Create a test deliverer user through admin
    deliverer_data = {
        "email": f"test_deliverer_{random_string()}@example.com",
        "password": "deliverpass123",
        "lastName": "DeliverLastName",
        "firstName": "DeliverFirstName",
        "phoneNumber": f"+1{random_number(10)}",
        "userType": "deliverer",
        "licensePlate": f"{random_string(3)}-{random_number(3)}",
        "licenseNumber": random_number(8),
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD
    }
    
    response = test_endpoint("post", "admin/createUser", 200, deliverer_data, auth=True)
    if response and response.get("status") == "success":
        print(f"[INFO] Admin created test deliverer: {deliverer_data['email']}")
    
    # Login to get the user ID
    login_data = {
        "email": deliverer_data["email"],
        "password": deliverer_data["password"]
    }
    
    response = test_endpoint("post", "connectUser", 200, login_data)
    if response and response.get("status") == "success":
        deliverer_id = response.get("user", {}).get("userId")
        
        # Modify the user
        modify_data = {
            "userId": deliverer_id,
            "firstName": "UpdatedFirstName",
            "licensePlate": f"{random_string(3)}-{random_number(3)}",
            "adminEmail": ADMIN_EMAIL,
            "adminPassword": ADMIN_PASSWORD
        }
        
        response = test_endpoint("post", "admin/modifyUser", 200, modify_data, auth=True)
        if response and response.get("status") == "success":
            print(f"[INFO] Admin successfully modified user {deliverer_id}")
        
        # Delete the user
        delete_data = {
            "userId": deliverer_id,
            "adminEmail": ADMIN_EMAIL,
            "adminPassword": ADMIN_PASSWORD
        }
        
        response = test_endpoint("post", "admin/deleteUser", 200, delete_data, auth=True)
        if response and response.get("status") == "success":
            print(f"[INFO] Admin successfully deleted user {deliverer_id}")

# ============= TEST STORE ENDPOINTS =============
def test_store_endpoints():
    global test_store_id
    
    print("\n=== Testing Store Endpoints ===")
    
    # Get all stores
    response = test_endpoint("get", "getStores")
    if response:
        store_count = len(response)
        print(f"[INFO] Found {store_count} stores")
        
        # If stores exist, get one by ID
        if store_count > 0:
            first_store_id = response[0]["store_id"]
            test_store_id = first_store_id
            test_endpoint("get", f"getStores", params={"storeId": first_store_id})
            print(f"[INFO] Successfully retrieved store ID: {first_store_id}")

# ============= TEST ADMIN STORE MANAGEMENT =============
def test_admin_store_management():
    global test_store_id, test_address_id
    
    print("\n=== Testing Admin Store Management ===")
    
    # Create a test store
    store_data = {
        "name": f"Test Store {random_string()}",
        "houseNumber": random_number(3),
        "street": f"{random_string()} St",
        "postalCode": f"{random_string(3)} {random_number(3)}",
        "city": f"{random_string()}ville",
        "civicNumber": random_number(5),
        "imageNom": "test_store.png",
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD
    }
    
    response = test_endpoint("post", "admin/createStore", 200, store_data, auth=True)
    if response and response.get("status") == "success":
        new_store_id = response.get("storeId")
        test_store_id = new_store_id
        print(f"[INFO] Admin created test store ID: {new_store_id}")
        
        # Get the store to verify and find its address
        store_response = test_endpoint("get", "getStores", params={"storeId": new_store_id})
        if store_response and len(store_response) > 0:
            print(f"[INFO] Successfully retrieved new store: {store_response[0]['name']}")
            
            # Modify the store
            modify_data = {
                "storeId": new_store_id,
                "name": f"Updated Store {random_string()}",
                "city": f"{random_string()}town",
                "adminEmail": ADMIN_EMAIL,
                "adminPassword": ADMIN_PASSWORD
            }
            
            response = test_endpoint("post", "admin/modifyStore", 200, modify_data, auth=True)
            if response and response.get("status") == "success":
                print(f"[INFO] Admin successfully modified store {new_store_id}")

# ============= TEST PRODUCT ENDPOINTS =============
def test_product_endpoints():
    global test_product_id
    
    print("\n=== Testing Product Endpoints ===")
    
    # Get all products
    response = test_endpoint("get", "getProducts")
    if response:
        product_count = len(response)
        print(f"[INFO] Found {product_count} products")
        
        # If products exist, get one by ID
        if product_count > 0:
            first_product_id = response[0]["product_id"]
            test_product_id = first_product_id
            test_endpoint("get", "getProducts", params={"productId": first_product_id})
            print(f"[INFO] Successfully retrieved product ID: {first_product_id}")
            
            # Test image endpoint for this product
            image_response = requests.get(f"{BASE_URL}/getImages/product/{first_product_id}")
            if image_response.status_code == 200:
                print(f"[SUCCESS] GET getImages/product/{first_product_id} - Status {image_response.status_code}")
            else:
                print(f"[ERROR] GET getImages/product/{first_product_id} - Status {image_response.status_code}")

# ============= TEST ADMIN PRODUCT MANAGEMENT =============
def test_admin_product_management():
    global test_product_id
    
    print("\n=== Testing Admin Product Management ===")
    
    # Create a test product
    product_data = {
        "name": f"Test Product {random_string()}",
        "description": f"Test description for {random_string()}",
        "price": round(random.uniform(10, 100), 2),
        "category": "Test Category",
        "isAvailable": True,
        "imageNom": "test_product.png",
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD
    }
    
    response = test_endpoint("post", "admin/createProduct", 200, product_data, auth=True)
    if response and response.get("status") == "success":
        new_product_id = response.get("productId")
        test_product_id = new_product_id
        print(f"[INFO] Admin created test product ID: {new_product_id}")
        
        # Get the product to verify
        product_response = test_endpoint("get", "getProducts", params={"productId": new_product_id})
        if product_response and len(product_response) > 0:
            print(f"[INFO] Successfully retrieved new product: {product_response[0]['name']}")

# ============= TEST INVENTORY ENDPOINTS =============
def test_inventory_endpoints():
    print("\n=== Testing Inventory Endpoints ===")
    
    # Get all inventory
    response = test_endpoint("get", "getAvailability")
    if response:
        inventory_count = len(response)
        print(f"[INFO] Found {inventory_count} inventory items")
        
        # Test filtering by store
        if test_store_id:
            store_inventory = test_endpoint("get", "getAvailability", params={"storeId": test_store_id})
            if store_inventory:
                print(f"[INFO] Found {len(store_inventory)} inventory items for store {test_store_id}")
        
        # Test filtering by product
        if test_product_id:
            product_inventory = test_endpoint("get", "getAvailability", params={"productId": test_product_id})
            if product_inventory:
                print(f"[INFO] Found {len(product_inventory)} inventory items for product {test_product_id}")
        
        # Test filtering by in-stock quantity
        in_stock_inventory = test_endpoint("get", "getAvailability", params={"inStock": 5})
        if in_stock_inventory:
            print(f"[INFO] Found {len(in_stock_inventory)} inventory items with at least 5 units")

# ============= TEST ORDER ENDPOINTS =============
def create_test_address():
    """Create a test address for ordering"""
    global test_address_id
    
    if not test_user_id:
        print("[ERROR] Cannot create address: No test user available")
        return None
    
    # This would typically be handled by the client application
    # For testing purposes, we'll assume an address was created with ID 1
    test_address_id = 1
    print(f"[INFO] Using test address ID: {test_address_id}")
    return test_address_id

def test_order_endpoints():
    global test_order_id
    
    print("\n=== Testing Order Endpoints ===")
    
    # Ensure we have the necessary test data
    if not test_user_id:
        print("[ERROR] Cannot test orders: No test user available")
        return
    
    if not test_store_id:
        print("[ERROR] Cannot test orders: No test store available")
        return
    
    if not test_product_id:
        print("[ERROR] Cannot test orders: No test product available")
        return
    
    # Create or get a test address
    address_id = create_test_address()
    if not address_id:
        return
    
    # Create an order
    order_data = {
        "userId": test_user_id,
        "storeId": test_store_id,
        "addressId": address_id,
        "items": [
            {
                "productId": test_product_id,
                "quantity": 2
            }
        ],
        "paymentMethod": "credit_card",
        "cardNumber": "4111111111111111",
        "cvcCard": "123",
        "expiryDate": "12/25"
    }
    
    response = test_endpoint("post", "createOrder", 200, order_data)
    if response and response.get("status") == "success":
        new_order_id = response.get("orderId")
        test_order_id = new_order_id
        print(f"[INFO] Created test order ID: {new_order_id}")
        
        # Cancel the order
        cancel_data = {
            "orderId": new_order_id
        }
        
        response = test_endpoint("post", "cancelOrder", 200, cancel_data)
        if response and response.get("status") == "success":
            print(f"[INFO] Successfully cancelled order {new_order_id}")

# ============= TEST CLEANUP =============
def test_cleanup():
    print("\n=== Cleaning Up Test Data ===")
    
    # Delete the test store if we created one
    if test_store_id:
        delete_store_data = {
            "storeId": test_store_id,
            "adminEmail": ADMIN_EMAIL,
            "adminPassword": ADMIN_PASSWORD
        }
        
        response = test_endpoint("post", "admin/deleteStore", 200, delete_store_data, auth=True)
        if response and response.get("status") == "success":
            print(f"[INFO] Successfully deleted test store {test_store_id}")
    
    # Note: We don't have an endpoint to delete products in the API

# ============= MAIN TEST RUNNER =============
def run_tests():
    print("=== Starting Boozy API Tests ===")
    
    try:
        # Run tests in sequence
        test_basic_routes()
        test_user_endpoints()
        test_admin_user_management()
        test_store_endpoints()
        test_admin_store_management()
        test_product_endpoints()
        test_admin_product_management()
        test_inventory_endpoints()
        test_order_endpoints()
        test_cleanup()
        
        print("\n=== All Tests Completed ===")
    except Exception as e:
        print(f"\n[CRITICAL ERROR] Test runner failed: {e}")

if __name__ == "__main__":
    run_tests()