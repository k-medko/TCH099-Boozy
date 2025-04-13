import requests
import time
import random
import string

# Base URL for the API
BASE_URL = "http://4.172.252.189:5000"

# ANSI Color Codes
CYAN     = "\033[96m"
BLUE     = "\033[94m"
GREEN    = "\033[92m"
YELLOW   = "\033[93m"
MAGENTA  = "\033[95m"
RED      = "\033[91m"
RESET    = "\033[0m"

# Helper functions for random data
def random_string(n=8):
    return ''.join(random.choice(string.ascii_lowercase) for _ in range(n))

def random_email():
    return f"{random_string()}@example.com"

# Create a user. For the client, also attach an address.
def create_user(user_type):
    payload = {
        "email": random_email(),
        "password": random_string(10),
        "last_name": "TestLast",
        "first_name": "TestFirst",
        "phone_number": "5141234567",
        "user_type": user_type,
        "civic": 123,
        "street": "TestStreet",
        "city": "Montreal",
        "postal_code": "H1H1H1"
    }
    if user_type == "carrier":
        payload["license_plate"] = "XYZ987"  # fixed for simplicity
        payload["car_brand"] = "Honda"
    response = requests.post(f"{BASE_URL}/createUser", json=payload)
    return payload["email"], payload["password"]

# Get an existing shop id (without printing its details)
def get_shop_id():
    response = requests.get(f"{BASE_URL}/getShops")
    shops = response.json()
    if not shops:
        raise Exception("No shop available!")
    return shops[0]["shop_id"]

# Client creates an order; for simplicity, assume product_id=1 exists.
def create_order(client_email, client_password, shop_id):
    order_payload = {
        "email": client_email,
        "password": client_password,
        "shop_id": shop_id,
        "items": [{"product_id": 1, "quantity": 1}],
        "payment_method": "credit_card",
        "card_name": "Test Client",
        "card_number": "1111222233334444",
        "CVC_card": 123,
        "expiry_date_month": 12,
        "expiry_date_year": 30
    }
    response = requests.post(f"{BASE_URL}/createOrder", json=order_payload)
    return response.json()

# Carrier fetches orders (returns minimal info, per /getOrders)
def get_orders():
    response = requests.get(f"{BASE_URL}/getOrders")
    return response.json()

# Carrier claims an order (automatically sets status to "InRoute")
def take_order(carrier_email, carrier_password, order_id):
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "order_id": order_id
    }
    response = requests.post(f"{BASE_URL}/takeOrder", json=payload)
    return response.json()

# Carrier updates the order; for "Shipping", the response returns the client’s info.
def update_order(carrier_email, carrier_password, new_status):
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "status": new_status
    }
    response = requests.post(f"{BASE_URL}/updateOrder", json=payload)
    return response.json()

def main():
    # Create client and carrier
    print(f"{CYAN}*** Creating Client...{RESET}")
    client_email, client_password = create_user("client")
    print(f"{CYAN}Client created with email: {client_email}{RESET}")
    
    print(f"{CYAN}*** Creating Carrier...{RESET}")
    carrier_email, carrier_password = create_user("carrier")
    print(f"{CYAN}Carrier created with email: {carrier_email}{RESET}")
    
    # Retrieve a shop id (do not output store details)
    shop_id = get_shop_id()
    
    # Client creates an order (order status "Searching")
    print(f"{GREEN}*** Client creating order...{RESET}")
    order_resp = create_order(client_email, client_password, shop_id)
    print(f"{GREEN}Order created (status: Searching).{RESET}")
    
    # Wait 5 seconds between requests
    time.sleep(5)
    
    # Carrier fetches available orders
    print(f"{YELLOW}*** Carrier fetching orders...{RESET}")
    orders = get_orders()
    if not orders:
        print(f"{RED}No orders available. Exiting.{RESET}")
        return
    order_id = orders[0]["order_id"]
    print(f"{YELLOW}Order {order_id} fetched.{RESET}")
    
    time.sleep(5)
    
    # Carrier claims the order
    print(f"{BLUE}*** Carrier claiming order {order_id}...{RESET}")
    take_resp = take_order(carrier_email, carrier_password, order_id)
    print(f"{BLUE}Order claimed; updated order info: {take_resp}{RESET}")
    
    time.sleep(5)
    
    # Carrier updates order to "Shipping" (picked up at store)
    print(f"{MAGENTA}*** Carrier updating order to 'Shipping'...{RESET}")
    shipping_resp = update_order(carrier_email, carrier_password, "Shipping")
    print(f"{MAGENTA}Order updated to 'Shipping'. Client info received: {shipping_resp}{RESET}")
    
    time.sleep(5)
    
    # Carrier updates order to "Completed" (delivered to client)
    print(f"{GREEN}*** Carrier updating order to 'Completed'...{RESET}")
    completed_resp = update_order(carrier_email, carrier_password, "Completed")
    print(f"{GREEN}Order updated to 'Completed'.{RESET}")

if __name__ == "__main__":
    main()
