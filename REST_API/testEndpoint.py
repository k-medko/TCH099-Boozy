import requests
import json
from pprint import pprint
import sys

def test_client_orders(api_url):
    """
    Test the getUserOrders endpoint for both client and carrier views
    """
    # Set headers
    headers = {
        "Content-Type": "application/json"
    }
    
    # Test 1: Client View
    print("\n" + "="*80)
    print("TEST 1: CLIENT VIEW")
    print("="*80)
    
    client_payload = {
        "email": "emma.lavoie123@example.com",
        "password": "password"
    }
    
    print(f"Sending request as client (emma.lavoie123@example.com)...")
    try:
        client_response = requests.post(
            f"{api_url}/getUserOrders",
            data=json.dumps(client_payload),
            headers=headers
        )
        
        print(f"Status Code: {client_response.status_code}")
        
        if client_response.status_code == 200:
            client_data = client_response.json()
            print(f"Response Status: {client_data.get('status')}")
            print(f"User Role: {client_data.get('user_role')}")
            
            if 'orders' in client_data and client_data['orders']:
                print(f"\nFound {len(client_data['orders'])} orders:")
                print("-" * 80)
                for order in client_data['orders']:
                    print(f"Order ID: {order.get('order_id')}")
                    print(f"Total Amount: ${order.get('total_amount'):.2f}")
                    print(f"Status: {order.get('status')}")
                    print(f"Shop ID: {order.get('shop_id')}")
                    print(f"Carrier Name: {order.get('carrier_name')}")
                    print(f"Creation Date: {order.get('creation_date')}")
                    print("-" * 80)
            else:
                print("No orders found for this client.")
        else:
            print(f"Error: {client_response.text}")
    
    except Exception as e:
        print(f"Exception occurred during client test: {str(e)}")
    
    # Test 2: Carrier View
    print("\n" + "="*80)
    print("TEST 2: CARRIER VIEW")
    print("="*80)
    
    carrier_payload = {
        "email": "livreur1@example.com",
        "password": "mdp123"
    }
    
    print(f"Sending request as carrier (livreur1@example.com)...")
    try:
        carrier_response = requests.post(
            f"{api_url}/getUserOrders",
            data=json.dumps(carrier_payload),
            headers=headers
        )
        
        print(f"Status Code: {carrier_response.status_code}")
        
        if carrier_response.status_code == 200:
            carrier_data = carrier_response.json()
            print(f"Response Status: {carrier_data.get('status')}")
            print(f"User Role: {carrier_data.get('user_role')}")
            
            if 'orders' in carrier_data and carrier_data['orders']:
                print(f"\nFound {len(carrier_data['orders'])} deliveries:")
                print("-" * 80)
                for order in carrier_data['orders']:
                    print(f"Order ID: {order.get('order_id')}")
                    print(f"Total Amount: ${order.get('total_amount'):.2f}")
                    print(f"Status: {order.get('status')}")
                    print(f"Shop ID: {order.get('shop_id')}")
                    print("-" * 80)
            else:
                print("No deliveries found for this carrier.")
        else:
            print(f"Error: {carrier_response.text}")
    
    except Exception as e:
        print(f"Exception occurred during carrier test: {str(e)}")

if __name__ == "__main__":
    # Use the provided API URL
    api_url = "http://4.172.252.189:5000"
    test_client_orders(api_url)