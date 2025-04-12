#!/usr/bin/env python3
import requests
import json
import time
import random
import string
import sys
from datetime import datetime

# Configuration
BASE_URL = "http://4.172.255.120:5000"
ADMIN_EMAIL = "tristan@boozy.com"
ADMIN_PASSWORD = "adminpass"
DELAY_BETWEEN_REQUESTS = 0.5  # in seconds

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    ENDC = '\033[0m'

# Test result tracking
results = {
    "total": 0,
    "passed": 0,
    "failed": 0
}

def random_string():
    length = random.randint(6, 12)
    allowed_chars = string.ascii_letters + string.digits
    return ''.join(random.choice(allowed_chars) for _ in range(length))

def print_header(message):
    """Print a formatted header message."""
    print(f"\n{Colors.BLUE}{'=' * 80}{Colors.ENDC}")
    print(f"{Colors.BLUE}== {message}{Colors.ENDC}")
    print(f"{Colors.BLUE}{'=' * 80}{Colors.ENDC}")

def print_subheader(message):
    """Print a formatted subheader message."""
    print(f"\n{Colors.CYAN}----- {message} -----{Colors.CYAN}{Colors.ENDC}")

def print_success(message):
    """Print a success message."""
    print(f"{Colors.GREEN}✓ {message}{Colors.ENDC}")
    results["passed"] += 1
    results["total"] += 1

def print_failure(message, details=None):
    """Print a failure message with optional details."""
    print(f"{Colors.RED}✗ {message}{Colors.ENDC}")
    if details:
        print(f"{Colors.YELLOW}  Details: {details}{Colors.ENDC}")
    results["failed"] += 1
    results["total"] += 1

def print_info(message):
    """Print an informational message."""
    print(f"{Colors.MAGENTA}ℹ {message}{Colors.ENDC}")

def make_request(method, endpoint, json=None, params=None, expected_status=200, error_ok=False):
    """
    Make an HTTP request and handle the response.
    
    Args:
        method: HTTP method (get, post, etc.)
        endpoint: API endpoint to call
        json: JSON data for request body
        params: URL parameters
        expected_status: Expected HTTP status code
        error_ok: Whether an error response is acceptable
        
    Returns:
        Response object or None if failed
    """
    url = f"{BASE_URL}/{endpoint}"
    print_info(f"Requesting {method.upper()} {url}")
    
    if json:
        print_info(f"Request data: {json}")
    
    try:
        response = getattr(requests, method.lower())(url, json=json, params=params)
        
        # Print response details
        print_info(f"Response status: {response.status_code}")
        try:
            response_json = response.json()
            print_info(f"Response data: {response_json}")
        except:
            print_info(f"Response content: {response.text[:200]}")
        
        # Check if status code matches expected
        if response.status_code == expected_status:
            return response
        else:
            if not error_ok:
                print_failure(f"Expected status {expected_status}, got {response.status_code}")
            return response if error_ok else None
    except Exception as e:
        print_failure(f"Request failed", str(e))
        return None
    finally:
        # Add delay between requests
        time.sleep(DELAY_BETWEEN_REQUESTS)

def test_get_product_image():
    """Test the product image endpoint."""
    print_header("Testing GET /getImages/product/{id}")
    
    # Test with valid product ID
    response = make_request("get", "getImages/product/15")
    if response and response.headers.get('Content-Type').startswith('image/'):
        print_success("Successfully retrieved product image")
    else:
        print_failure("Failed to retrieve product image")
    
    # Test with non-existent product ID
    response = make_request("get", "getImages/product/999999")
    if response and response.headers.get('Content-Type').startswith('image/'):
        print_success("Default image returned for non-existent product")
    else:
        print_failure("Failed to handle non-existent product image")
    
    # Test with invalid product ID
    response = make_request("get", "getImages/product/invalid", error_ok=True)
    if response and response.status_code != 200:
        print_success("Properly handled invalid product ID")
    else:
        print_failure("Failed to properly handle invalid product ID")

def test_get_shops():
    global test_shop_id
    """Test the getShops endpoint."""
    print_header("Testing GET /getShops")
    
    # Test getting all shops
    print_subheader("Getting all shops")
    response = make_request("get", "getShops")
    if response:
        shops = response.json()
        if isinstance(shops, list):
            print_success(f"Successfully retrieved {len(shops)} shops")
            
            # Save the first shop ID for later tests
            if shops:
                test_shop_id = shops[0]["shop_id"]
                print_info(f"Using shop_id {test_shop_id} for future tests")
        else:
            print_failure("Response is not a list of shops")
    
    # Test getting specific shop
    if 'test_shop_id' in globals():
        print_subheader(f"Getting specific shop (ID: {test_shop_id})")
        response = make_request("get", "getShops", params={"shopId": test_shop_id})
        if response:
            shop = response.json()
            if isinstance(shop, list) and len(shop) == 1 and shop[0]["shop_id"] == test_shop_id:
                print_success(f"Successfully retrieved specific shop: {shop[0]['name']}")
            else:
                print_failure("Failed to retrieve the specific shop")
    
    # Test with non-existent shop ID
    print_subheader("Getting non-existent shop")
    response = make_request("get", "getShops", params={"shopId": 99999})
    if response:
        shop = response.json()
        if isinstance(shop, list) and len(shop) == 0:
            print_success("Empty list returned for non-existent shop")
        else:
            print_failure("Unexpected response for non-existent shop")
    
    # Test with invalid shop ID
    print_subheader("Getting shop with invalid ID")
    response = make_request("get", "getShops", params={"shopId": "invalid"}, error_ok=True)
    if response:
        # API should either return an empty list or an error
        if (response.status_code != 200) or (response.json() == []):
            print_success("Properly handled invalid shop ID")
        else:
            print_failure("Failed to handle invalid shop ID properly")

def test_get_products():
    """Test the getProducts endpoint."""
    print_header("Testing GET /getProducts")
    
    # Test getting all products
    print_subheader("Getting all products")
    response = make_request("get", "getProducts")
    if response:
        products = response.json()
        if isinstance(products, list):
            print_success(f"Successfully retrieved {len(products)} products")
            
            # Save a product ID for later tests
            if products:
                global test_product_id
                test_product_id = products[0]["product_id"]
                global test_product_category
                test_product_category = products[0]["category"]
                print_info(f"Using product_id {test_product_id} for future tests")
                print_info(f"Using category '{test_product_category}' for future tests")
        else:
            print_failure("Response is not a list of products")
    
    # Test getting specific product
    if 'test_product_id' in globals():
        print_subheader(f"Getting specific product (ID: {test_product_id})")
        response = make_request("get", "getProducts", params={"productId": test_product_id})
        if response:
            product = response.json()
            if isinstance(product, list) and len(product) == 1 and product[0]["product_id"] == test_product_id:
                print_success(f"Successfully retrieved specific product: {product[0]['name']}")
            else:
                print_failure("Failed to retrieve the specific product")
    
    # Test getting products by category
    if 'test_product_category' in globals():
        print_subheader(f"Getting products by category ('{test_product_category}')")
        response = make_request("get", "getProducts", params={"category": test_product_category})
        if response:
            products = response.json()
            if isinstance(products, list) and all(p["category"] == test_product_category for p in products):
                print_success(f"Successfully retrieved {len(products)} products in category '{test_product_category}'")
            else:
                print_failure(f"Failed to filter products by category '{test_product_category}'")
    
    # Test with non-existent product ID
    print_subheader("Getting non-existent product")
    response = make_request("get", "getProducts", params={"productId": 99999})
    if response:
        product = response.json()
        if isinstance(product, list) and len(product) == 0:
            print_success("Empty list returned for non-existent product")
        else:
            print_failure("Unexpected response for non-existent product")
    
    # Test with non-existent category
    print_subheader("Getting products with non-existent category")
    response = make_request("get", "getProducts", params={"category": "non_existent_category"})
    if response:
        products = response.json()
        if isinstance(products, list) and len(products) == 0:
            print_success("Empty list returned for non-existent category")
        else:
            print_failure("Unexpected response for non-existent category")

def test_get_availability():
    """Test the getAvailability endpoint."""
    print_header("Testing GET /getAvailability")
    
    # Define test scenarios
    scenarios = [
        {"name": "All inventory", "params": {}},
    ]
    
    # Add test scenarios if we have shop_id and product_id from previous tests
    if 'test_shop_id' in globals():
        scenarios.append({
            "name": f"Inventory for shop {test_shop_id}", 
            "params": {"shopId": test_shop_id}
        })
    
    if 'test_product_id' in globals():
        scenarios.append({
            "name": f"Availability for product {test_product_id}", 
            "params": {"productId": test_product_id}
        })
    
    if 'test_shop_id' in globals() and 'test_product_id' in globals():
        scenarios.append({
            "name": f"Specific product {test_product_id} in shop {test_shop_id}", 
            "params": {"shopId": test_shop_id, "productId": test_product_id}
        })
        scenarios.append({
            "name": "Products with quantity >= 5", 
            "params": {"inStock": 5}
        })
    
    # Run all test scenarios
    for scenario in scenarios:
        print_subheader(scenario["name"])
        response = make_request("get", "getAvailability", params=scenario["params"])
        if response:
            inventory = response.json()
            if isinstance(inventory, list):
                print_success(f"Successfully retrieved {len(inventory)} inventory items")
                
                # Save data for test_create_order if we have both shop and product ids
                if scenario["name"].startswith("Specific product") and inventory:
                    global test_inventory_item
                    test_inventory_item = inventory[0]
                    print_info(f"Saved inventory item for future order test")
            else:
                print_failure("Response is not a list of inventory items")
    
    # Test with non-existent shop ID
    print_subheader("Getting availability for non-existent shop")
    response = make_request("get", "getAvailability", params={"shopId": 99999})
    if response:
        inventory = response.json()
        if isinstance(inventory, list) and len(inventory) == 0:
            print_success("Empty list returned for non-existent shop")
        else:
            print_failure("Unexpected response for non-existent shop")
    
    # Test with non-existent product ID
    print_subheader("Getting availability for non-existent product")
    response = make_request("get", "getAvailability", params={"productId": 99999})
    if response:
        inventory = response.json()
        if isinstance(inventory, list) and len(inventory) == 0:
            print_success("Empty list returned for non-existent product")
        else:
            print_failure("Unexpected response for non-existent product")
    
    # Test with invalid shop ID type
    print_subheader("Getting availability with invalid shop ID")
    response = make_request("get", "getAvailability", params={"shopId": "invalid"}, error_ok=True)
    # We don't validate the response here since the server should either return an empty list or an error

def test_create_user():
    """Test the createUser endpoint."""
    print_header("Testing POST /createUser")
    
    # Generate unique email for testing
    timestamp = datetime.now().strftime("%Y%m%d%H%M%S")
    random_suffix = random.randint(1000, 9999)
    test_email = f"test.user{timestamp}{random_suffix}@example.com"
    
    # Create a standard customer
    print_subheader("Creating a customer")
    customer_data = {
        "email": test_email,
        "password": "password123",
        "firstName": "Test",
        "lastName": "Customer",
        "phoneNumber": f"+1555{random.randint(1000000, 9999999)}",
        "userType": "customer",
        "address": {
            "civic": 123,
            "street": "Main Street",
            "city": "Test City",
            "postalCode": "A1B 2C3"
        }
    }
    
    response = make_request("post", "createUser", json=customer_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully created customer user")
        # Save user ID for later tests
        global test_customer_id
        test_customer_id = response.json().get("userId")
        global test_customer_email
        test_customer_email = test_email
        global test_customer_password
        test_customer_password = "password123"
        print_info(f"Created user ID: {test_customer_id}")
    else:
        print_failure("Failed to create customer user")
    
    # Create a deliverer
    print_subheader("Creating a deliverer")
    test_email_deliverer = f"test.deliverer{timestamp}{random_suffix}@example.com"
    deliverer_data = {
        "email": test_email_deliverer,
        "password": "password123",
        "firstName": "Test",
        "lastName": "Deliverer",
        "phoneNumber": f"+1555{random.randint(1000000, 9999999)}",
        "userType": "deliverer",
        "licensePlate": f"ABC{random.randint(100, 999)}",
        "carBrand": "Toyota",
        "civic": 456,
        "street": "Delivery Avenue",
        "city": "Test City",
        "postalCode": "X9Y 8Z7"
    }
    
    response = make_request("post", "createUser", json=deliverer_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully created deliverer user")
        global test_deliverer_id
        test_deliverer_id = response.json().get("userId")
        print_info(f"Created deliverer ID: {test_deliverer_id}")
    else:
        print_failure("Failed to create deliverer user")
    
    # Test duplicate email
    print_subheader("Testing duplicate email")
    response = make_request("post", "createUser", json=customer_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected duplicate email")
    else:
        print_failure("Failed to reject duplicate email")
    
    # Test missing required fields
    print_subheader("Testing missing required fields")
    incomplete_data = {
        "email": f"incomplete{timestamp}@example.com",
        "lastName": "Incomplete"
        # Missing other required fields
    }
    response = make_request("post", "createUser", json=incomplete_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected incomplete data")
    else:
        print_failure("Failed to reject incomplete data")
    
    # Test invalid user type
    print_subheader("Testing invalid user type")
    invalid_type_data = {
        "email": f"invalidtype{timestamp}@example.com",
        "password": "password123",
        "firstName": "Invalid",
        "lastName": "Type",
        "phoneNumber": f"+1555{random.randint(1000000, 9999999)}",
        "userType": "invalid_type"
    }
    response = make_request("post", "createUser", json=invalid_type_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected invalid user type")
    else:
        print_failure("Failed to reject invalid user type")
    
    # Test deliverer without license plate
    print_subheader("Testing deliverer without license plate")
    invalid_deliverer_data = {
        "email": f"invalid.deliverer{timestamp}@example.com",
        "password": "password123",
        "firstName": "Invalid",
        "lastName": "Deliverer",
        "phoneNumber": f"+1555{random.randint(1000000, 9999999)}",
        "userType": "deliverer"
        # Missing license plate
    }
    response = make_request("post", "createUser", json=invalid_deliverer_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected deliverer without license plate")
    else:
        print_failure("Failed to reject deliverer without license plate")

def test_connect_user():
    """Test the connectUser endpoint."""
    print_header("Testing POST /connectUser")
    
    # Test with valid admin credentials
    print_subheader("Testing admin login")
    admin_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD
    }
    response = make_request("post", "connectUser", json=admin_data)
    if response and response.json().get("status") == "success":
        admin_info = response.json().get("user")
        if admin_info and admin_info.get("userType") == "admin":
            print_success("Successfully connected as admin")
            global admin_user_id
            admin_user_id = admin_info.get("userId")
            print_info(f"Admin user ID: {admin_user_id}")
        else:
            print_failure("Connected user is not an admin")
    else:
        print_failure("Failed to connect as admin")
    
    # Test with customer credentials (if available from previous test)
    if 'test_customer_email' in globals() and 'test_customer_password' in globals():
        print_subheader("Testing customer login")
        customer_data = {
            "email": test_customer_email,
            "password": test_customer_password
        }
        response = make_request("post", "connectUser", json=customer_data)
        if response and response.json().get("status") == "success":
            customer_info = response.json().get("user")
            if customer_info and customer_info.get("userType") == "customer":
                print_success("Successfully connected as customer")
                # Verify the ID matches what we got earlier
                if customer_info.get("userId") == test_customer_id:
                    print_success("User ID matches previously created user")
                else:
                    print_failure("User ID mismatch")
            else:
                print_failure("Connected user is not a customer")
        else:
            print_failure("Failed to connect as customer")
    
    # Test with invalid email
    print_subheader("Testing invalid email")
    invalid_email_data = {
        "email": "nonexistent@example.com",
        "password": "wrongpassword"
    }
    response = make_request("post", "connectUser", json=invalid_email_data, expected_status=401, error_ok=True)
    if response and response.status_code == 401 and response.json().get("status") == "error":
        print_success("Properly rejected invalid credentials")
    else:
        print_failure("Failed to reject invalid credentials")
    
    # Test with missing fields
    print_subheader("Testing missing fields")
    incomplete_data = {
        "email": ADMIN_EMAIL
        # Missing password
    }
    response = make_request("post", "connectUser", json=incomplete_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected incomplete data")
    else:
        print_failure("Failed to reject incomplete data")

def test_create_order():
    """Test the createOrder endpoint."""
    print_header("Testing POST /createOrder")
    
    # Skip if we don't have the necessary data from previous tests
    if not all(var in globals() for var in ['test_customer_id', 'test_shop_id', 'test_product_id']):
        print_info("Skipping create order test due to missing test data from previous tests")
        return
    
    # Create an order
    print_subheader("Creating an order")
    
    # Generate a random card number
    card_number = ''.join([str(random.randint(0, 9)) for _ in range(16)])
    
    order_data = {
        "userId": test_customer_id,
        "shopId": test_shop_id,
        "items": [
            {
                "productId": test_product_id,
                "quantity": 1
            }
        ],
        "paymentMethod": "Credit Card",
        "cardName": "Test Customer",
        "cardNumber": card_number,
        "cvcCard": 123,
        "expiryDateMonth": 12,
        "expiryDateYear": 25,
        # Use existing address from customer
    }
    
    response = make_request("post", "createOrder", json=order_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully created order")
        # Save order ID for later tests
        global test_order_id
        test_order_id = response.json().get("orderId")
        print_info(f"Created order ID: {test_order_id}")
    else:
        print_failure("Failed to create order")
    
    # Try creating an order with insufficient quantity
    if 'test_inventory_item' in globals():
        print_subheader("Testing order with excessive quantity")
        # Get the available quantity and add 10 to it
        available_qty = test_inventory_item.get("quantity", 1)
        excessive_qty = available_qty + 10
        
        excessive_order_data = order_data.copy()
        excessive_order_data["items"] = [
            {
                "productId": test_product_id,
                "quantity": excessive_qty
            }
        ]
        
        response = make_request("post", "createOrder", json=excessive_order_data, expected_status=400, error_ok=True)
        if response and response.status_code == 400 and response.json().get("status") == "error":
            print_success("Properly rejected order with excessive quantity")
        else:
            print_failure("Failed to reject order with excessive quantity")
    
    # Test with missing required fields
    print_subheader("Testing missing required fields")
    incomplete_order_data = {
        "userId": test_customer_id,
        "shopId": test_shop_id,
        # Missing other required fields
    }
    response = make_request("post", "createOrder", json=incomplete_order_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected incomplete order data")
    else:
        print_failure("Failed to reject incomplete order data")
    
    # Test with non-existent user
    print_subheader("Testing order with non-existent user")
    nonexistent_user_data = order_data.copy()
    nonexistent_user_data["userId"] = 99999
    
    response = make_request("post", "createOrder", json=nonexistent_user_data, expected_status=404, error_ok=True)
    if response and response.status_code == 404 and response.json().get("status") == "error":
        print_success("Properly rejected order with non-existent user")
    else:
        print_failure("Failed to reject order with non-existent user")
    
    # Test with non-existent shop
    print_subheader("Testing order with non-existent shop")
    nonexistent_shop_data = order_data.copy()
    nonexistent_shop_data["shopId"] = 99999
    
    response = make_request("post", "createOrder", json=nonexistent_shop_data, expected_status=404, error_ok=True)
    if response and response.status_code == 404 and response.json().get("status") == "error":
        print_success("Properly rejected order with non-existent shop")
    else:
        print_failure("Failed to reject order with non-existent shop")

def test_cancel_order():
    """Test the cancelOrder endpoint."""
    print_header("Testing POST /cancelOrder")
    
    # Skip if we don't have an order ID from previous test
    if 'test_order_id' not in globals():
        print_info("Skipping cancel order test due to missing test order ID from previous test")
        return
    
    # Cancel the order
    print_subheader("Cancelling an order")
    cancel_data = {
        "orderId": test_order_id
    }
    
    response = make_request("post", "cancelOrder", json=cancel_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully cancelled order")
    else:
        print_failure("Failed to cancel order")
    
    # Try cancelling the same order again
    print_subheader("Testing cancelling an already cancelled order")
    response = make_request("post", "cancelOrder", json=cancel_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected cancelling an already cancelled order")
    else:
        print_failure("Failed to reject cancelling an already cancelled order")
    
    # Test with non-existent order ID
    print_subheader("Testing non-existent order ID")
    nonexistent_order_data = {
        "orderId": 99999
    }
    response = make_request("post", "cancelOrder", json=nonexistent_order_data, expected_status=404, error_ok=True)
    if response and response.status_code == 404 and response.json().get("status") == "error":
        print_success("Properly rejected non-existent order ID")
    else:
        print_failure("Failed to reject non-existent order ID")
    
    # Test with missing order ID
    print_subheader("Testing missing order ID")
    missing_order_data = {}
    response = make_request("post", "cancelOrder", json=missing_order_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400 and response.json().get("status") == "error":
        print_success("Properly rejected missing order ID")
    else:
        print_failure("Failed to reject missing order ID")



# Test functions for each endpoint
def test_admin_create_user():
    print("\n=== Testing admin_create_user endpoint ===")
    global created_user_ids
    created_user_ids = []
    # Test case 1: Create a normal client user
    client_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"test_client_{random_string()}@example.com",
        "user_password": "testpassword123",
        "user_firstName": "TestClient",
        "user_lastName": "User",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "customer",
        "address": {
            "civic": 123,
            "street": "Main St",
            "city": "Montreal",
            "postalCode": "H1H1H1"
        }
    }
    
    response = make_request("admin/createUser", method="POST", data=client_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        created_user_ids.append(response.json().get("userId"))
        print(f"✅ Successfully created client user with ID: {created_user_ids[-1]}")
    else:
        print("❌ Failed to create client user")
    
    # Test case 2: Create a deliverer
    deliverer_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"test_deliverer_{random_string()}@example.com",
        "user_password": "testpassword123",
        "user_firstName": "TestDeliverer",
        "user_lastName": "User",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "deliverer",
        "licensePlate": f"TEST-{random_string(3)}",
        "carBrand": "Honda",
        "address": {
            "civic": 456,
            "street": "Delivery St",
            "city": "Montreal",
            "postalCode": "H2H2H2"
        }
    }
    
    response = make_request("admin/createUser", method="POST", data=deliverer_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        created_user_ids.append(response.json().get("userId"))
        print(f"✅ Successfully created deliverer user with ID: {created_user_ids[-1]}")
    else:
        print("❌ Failed to create deliverer user")
    
    # Test case 3: Create another admin
    admin_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"test_admin_{random_string()}@example.com",
        "user_password": "adminpassword123",
        "user_firstName": "TestAdmin",
        "user_lastName": "User",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "admin"
    }
    
    response = make_request("admin/createUser", method="POST", data=admin_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        created_user_ids.append(response.json().get("userId"))
        print(f"✅ Successfully created admin user with ID: {created_user_ids[-1]}")
    else:
        print("❌ Failed to create admin user")
    
    # Test case 4: Test with invalid admin credentials
    invalid_admin_data = {
        "adminEmail": "wrong@email.com",
        "adminPassword": "wrongpassword",
        "user_email": f"test_user_{random_string()}@example.com",
        "user_password": "testpassword",
        "user_firstName": "Test",
        "user_lastName": "User",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "customer"
    }
    
    response = make_request("admin/createUser", method="POST", data=invalid_admin_data)
    if response and response.status_code != 200:
        print("✅ Security check passed - rejected invalid admin credentials")
    else:
        print("❌ Security check failed - accepted invalid admin credentials")
    
    # Test case 5: Missing required fields
    missing_fields_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"test_user_{random_string()}@example.com",
        # Missing password and other required fields
    }
    
    response = make_request("admin/createUser", method="POST", data=missing_fields_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected incomplete data")
    else:
        print("❌ Validation check failed - accepted incomplete data")
    
    # Test case 6: Invalid user type
    invalid_type_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"test_user_{random_string()}@example.com",
        "user_password": "testpassword123",
        "user_firstName": "Test",
        "user_lastName": "User",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "invalid_type"  # Invalid user type
    }
    
    response = make_request("admin/createUser", method="POST", data=invalid_type_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected invalid user type")
    else:
        print("❌ Validation check failed - accepted invalid user type")
    
    # Test case 7: Carrier without license plate
    invalid_carrier_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"test_carrier_{random_string()}@example.com",
        "user_password": "testpassword123",
        "user_firstName": "Test",
        "user_lastName": "Carrier",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "deliverer"
        # Missing license plate which is required for carriers
    }
    
    response = make_request("admin/createUser", method="POST", data=invalid_carrier_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected carrier without license plate")
    else:
        print("❌ Validation check failed - accepted carrier without license plate")

def test_admin_modify_user():
    print("\n=== Testing admin_modify_user endpoint ===")
    
    if not created_user_ids:
        print("❌ No users available to modify. Skipping test.")
        return
    
    user_id = created_user_ids[0]
    
    # Test case 1: Update basic user information
    update_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "userId": user_id,
        "firstName": f"Modified{random_string()}",
        "lastName": f"UserUpdated{random_string()}",
        "phoneNumber": f"+1{random.randint(1000000000, 9999999999)}"
    }
    
    response = make_request("admin/modifyUser", method="POST", data=update_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        print(f"✅ Successfully updated user {user_id}")
    else:
        print(f"❌ Failed to update user {user_id}")
    
    # Test case 2: Update address
    address_update_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "userId": user_id,
        "address": {
            "civic": 789,
            "apartment": "B",
            "street": "Updated St",
            "city": "Montreal Updated",
            "postalCode": "H3H3H3"
        }
    }
    
    response = make_request("admin/modifyUser", method="POST", data=address_update_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        print(f"✅ Successfully updated user address for user {user_id}")
    else:
        print(f"❌ Failed to update user address for user {user_id}")
    
    # Test case 3: Invalid user ID
    invalid_id_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "userId": 99999,  # Assuming this ID doesn't exist
        "firstName": "Invalid",
        "lastName": "Update"
    }
    
    response = make_request("admin/modifyUser", method="POST", data=invalid_id_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected invalid user ID")
    else:
        print("❌ Validation check failed - accepted invalid user ID")
    
    # Test case 4: Invalid admin credentials
    invalid_admin_data = {
        "adminEmail": "wrong@email.com",
        "adminPassword": "wrongpassword",
        "userId": user_id,
        "firstName": "Invalid",
        "lastName": "Admin"
    }
    
    response = make_request("admin/modifyUser", method="POST", data=invalid_admin_data)
    if response and response.status_code != 200:
        print("✅ Security check passed - rejected invalid admin credentials")
    else:
        print("❌ Security check failed - accepted invalid admin credentials")
    
    # Test case 5: Change user type
    if len(created_user_ids) > 1:
        user_type_data = {
            "adminEmail": ADMIN_EMAIL,
            "adminPassword": ADMIN_PASSWORD,
            "userId": created_user_ids[1],
            "userType": "customer",  # Change from deliverer to customer
            "licensePlate": None  # Remove license plate as no longer needed
        }
        
        response = make_request("admin/modifyUser", method="POST", data=user_type_data)
        if response and response.status_code == 200 and response.json().get("status") == "success":
            print(f"✅ Successfully changed user type for user {created_user_ids[1]}")
        else:
            print(f"❌ Failed to change user type for user {created_user_ids[1]}")

def test_admin_delete_user():
    print("\n=== Testing admin_delete_user endpoint ===")
    
    if not created_user_ids:
        print("❌ No users available to delete. Skipping test.")
        return
    
    # Create a temporary user to delete
    temp_user_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "user_email": f"temp_user_{random_string()}@example.com",
        "user_password": "temppassword123",
        "user_firstName": "Temporary",
        "user_lastName": "User",
        "user_phone": f"+1{random.randint(1000000000, 9999999999)}",
        "user_type": "customer"
    }
    
    create_response = make_request("admin/createUser", method="POST", data=temp_user_data)
    if create_response and create_response.status_code == 200:
        temp_user_id = create_response.json().get("userId")
        print(f"Created temporary user with ID: {temp_user_id}")
        
        # Test case 1: Delete the temporary user
        delete_data = {
            "adminEmail": ADMIN_EMAIL,
            "adminPassword": ADMIN_PASSWORD,
            "userId": temp_user_id
        }
        
        response = make_request("admin/deleteUser", method="POST", data=delete_data)
        if response and response.status_code == 200 and response.json().get("status") == "success":
            print(f"✅ Successfully deleted user {temp_user_id}")
        else:
            print(f"❌ Failed to delete user {temp_user_id}")
            
        # Test case 2: Verify the user is deleted by trying to delete again
        response = make_request("admin/deleteUser", method="POST", data=delete_data)
        if response and response.status_code != 200:
            print("✅ Verification passed - user was actually deleted")
        else:
            print("❌ Verification failed - user might not be properly deleted")
    else:
        print("❌ Failed to create temporary user for deletion test")
    
    # Test case 3: Invalid user ID
    invalid_id_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "userId": 99999  # Assuming this ID doesn't exist
    }
    
    response = make_request("admin/deleteUser", method="POST", data=invalid_id_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected invalid user ID")
    else:
        print("❌ Validation check failed - accepted invalid user ID")
    
    # Test case 4: Invalid admin credentials
    invalid_admin_data = {
        "adminEmail": "wrong@email.com",
        "adminPassword": "wrongpassword",
        "userId": created_user_ids[0] if created_user_ids else 1
    }
    
    response = make_request("admin/deleteUser", method="POST", data=invalid_admin_data)
    if response and response.status_code != 200:
        print("✅ Security check passed - rejected invalid admin credentials")
    else:
        print("❌ Security check failed - accepted invalid admin credentials")
    
    # Test case 5: Missing user ID
    missing_id_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD
        # Missing userId
    }
    
    response = make_request("admin/deleteUser", method="POST", data=missing_id_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected request with missing user ID")
    else:
        print("❌ Validation check failed - accepted request with missing user ID")

def test_admin_create_shop():
    print("\n=== Testing admin_create_shop endpoint ===")
    global created_shop_ids
    created_shop_ids = []
    # Test case 1: Create a valid shop
    shop_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "name": f"Test Shop {random_string()}",
        "civic": random.randint(100, 999),
        "street": f"{random_string()} Street",
        "city": "Montreal",
        "postalCode": "H1H 1H1"
    }
    
    response = make_request("admin/createShop", method="POST", data=shop_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        created_shop_ids.append(response.json().get("shopId"))
        print(f"✅ Successfully created shop with ID: {created_shop_ids[-1]}")
    else:
        print("❌ Failed to create shop")
    
    # Test case 2: Create another shop with apartment
    shop_data_2 = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "name": f"Test Shop with Apt {random_string()}",
        "civic": random.randint(100, 999),
        "apartment": f"{random.randint(1, 100)}",
        "street": f"{random_string()} Avenue",
        "city": "Quebec",
        "postalCode": "G1G 1G1"
    }
    
    response = make_request("admin/createShop", method="POST", data=shop_data_2)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        created_shop_ids.append(response.json().get("shopId"))
        print(f"✅ Successfully created shop with apartment with ID: {created_shop_ids[-1]}")
    else:
        print("❌ Failed to create shop with apartment")
    
    # Test case 3: Missing required fields
    missing_fields_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "name": f"Incomplete Shop {random_string()}"
        # Missing civic, street, city
    }
    
    response = make_request("admin/createShop", method="POST", data=missing_fields_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected incomplete shop data")
    else:
        print("❌ Validation check failed - accepted incomplete shop data")
    
    # Test case 4: Invalid admin credentials
    invalid_admin_data = {
        "adminEmail": "wrong@email.com",
        "adminPassword": "wrongpassword",
        "name": f"Invalid Admin Shop {random_string()}",
        "civic": random.randint(100, 999),
        "street": f"{random_string()} Boulevard",
        "city": "Toronto",
        "postalCode": "M1M 1M1"
    }
    
    response = make_request("admin/createShop", method="POST", data=invalid_admin_data)
    if response and response.status_code != 200:
        print("✅ Security check passed - rejected invalid admin credentials")
    else:
        print("❌ Security check failed - accepted invalid admin credentials")
    
    # Test case 5: Extra long shop name (boundary testing)
    long_name_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "name": "Super " + "".join(["Long " for _ in range(50)]) + "Shop Name",
        "civic": random.randint(100, 999),
        "street": f"{random_string()} Road",
        "city": "Montreal",
        "postalCode": "H2H 2H2"
    }
    
    response = make_request("admin/createShop", method="POST", data=long_name_data)
    if response:
        if response.status_code == 200 and response.json().get("status") == "success":
            created_shop_ids.append(response.json().get("shopId"))
            print(f"✅ Successfully created shop with long name with ID: {created_shop_ids[-1]}")
        elif response.status_code != 200:
            print("✅ System properly handled overly long shop name")
        else:
            print("❌ Unexpected behavior with long shop name")

def test_admin_modify_shop():
    print("\n=== Testing admin_modify_shop endpoint ===")
    
    if not created_shop_ids:
        print("❌ No shops available to modify. Skipping test.")
        return
    
    shop_id = created_shop_ids[0]
    
    # Test case 1: Update shop name
    name_update_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "shopId": shop_id,
        "name": f"Updated Shop Name {random_string()}"
    }
    
    response = make_request("admin/modifyShop", method="POST", data=name_update_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        print(f"✅ Successfully updated shop name for shop {shop_id}")
    else:
        print(f"❌ Failed to update shop name for shop {shop_id}")
    
    # Test case 2: Update shop address
    address_update_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "shopId": shop_id,
        "civic": random.randint(100, 999),
        "street": f"Updated {random_string()} Street",
        "city": "Updated City",
        "postalCode": "U1U 1U1"
    }
    
    response = make_request("admin/modifyShop", method="POST", data=address_update_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        print(f"✅ Successfully updated shop address for shop {shop_id}")
    else:
        print(f"❌ Failed to update shop address for shop {shop_id}")
    
    # Test case 3: Update both name and address
    full_update_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "shopId": shop_id,
        "name": f"Fully Updated Shop {random_string()}",
        "civic": random.randint(100, 999),
        "apartment": f"{random.randint(1, 100)}",
        "street": f"Fully Updated {random_string()} Avenue",
        "city": "New City",
        "postalCode": "N1N 1N1"
    }
    
    response = make_request("admin/modifyShop", method="POST", data=full_update_data)
    if response and response.status_code == 200 and response.json().get("status") == "success":
        print(f"✅ Successfully performed full update for shop {shop_id}")
    else:
        print(f"❌ Failed to perform full update for shop {shop_id}")
    
    # Test case 4: Invalid shop ID
    invalid_id_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "shopId": 99999,  # Assuming this ID doesn't exist
        "name": "Invalid Shop Update"
    }
    
    response = make_request("admin/modifyShop", method="POST", data=invalid_id_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected invalid shop ID")
    else:
        print("❌ Validation check failed - accepted invalid shop ID")
    
    # Test case 5: Invalid admin credentials
    invalid_admin_data = {
        "adminEmail": "wrong@email.com",
        "adminPassword": "wrongpassword",
        "shopId": shop_id,
        "name": "Invalid Admin Update"
    }
    
    response = make_request("admin/modifyShop", method="POST", data=invalid_admin_data)
    if response and response.status_code != 200:
        print("✅ Security check passed - rejected invalid admin credentials")
    else:
        print("❌ Security check failed - accepted invalid admin credentials")
    
    # Test case 6: Missing shop ID
    missing_id_data = {
        "adminEmail": ADMIN_EMAIL,
        "adminPassword": ADMIN_PASSWORD,
        "name": "Missing ID Update"
    }
    
    response = make_request("admin/modifyShop", method="POST", data=missing_id_data)
    if response and response.status_code != 200:
        print("✅ Validation check passed - rejected request with missing shop ID")
    else:
        print("❌ Validation check failed - accepted request with missing shop ID")

#!/usr/bin/env python3
import requests
import json
import time
import random
import sys
from datetime import datetime

# Configuration
BASE_URL = "http://4.172.255.120:5000"
ADMIN_EMAIL = "tristan@boozy.com"
ADMIN_PASSWORD = "adminpass"
DELAY_BETWEEN_REQUESTS = 0.5  # in seconds

# Color codes for terminal output
class Colors:
    GREEN = '\033[92m'
    YELLOW = '\033[93m'
    RED = '\033[91m'
    BLUE = '\033[94m'
    MAGENTA = '\033[95m'
    CYAN = '\033[96m'
    ENDC = '\033[0m'

# Test result tracking
results = {
    "total": 0,
    "passed": 0,
    "failed": 0
}

def print_header(message):
    """Print a formatted header message."""
    print(f"\n{Colors.BLUE}{'=' * 80}{Colors.ENDC}")
    print(f"{Colors.BLUE}== {message}{Colors.ENDC}")
    print(f"{Colors.BLUE}{'=' * 80}{Colors.ENDC}")

def print_subheader(message):
    """Print a formatted subheader message."""
    print(f"\n{Colors.CYAN}----- {message} -----{Colors.CYAN}{Colors.ENDC}")

def print_success(message):
    """Print a success message."""
    print(f"{Colors.GREEN}✓ {message}{Colors.ENDC}")
    results["passed"] += 1
    results["total"] += 1

def print_failure(message, details=None):
    """Print a failure message with optional details."""
    print(f"{Colors.RED}✗ {message}{Colors.ENDC}")
    if details:
        print(f"{Colors.YELLOW}  Details: {details}{Colors.ENDC}")
    results["failed"] += 1
    results["total"] += 1

def print_info(message):
    """Print an informational message."""
    print(f"{Colors.MAGENTA}ℹ {message}{Colors.ENDC}")

def make_request(method, endpoint, json=None, params=None, expected_status=200, error_ok=False):
    """
    Make an HTTP request and handle the response.
    
    Args:
        method: HTTP method (get, post, etc.)
        endpoint: API endpoint to call
        json: JSON data for request body
        params: URL parameters
        expected_status: Expected HTTP status code
        error_ok: Whether an error response is acceptable
        
    Returns:
        Response object or None if failed
    """
    url = f"{BASE_URL}/{endpoint}"
    print_info(f"Requesting {method.upper()} {url}")
    
    if json:
        print_info(f"Request data: {json}")
    
    try:
        response = getattr(requests, method.lower())(url, json=json, params=params)
        
        # Print response details
        print_info(f"Response status: {response.status_code}")
        try:
            response_json = response.json()
            print_info(f"Response data: {response_json}")
        except:
            print_info(f"Response content: {response.text[:200]}")
        
        # Check if status code matches expected
        if response.status_code == expected_status:
            return response
        else:
            if not error_ok:
                print_failure(f"Expected status {expected_status}, got {response.status_code}")
            return response if error_ok else None
    except Exception as e:
        print_failure(f"Request failed", str(e))
        return None
    finally:
        # Add delay between requests
        time.sleep(DELAY_BETWEEN_REQUESTS)

def test_admin_delete_shop():
    print_header("Testing /admin/deleteShop endpoint")
    
    # Create a shop first to ensure we have something to delete
    print_subheader("Creating a test shop to delete")
    create_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": f"Test Delete Shop {random.randint(1000, 9999)}",
        "civic": f"{random.randint(100, 999)}",
        "street": "Test Street",
        "city": "Test City",
        "postalCode": "H1H 1H1"
    }
    
    create_response = make_request("post", "admin/createShop", json=create_data)
    if not create_response:
        print_failure("Could not create shop for deletion test")
        return
    
    shop_id = create_response.json().get("shopId")
    print_info(f"Created shop with ID: {shop_id}")
    
    # Test case 1: Delete shop with valid credentials
    print_subheader("Test case 1: Deleting shop with valid credentials")
    delete_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "shopId": shop_id
    }
    
    response = make_request("post", "admin/deleteShop", json=delete_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully deleted shop")
    else:
        print_failure("Failed to delete shop")
    
    # Test case 2: Attempt to delete already deleted shop (should fail)
    print_subheader("Test case 2: Attempting to delete non-existent shop")
    response = make_request("post", "admin/deleteShop", json=delete_data, error_ok=True)
    if response and response.json().get("status") == "error":
        print_success("Correctly returned error for non-existent shop")
    else:
        print_failure("Should have returned error for non-existent shop")
    
    # Test case 3: Attempt to delete with invalid admin credentials
    print_subheader("Test case 3: Attempting to delete with invalid credentials")
    invalid_data = {
        "email": "fake@example.com",
        "password": "wrongpassword",
        "shopId": shop_id
    }
    
    response = make_request("post", "admin/deleteShop", json=invalid_data, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly denied access with invalid credentials")
    else:
        print_failure("Should have denied access with invalid credentials")
    
    # Test case 4: Attempt to delete with missing shopId
    print_subheader("Test case 4: Attempting to delete with missing shopId")
    missing_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD
    }
    
    response = make_request("post", "admin/deleteShop", json=missing_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400:
        print_success("Correctly rejected request with missing shopId")
    else:
        print_failure("Should have rejected request with missing shopId")

def test_admin_create_product():
    global test_product_id
    global extreme_product_id
    print_header("Testing /admin/createProduct endpoint")
    
    # Test case 1: Create product with valid credentials and data
    print_subheader("Test case 1: Creating product with valid data")
    product_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": f"Test Beverage {random.randint(1000, 9999)}",
        "description": "A test product for API validation",
        "price": round(random.uniform(10.0, 50.0), 2),
        "category": "beer",
        "alcohol": round(random.uniform(4.0, 12.0), 1)
    }
    
    response = make_request("post", "admin/createProduct", json=product_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully created product")
        product_id = response.json().get("productId")
        print_info(f"Created product with ID: {product_id}")
        # Store product ID for later tests
        test_product_id = product_id
    else:
        print_failure("Failed to create product")
        test_product_id = None
    
    # Test case 2: Create product with missing required fields
    print_subheader("Test case 2: Creating product with missing required fields")
    invalid_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": "Incomplete Product"
        # Missing price, category, and alcohol
    }
    
    response = make_request("post", "admin/createProduct", json=invalid_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400:
        print_success("Correctly rejected product with missing fields")
    else:
        print_failure("Should have rejected product with missing fields")
    
    # Test case 3: Create product with invalid admin credentials
    print_subheader("Test case 3: Creating product with invalid credentials")
    invalid_creds = {
        "email": "fake@example.com",
        "password": "wrongpassword",
        "name": "Unauthorized Product",
        "price": 15.99,
        "category": "wine",
        "alcohol": 13.5
    }
    
    response = make_request("post", "admin/createProduct", json=invalid_creds, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly denied access with invalid credentials")
    else:
        print_failure("Should have denied access with invalid credentials")
    
    # Test case 4: Create product with extreme values
    print_subheader("Test case 4: Creating product with extreme values")
    extreme_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": "Extreme " + "X" * 50,  # Very long name
        "description": "X" * 1000,  # Very long description
        "price": 9999.99,  # High price
        "category": "spirits",
        "alcohol": 99.9  # Maximum alcohol percentage
    }
    
    response = make_request("post", "admin/createProduct", json=extreme_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully created product with extreme values")
        extreme_product_id = response.json().get("productId")
        print_info(f"Created extreme product with ID: {extreme_product_id}")
    else:
        print_failure("Failed to create product with extreme values")
        extreme_product_id = None

def test_admin_modify_product():
    print_header("Testing /admin/modifyProduct endpoint")
    
    if not test_product_id:
        print_failure("No test product available to modify")
        return
    
    # Test case 1: Modify product with valid credentials and data
    print_subheader("Test case 1: Modifying product with valid data")
    update_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "productId": test_product_id,
        "name": f"Modified Product {random.randint(1000, 9999)}",
        "description": "This product has been modified",
        "price": round(random.uniform(10.0, 50.0), 2),
        "category": "wine",
        "alcohol": round(random.uniform(4.0, 12.0), 1)
    }
    
    response = make_request("post", "admin/modifyProduct", json=update_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully modified product")
    else:
        print_failure("Failed to modify product")
    
    # Test case 2: Modify product with partial data
    print_subheader("Test case 2: Modifying product with partial data")
    partial_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "productId": test_product_id,
        "price": round(random.uniform(10.0, 50.0), 2)
    }
    
    response = make_request("post", "admin/modifyProduct", json=partial_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully modified product with partial data")
    else:
        print_failure("Failed to modify product with partial data")
    
    # Test case 3: Modify non-existent product
    print_subheader("Test case 3: Modifying non-existent product")
    nonexistent_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "productId": 9999999,  # Assuming this ID doesn't exist
        "name": "Non-existent Product",
        "price": 25.99
    }
    
    response = make_request("post", "admin/modifyProduct", json=nonexistent_data, expected_status=404, error_ok=True)
    if response and response.status_code == 404:
        print_success("Correctly returned error for non-existent product")
    else:
        print_failure("Should have returned error for non-existent product")
    
    # Test case 4: Modify product with invalid admin credentials
    print_subheader("Test case 4: Modifying product with invalid credentials")
    invalid_creds = {
        "email": "fake@example.com",
        "password": "wrongpassword",
        "productId": test_product_id,
        "name": "Unauthorized Update"
    }
    
    response = make_request("post", "admin/modifyProduct", json=invalid_creds, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly denied access with invalid credentials")
    else:
        print_failure("Should have denied access with invalid credentials")
    
    # Test case 5: Modify product with missing productId
    print_subheader("Test case 5: Modifying product with missing productId")
    missing_id = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": "Missing ID Product"
    }
    
    response = make_request("post", "admin/modifyProduct", json=missing_id, expected_status=400, error_ok=True)
    if response and response.status_code == 400:
        print_success("Correctly rejected request with missing productId")
    else:
        print_failure("Should have rejected request with missing productId")

def test_admin_delete_product():
    print_header("Testing /admin/deleteProduct endpoint")
    
    if not test_product_id:
        print_failure("No test product available to delete")
        return
    
    # Test case 1: Delete product with valid credentials
    print_subheader("Test case 1: Deleting product with valid credentials")
    delete_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "productId": test_product_id
    }
    
    response = make_request("post", "admin/deleteProduct", json=delete_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully deleted product")
    else:
        print_failure("Failed to delete product")
    
    # Test case 2: Delete extreme product if available
    if extreme_product_id:
        print_subheader("Test case 2: Deleting extreme product")
        extreme_delete_data = {
            "email": ADMIN_EMAIL,
            "password": ADMIN_PASSWORD,
            "productId": extreme_product_id
        }
        
        response = make_request("post", "admin/deleteProduct", json=extreme_delete_data)
        if response and response.json().get("status") == "success":
            print_success("Successfully deleted extreme product")
        else:
            print_failure("Failed to delete extreme product")
    
    # Test case 3: Attempt to delete already deleted product
    print_subheader("Test case 3: Attempting to delete non-existent product")
    response = make_request("post", "admin/deleteProduct", json=delete_data, expected_status=404, error_ok=True)
    if response and response.json().get("status") == "error":
        print_success("Correctly returned error for non-existent product")
    else:
        print_failure("Should have returned error for non-existent product")
    
    # Test case 4: Attempt to delete with invalid admin credentials
    print_subheader("Test case 4: Attempting to delete with invalid credentials")
    invalid_data = {
        "email": "fake@example.com",
        "password": "wrongpassword",
        "productId": 1  # Using an ID that likely exists
    }
    
    response = make_request("post", "admin/deleteProduct", json=invalid_data, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly denied access with invalid credentials")
    else:
        print_failure("Should have denied access with invalid credentials")
    
    # Test case 5: Attempt to delete with missing productId
    print_subheader("Test case 5: Attempting to delete with missing productId")
    missing_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD
    }
    
    response = make_request("post", "admin/deleteProduct", json=missing_data, expected_status=400, error_ok=True)
    if response and response.status_code == 400:
        print_success("Correctly rejected request with missing productId")
    else:
        print_failure("Should have rejected request with missing productId")

def test_admin_get_users():
    print_header("Testing /admin/getUsers endpoint")
    
    # Test case 1: Get users with valid admin credentials
    print_subheader("Test case 1: Getting users with valid credentials")
    valid_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD
    }
    
    response = make_request("post", "admin/getUsers", json=valid_data)
    if response and response.json().get("status") == "success":
        users = response.json().get("users", [])
        print_success(f"Successfully retrieved {len(users)} users")
        
        # Check if response contains essential user data
        if users and all(key in users[0] for key in ["userId", "email", "userType"]):
            print_success("User data contains required fields")
        else:
            print_failure("User data is missing required fields")
    else:
        print_failure("Failed to retrieve users")
    
    # Test case 2: Get users with invalid admin credentials
    print_subheader("Test case 2: Getting users with invalid credentials")
    invalid_data = {
        "email": "fake@example.com",
        "password": "wrongpassword"
    }
    
    response = make_request("post", "admin/getUsers", json=invalid_data, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly denied access with invalid credentials")
    else:
        print_failure("Should have denied access with invalid credentials")
    
    # Test case 3: Get users with mixed case email (testing case insensitivity)
    print_subheader("Test case 3: Getting users with mixed case email")
    mixed_case_data = {
        "email": ADMIN_EMAIL.upper(),  # Convert to uppercase
        "password": ADMIN_PASSWORD
    }
    
    response = make_request("post", "admin/getUsers", json=mixed_case_data, error_ok=True)
    if response and response.json().get("status") == "success":
        print_success("Successfully retrieved users with mixed case email")
    else:
        print_info("Email lookup might be case-sensitive")
    
    # Test case 4: SQL injection attempt in credentials
    print_subheader("Test case 4: SQL injection attempt in credentials")
    sql_injection_data = {
        "email": "' OR 1=1; --",
        "password": "anything"
    }
    
    response = make_request("post", "admin/getUsers", json=sql_injection_data, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly protected against SQL injection")
    else:
        print_failure("Potential SQL injection vulnerability")

def test_admin_modify_availability():
    print_header("Testing /admin/modifyAvailability endpoint")
    
    # First, create a test product
    print_subheader("Creating a test product for inventory")
    product_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": f"Inventory Test Product {random.randint(1000, 9999)}",
        "description": "A test product for inventory management",
        "price": round(random.uniform(10.0, 50.0), 2),
        "category": "beer",
        "alcohol": round(random.uniform(4.0, 12.0), 1)
    }
    
    product_response = make_request("post", "admin/createProduct", json=product_data)
    if not product_response or product_response.json().get("status") != "success":
        print_failure("Could not create test product for inventory tests")
        return
    
    inventory_product_id = product_response.json().get("productId")
    print_info(f"Created test product with ID: {inventory_product_id}")
    
    # Get a shop to use for inventory tests
    print_subheader("Getting a shop for inventory tests")
    shop_response = make_request("get", "getShops")
    if not shop_response:
        print_failure("Could not get shops for inventory tests")
        return
    
    shops = shop_response.json()
    if not shops:
        print_failure("No shops available for inventory tests")
        return
    
    test_shop_id = shops[0]["shop_id"]
    print_info(f"Using shop with ID: {test_shop_id}")
    
    # Test case 1: Add inventory to shop with valid data
    print_subheader("Test case 1: Adding inventory with valid data")
    inventory_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "shopId": test_shop_id,
        "products": [
            {
                "productId": inventory_product_id,
                "quantity": 50
            }
        ]
    }
    
    response = make_request("post", "admin/modifyAvailability", json=inventory_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully added inventory")
    else:
        print_failure("Failed to add inventory")
    
    # Test case 2: Update existing inventory (change quantity)
    print_subheader("Test case 2: Updating existing inventory")
    update_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "shopId": test_shop_id,
        "products": [
            {
                "productId": inventory_product_id,
                "quantity": 75
            }
        ]
    }
    
    response = make_request("post", "admin/modifyAvailability", json=update_data)
    if response and response.json().get("status") == "success":
        print_success("Successfully updated inventory")
    else:
        print_failure("Failed to update inventory")
    
    # Test case 3: Add multiple products at once
    print_subheader("Test case 3: Adding multiple products at once")
    # First, create another test product
    another_product_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "name": f"Second Inventory Test {random.randint(1000, 9999)}",
        "price": round(random.uniform(10.0, 50.0), 2),
        "category": "wine",
        "alcohol": round(random.uniform(4.0, 12.0), 1)
    }
    
    another_product_response = make_request("post", "admin/createProduct", json=another_product_data)
    if not another_product_response:
        print_failure("Could not create second test product")
    else:
        second_product_id = another_product_response.json().get("productId")
        print_info(f"Created second test product with ID: {second_product_id}")
        
        # Add both products to inventory
        multi_product_data = {
            "email": ADMIN_EMAIL,
            "password": ADMIN_PASSWORD,
            "shopId": test_shop_id,
            "products": [
                {
                    "productId": inventory_product_id,
                    "quantity": 100
                },
                {
                    "productId": second_product_id,
                    "quantity": 25
                }
            ]
        }
        
        response = make_request("post", "admin/modifyAvailability", json=multi_product_data)
        if response and response.json().get("status") == "success":
            print_success("Successfully added multiple products to inventory")
        else:
            print_failure("Failed to add multiple products to inventory")
    
    # Test case 4: Invalid shop ID
    print_subheader("Test case 4: Invalid shop ID")
    invalid_shop_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "shopId": 999999,  # Assuming this shop ID doesn't exist
        "products": [
            {
                "productId": inventory_product_id,
                "quantity": 50
            }
        ]
    }
    
    response = make_request("post", "admin/modifyAvailability", json=invalid_shop_data, expected_status=404, error_ok=True)
    if response and response.status_code == 404:
        print_success("Correctly returned error for non-existent shop")
    else:
        print_failure("Should have returned error for non-existent shop")
    
    # Test case 5: Invalid product ID
    print_subheader("Test case 5: Invalid product ID")
    invalid_product_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "shopId": test_shop_id,
        "products": [
            {
                "productId": 999999,  # Assuming this product ID doesn't exist
                "quantity": 50
            }
        ]
    }
    
    response = make_request("post", "admin/modifyAvailability", json=invalid_product_data, expected_status=404, error_ok=True)
    if response and response.status_code == 404:
        print_success("Correctly returned error for non-existent product")
    else:
        print_failure("Should have returned error for non-existent product")
    
    # Test case 6: Invalid credentials
    print_subheader("Test case 6: Invalid credentials")
    invalid_creds_data = {
        "email": "fake@example.com",
        "password": "wrongpassword",
        "shopId": test_shop_id,
        "products": [
            {
                "productId": inventory_product_id,
                "quantity": 50
            }
        ]
    }
    
    response = make_request("post", "admin/modifyAvailability", json=invalid_creds_data, expected_status=401, error_ok=True)
    if response and response.status_code == 401:
        print_success("Correctly denied access with invalid credentials")
    else:
        print_failure("Should have denied access with invalid credentials")
    
    # Clean up test products
    print_subheader("Cleaning up test products")
    delete_data = {
        "email": ADMIN_EMAIL,
        "password": ADMIN_PASSWORD,
        "productId": inventory_product_id
    }
    
    make_request("post", "admin/deleteProduct", json=delete_data)
    print_info(f"Deleted product with ID: {inventory_product_id}")
    
    if 'second_product_id' in locals():
        delete_data["productId"] = second_product_id
        make_request("post", "admin/deleteProduct", json=delete_data)
        print_info(f"Deleted product with ID: {second_product_id}")

def print_summary():
    """Print test results summary."""
    success_rate = (results["passed"] / results["total"]) * 100 if results["total"] > 0 else 0
    
    print(f"\n{Colors.BLUE}{'=' * 80}{Colors.ENDC}")
    print(f"{Colors.BLUE}== TEST SUMMARY{Colors.ENDC}")
    print(f"{Colors.BLUE}{'=' * 80}{Colors.ENDC}")
    print(f"Total tests: {results['total']}")
    print(f"{Colors.GREEN}Passed: {results['passed']}{Colors.ENDC}")
    print(f"{Colors.RED}Failed: {results['failed']}{Colors.ENDC}")
    print(f"Success rate: {success_rate:.2f}%")
    print(f"{Colors.BLUE}{'=' * 80}{Colors.ENDC}")

if __name__ == "__main__":
    global test_product_id
    global extreme_product_id
    print_header("Starting Boozy API Admin Endpoints Tests")
    print_info(f"Base URL: {BASE_URL}")
    print_info(f"Admin Email: {ADMIN_EMAIL}")
    print_info(f"Test started at: {datetime.now().strftime('%Y-%m-%d %H:%M:%S')}")
    
    # Initialize global variables for test products
    test_product_id = None
    extreme_product_id = None
    
    try:
        # Public endpoint tests
        test_get_product_image()
        test_get_shops()
        test_get_products()
        test_get_availability()
        test_create_user()
        test_connect_user()
        test_create_order()
        test_cancel_order()

        # Admin endpoint tests
        test_admin_create_user()
        test_admin_modify_user()
        test_admin_delete_user()
        test_admin_create_shop()
        test_admin_modify_shop()
        test_admin_delete_shop()
        test_admin_create_product()
        test_admin_modify_product()
        test_admin_delete_product()
        test_admin_get_users()
        test_admin_modify_availability()
        
        # Print summary
        print_summary()
        
    except KeyboardInterrupt:
        print_info("\nTest interrupted by user")
        print_summary()
        sys.exit(1)
    except Exception as e:
        print_failure(f"Unexpected error during testing", str(e))
        print_summary()
        sys.exit(1)