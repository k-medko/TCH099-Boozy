import requests
import time
import random
import string

BASE_URL = "http://4.172.252.189:5000"

# -------------------------------
# Helper functions for random data
# -------------------------------
def random_string(n=8):
    return ''.join(random.choice(string.ascii_lowercase) for _ in range(n))

def random_email():
    return f"{random_string()}@example.com"

# -------------------------------
# Simple API wrapper functions
# -------------------------------
def create_client():
    client_email = random_email()
    client_password = random_string(10)
    payload = {
        "email": client_email,
        "password": client_password,
        "last_name": "ClientLast",
        "first_name": "ClientFirst",
        "phone_number": "5141234567",
        # Providing minimal address fields to attach an address
        "civic": 123,
        "street": "Client St",
        "city": "Montreal",
        "postal_code": "H1H 1H1",
        "user_type": "client"
    }
    r = requests.post(f"{BASE_URL}/createUser", json=payload)
    print("Client created:", r.json())
    return client_email, client_password

def create_carrier():
    carrier_email = random_email()
    carrier_password = random_string(10)
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "last_name": "CarrierLast",
        "first_name": "CarrierFirst",
        "phone_number": "5147654321",
        "user_type": "carrier",
        "license_plate": "ABC123",  # For simplicity, using a fixed plate here
        "car_brand": "Toyota"
    }
    r = requests.post(f"{BASE_URL}/createUser", json=payload)
    print("Carrier created:", r.json())
    return carrier_email, carrier_password

def get_shop():
    r = requests.get(f"{BASE_URL}/getShops")
    shops = r.json()
    print("Shops available:", shops)
    if not shops:
        raise Exception("No shops available!")
    # Pick the first shop
    return shops[0]["shop_id"]

def get_available_product(shop_id):
    r = requests.get(f"{BASE_URL}/getAvailability", params={"shop_id": shop_id})
    avail = r.json()
    print("Shop availability:", avail)
    if not avail:
        raise Exception("No products available in shop!")
    # Pick first available product
    return avail[0]["product_id"]

def create_order(client_email, client_password, shop_id, product_id):
    order_payload = {
        "email": client_email,
        "password": client_password,
        "shop_id": shop_id,
        "items": [{"product_id": product_id, "quantity": 1}],
        "payment_method": "credit_card",
        "card_name": "Test Client",
        "card_number": "1234123412341234",
        "CVC_card": 123,
        "expiry_date_month": 12,
        "expiry_date_year": 30
    }
    r = requests.post(f"{BASE_URL}/createOrder", json=order_payload)
    print("Order created:", r.json())
    return r.json()

def get_orders():
    r = requests.get(f"{BASE_URL}/getOrders")
    orders = r.json()
    print("Orders fetched:", orders)
    return orders

def take_order(carrier_email, carrier_password, order_id):
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "order_id": order_id
    }
    r = requests.post(f"{BASE_URL}/takeOrder", json=payload)
    print("Order claimed by carrier:", r.json())
    return r.json()

def update_order(carrier_email, carrier_password, new_status):
    payload = {
        "email": carrier_email,
        "password": carrier_password,
        "status": new_status
    }
    r = requests.post(f"{BASE_URL}/updateOrder", json=payload)
    print(f"Order updated to {new_status}:", r.json())
    return r.json()

# -------------------------------
# Main flow: step-by-step with 3-second pauses
# -------------------------------
def main():
    print("Creating client...")
    client_email, client_password = create_client()
    
    print("Creating carrier...")
    carrier_email, carrier_password = create_carrier()
    
    print("Retrieving a shop...")
    shop_id = get_shop()
    
    print("Retrieving an available product from the shop...")
    product_id = get_available_product(shop_id)
    
    print("Client creating an order (status 'Searching')...")
    order_resp = create_order(client_email, client_password, shop_id, product_id)
    
    # Pause 3 seconds before carrier actions
    time.sleep(3)
    
    print("Carrier fetching available orders...")
    orders = get_orders()
    time.sleep(3)
    
    if not orders:
        print("No orders found. Exiting.")
        return
    first_order = orders[0]
    order_id = first_order["order_id"]
    
    print("Carrier claiming the first order...")
    take_order(carrier_email, carrier_password, order_id)
    time.sleep(3)
    
    print("Carrier updating order status to 'Shipped' (reached shop)...")
    update_order(carrier_email, carrier_password, "Shipped")
    time.sleep(3)
    
    print("Carrier updating order status to 'Completed' (delivered to client)...")
    update_order(carrier_email, carrier_password, "Completed")
    
if __name__ == "__main__":
    main()
