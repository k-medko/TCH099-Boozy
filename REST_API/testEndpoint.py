import requests
import base64
import json
import os

# API endpoint
api_url = "http://4.172.252.189:5000/admin/modifyShop"

# Admin credentials
admin_email = "tristan@boozy.com"
admin_password = "adminpass"

# Shop ID to modify (replace with a valid shop_id from your database)
shop_id = 1  # Change this to a valid shop ID

# Image path
image_path = "REST_API/testImage.png"


# Verify image exists
if not os.path.exists(image_path):
    print(f"Error: {image_path} not found. Please create or provide the correct path to the image.")
    exit(1)

# Read and encode the image
try:
    with open(image_path, "rb") as image_file:
        encoded_image = base64.b64encode(image_file.read()).decode('utf-8')
except Exception as e:
    print(f"Error reading or encoding the image: {str(e)}")
    exit(1)

# Prepare payload
payload = {
    "admin_email": admin_email,
    "admin_password": admin_password,
    "shop_id": shop_id,
    "image": encoded_image
}

# Make the request
try:
    response = requests.post(api_url, json=payload)
    
    # Check response
    if response.status_code == 200:
        result = response.json()
        print("Response:", result)
        if result.get("status") == "success":
            print("Shop image modified successfully!")
        else:
            print(f"Error: {result.get('message', 'Unknown error')}")
    else:
        print(f"HTTP Error: {response.status_code}")
        print(response.text)
except Exception as e:
    print(f"Request failed: {str(e)}")

print("\nNote: If you're seeing an error about the image function, check if the 'changeImage' function is properly implemented in the API.")