import requests
import time
import random
import string

# Base URL (adjust if necessary)
BASE_URL = "http://4.172.252.189:5000"

# ANSI colors for output
CYAN     = "\033[96m"
BLUE     = "\033[94m"
GREEN    = "\033[92m"
YELLOW   = "\033[93m"
MAGENTA  = "\033[95m"
RED      = "\033[91m"
RESET    = "\033[0m"

def random_string(n=8):
    return ''.join(random.choice(string.ascii_lowercase) for _ in range(n))

def random_email():
    return f"{random_string()}@example.com"

def random_phone_number():
    # Generate a random 10-digit phone number
    return ''.join(random.choice(string.digits) for _ in range(10))

def random_license_plate(n=7):
    # Generate a random license plate consisting of uppercase letters and digits.
    return ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(n))

def safe_json(response):
    try:
        return response.json()
    except Exception as e:
        print(f"{RED}Error decoding JSON: {response.text}{RESET}")
        return None

# Create a user (client or carrier). All not-null fields are provided and unique parameters are randomized.
def create_user(user_type):
    payload = {
        "email": random_email(),
        "password": random_string(10),
        "last_name": "SimLast",
        "first_name": "SimFirst",
        "phone_number": random_phone_number(),   # Random phone number
        "user_type": user_type,
        "civic": 123,
        "apartment": "",         # explicitly include apartment as empty string
        "street": "SimStreet",
        "city": "Montreal",
        "postal_code": "H1H1H1"
    }
    if user_type == "carrier":
        # Use a random license plate to avoid unique constraint issues
        payload["license_plate"] = random_license_plate()
        payload["car_brand"] = "Toyota"
    resp = requests.post(f"{BASE_URL}/createUser", json=payload)
    data = safe_json(resp)
    print(f"{CYAN}Created {user_type}:{RESET} {data if data else 'No JSON returned'}")
    return payload["email"], payload["password"]

# Get the first available shop id from /getShops.
def get_shop_id():
    resp = requests.get(f"{BASE_URL}/getShops")
    data = safe_json(resp)
    if not data:
        print(f"{RED}No shop available.{RESET}")
        return None
    print(f"{CYAN}Using shop:{RESET} {data[0]}")
    return data[0]["shop_id"]

# Client creates an order. We assume a product with product_id = 1 is available.
def create_order(client_email, client_password, shop_id):
    payload = {
        "email": client_email,
        "password": client_password,
        "shop_id": shop_id,
        "items": [{"product_id": 1, "quantity": 1}],
        "payment_method": "credit_card",
        "card_name": "SimClient",
        "card_number": "1111222233334444",
        "CVC_card": 123,
        "expiry_date_month": 12,
        "expiry_date_year": 30
    }
    resp = requests.post(f"{BASE_URL}/createOrder", json=payload)
    data = safe_json(resp)
    print(f"{GREEN}Order creation response:{RESET} {data if data else 'No JSON returned'}")

# Client retrieves order status via /getOrderStatus.
def get_order_status(user_email, user_password, order_id):
    payload = {
        "email": user_email,
        "password": user_password,
        "order_id": order_id
    }
    resp = requests.post(f"{BASE_URL}/getUserOrders", json=payload)
    data = safe_json(resp)
    print(f"{MAGENTA}Order status for {order_id}:{RESET} {data if data else 'No JSON returned'}")
    return data

# Carrier fetches available orders (only those with status 'Searching').
def get_orders():
    resp = requests.get(f"{BASE_URL}/getOrders")
    data = safe_json(resp)
    print(f"{YELLOW}Available orders:{RESET} {data if data else 'No JSON returned'}")
    return data

# Carrier claims an order via /takeOrder (which sets status to InRoute automatically).
def take_order(carrier_email, carrier_password, order_id):
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "order_id": order_id
    }
    resp = requests.post(f"{BASE_URL}/takeOrder", json=payload)
    data = safe_json(resp)
    print(f"{BLUE}Carrier takeOrder response:{RESET} {data if data else 'No JSON returned'}")
    return data

# Carrier updates order status via /updateOrder.
def update_order(carrier_email, carrier_password, new_status):
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "status": new_status
    }
    resp = requests.post(f"{BASE_URL}/updateOrder", json=payload)
    data = safe_json(resp)
    print(f"{GREEN}Carrier updateOrder to {new_status}:{RESET} {data if data else 'No JSON returned'}")
    return data

def main():
    # Create a client and a carrier.
    print(f"{CYAN}=== Creating Client ==={RESET}")
    client_email, client_password = create_user("client")
    time.sleep(5)
    print(f"{CYAN}=== Creating Carrier ==={RESET}")
    carrier_email, carrier_password = create_user("carrier")
    time.sleep(5)

    # Get shop id.
    shop_id = get_shop_id()
    if shop_id is None:
        return

    # Client creates an order.
    print(f"{GREEN}=== Client creating Order (status 'Searching') ==={RESET}")
    create_order(client_email, client_password, shop_id)
    time.sleep(5)

    # Retrieve available orders.
    orders = get_orders()
    if not orders:
        print(f"{RED}No available orders. Exiting simulation.{RESET}")
        return
    order_id = orders[-1]["order_id"]

    # Client checks order status before pickup.
    print(f"{MAGENTA}=== Client checking order status BEFORE pickup ==={RESET}")
    get_order_status(client_email, client_password, order_id)
    time.sleep(5)

    # Carrier claims the order.
    print(f"{BLUE}=== Carrier claiming the Order ==={RESET}")
    take_order(carrier_email, carrier_password, order_id)
    time.sleep(5)

    # Client checks order status after claim.
    print(f"{MAGENTA}=== Client checking order status AFTER claim ==={RESET}")
    get_order_status(client_email, client_password, order_id)
    time.sleep(5)

    # Carrier updates order to "Shipping" (picked up at store) and receives client info.
    print(f"{GREEN}=== Carrier updating order to 'Shipping' ==={RESET}")
    update_order(carrier_email, carrier_password, "Shipping")
    time.sleep(5)

    # Client checks order status after Shipping update.
    print(f"{MAGENTA}=== Client checking order status AFTER 'Shipping' update ==={RESET}")
    get_order_status(client_email, client_password, order_id)
    time.sleep(5)

    # Carrier updates order to "Completed" (delivered).
    print(f"{GREEN}=== Carrier updating order to 'Completed' ==={RESET}")
    update_order(carrier_email, carrier_password, "Completed")
    time.sleep(5)

    # Final check by client.
    print(f"{MAGENTA}=== Client checking final order status ==={RESET}")
    get_order_status(client_email, client_password, order_id)

if __name__ == "__main__":
    main()
