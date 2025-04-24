import requests
import json
from datetime import datetime

# API configuration
API_URL = "http://4.172.252.189:5000"
ADMIN_EMAIL = "tristan@boozy.com"
ADMIN_PASSWORD = "adminpass"

def test_get_all_orders():
    """
    Test the admin getAllOrders endpoint
    """
    # Create the request payload with admin credentials
    payload = {
        "admin_email": ADMIN_EMAIL,
        "admin_password": ADMIN_PASSWORD
    }
    
    # Set headers for the request
    headers = {
        "Content-Type": "application/json"
    }
    
    # Print test information
    print(f"Testing Admin API: {API_URL}/admin/getAllOrders")
    print(f"Using admin email: {ADMIN_EMAIL}")
    print("Sending request...\n")
    
    try:
        # Send the POST request to the API
        response = requests.post(
            f"{API_URL}/admin/getAllOrders",
            headers=headers,
            data=json.dumps(payload)
        )
        
        # Check the response status code
        if response.status_code == 200:
            # Success - parse the response data
            data = response.json()
            
            # Display summary info
            print(f"✅ SUCCESS: Got {data['total_orders']} orders")
            print("-" * 50)
            
            # Display some details about each order
            for i, order in enumerate(data['orders'], 1):
                print(f"Order #{i}: ID {order['order_id']}")
                print(f"  Date: {order['creation_date']}")
                print(f"  Status: {order['status']}")
                print(f"  Client: {order['client']['first_name']} {order['client']['last_name']} ({order['client']['email']})")
                print(f"  Carrier: {order['carrier']['first_name']} {order['carrier']['last_name']}")
                print(f"  Shop: {order['shop']['name']}")
                print(f"  Total Amount: ${order['total_amount']:.2f}")
                print(f"  Items: {order['total_items']}")
                
                # Print first 3 items in the order
                print("  Products:")
                for j, item in enumerate(order['items'][:3], 1):
                    print(f"    - {item['quantity']}x {item['name']} (${item['price']:.2f} each)")
                
                if len(order['items']) > 3:
                    print(f"    - ... and {len(order['items']) - 3} more items")
                
                print("-" * 50)
            
            # Save the complete data to a file for further inspection
            timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
            filename = f"orders_data_{timestamp}.json"
            with open(filename, 'w') as f:
                json.dump(data, f, indent=2)
            print(f"Complete order data saved to '{filename}'")
            
        elif response.status_code == 401:
            # Authentication error
            data = response.json()
            print(f"❌ AUTHENTICATION ERROR: {data.get('message', 'Unauthorized access')}")
        else:
            # Other error
            print(f"❌ ERROR: Received status code {response.status_code}")
            print(response.text)
            
    except requests.exceptions.RequestException as e:
        print(f"❌ CONNECTION ERROR: Could not connect to the API")
        print(f"Details: {str(e)}")
    except json.JSONDecodeError:
        print(f"❌ RESPONSE ERROR: Could not parse response as JSON")
        print(f"Response text: {response.text}")
    except Exception as e:
        print(f"❌ UNEXPECTED ERROR: {str(e)}")

if __name__ == "__main__":
    test_get_all_orders()