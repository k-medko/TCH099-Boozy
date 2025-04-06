import requests
import time

def test_all_endpoints():
    base_url = "http://4.172.255.120:5000"

    def print_resp(name, resp):
        print(f"=== {name} ===")
        print("Status:", resp.status_code)
        try:
            print("Response:", resp.json())
        except Exception:
            print("Response:", resp.text)
        print("")

    # GET endpoints
    get_endpoints = [
        ("/", "Home"),
        ("/test", "Test"),
        ("/favicon.ico", "Favicon"),
        ("/getStores", "Get Stores"),
        ("/getProducts", "Get Products"),
        ("/getAvailability", "Get Availability"),
        ("/getImages/product/1", "Get Product Image"),
        ("/getImages/store/1", "Get Store Image")
    ]
    for endpoint, name in get_endpoints:
        resp = requests.get(base_url + endpoint)
        print_resp(name, resp)

    # POST endpoints

    # 1. Create a new customer (unique email)
    unique_email = f"testuser_{int(time.time())}@example.com"
    create_user_data = {
        "email": unique_email,
        "password": "testpass",
        "lastName": "Tester",
        "firstName": "Test",
        "phoneNumber": "0000000000",
        "userType": "customer"
    }
    resp = requests.post(base_url + "/createUser", json=create_user_data)
    print_resp("Create User", resp)

    # 2. Connect the new user
    connect_data = {
        "email": unique_email,
        "password": "testpass"
    }
    resp = requests.post(base_url + "/connectUser", json=connect_data)
    print_resp("Connect User", resp)

    # 3. Create an order (using assumed valid IDs)
    order_data = {
        "userId": 9,         # Existing customer (sam.green@example.com)
        "storeId": 1,        # Assumed store
        "addressId": 1,      # Assumed address
        "items": [{"productId": 1, "quantity": 1}],
        "paymentMethod": "card",
        "cardNumber": "4111111111111111",
        "cvcCard": "123",
        "expiryDate": "12/25"
    }
    resp = requests.post(base_url + "/createOrder", json=order_data)
    print_resp("Create Order", resp)

    # 4. Cancel order (if orderId returned)
    try:
        order_id = resp.json().get("orderId")
        if order_id:
            cancel_data = {"orderId": order_id}
            resp_cancel = requests.post(base_url + "/cancelOrder", json=cancel_data)
            print_resp("Cancel Order", resp_cancel)
        else:
            print("Create Order did not return orderId; skipping Cancel Order test.\n")
    except Exception as e:
        print("Error processing createOrder response:", e)

    # Admin credentials (from provided accounts)
    admin_creds = {"adminEmail": "admin.two@example.com", "adminPassword": "password"}

    # 5. Admin: Create User
    admin_create_user_data = {
        **admin_creds,
        "email": f"admincreated_{int(time.time())}@example.com",
        "password": "testpass",
        "lastName": "Admin",
        "firstName": "Created",
        "phoneNumber": "1111111111",
        "userType": "customer"
    }
    resp = requests.post(base_url + "/admin/createUser", json=admin_create_user_data)
    print_resp("Admin Create User", resp)

    # 6. Admin: Modify User (example: modify user with id 9)
    admin_modify_user_data = {
        **admin_creds,
        "userId": 9,
        "email": "modified.sam.green@example.com"
    }
    resp = requests.post(base_url + "/admin/modifyUser", json=admin_modify_user_data)
    print_resp("Admin Modify User", resp)

    # 7. Admin: Delete User (example: delete user with id 9)
    admin_delete_user_data = {**admin_creds, "userId": 9}
    resp = requests.post(base_url + "/admin/deleteUser", json=admin_delete_user_data)
    print_resp("Admin Delete User", resp)

    # 8. Admin: Create Store
    admin_create_store_data = {
        **admin_creds,
        "name": "Test Store",
        "houseNumber": "100",
        "street": "Main St",
        "postalCode": "12345",
        "city": "TestCity",
        "civicNumber": "001"
    }
    resp = requests.post(base_url + "/admin/createStore", json=admin_create_store_data)
    print_resp("Admin Create Store", resp)
    store_id = None
    try:
        store_id = resp.json().get("storeId")
    except Exception:
        pass

    if store_id:
        # 9. Admin: Modify Store
        admin_modify_store_data = {**admin_creds, "storeId": store_id, "name": "Modified Test Store"}
        resp = requests.post(base_url + "/admin/modifyStore", json=admin_modify_store_data)
        print_resp("Admin Modify Store", resp)
        # 10. Admin: Delete Store
        admin_delete_store_data = {**admin_creds, "storeId": store_id}
        resp = requests.post(base_url + "/admin/deleteStore", json=admin_delete_store_data)
        print_resp("Admin Delete Store", resp)
    else:
        print("Admin Create Store did not return storeId; skipping store modify/delete tests.\n")

    # 11. Admin: Create Product
    admin_create_product_data = {
        **admin_creds,
        "name": "Test Product",
        "price": 10.0,
        "category": "TestCat",
        "isAvailable": True
    }
    resp = requests.post(base_url + "/admin/createProduct", json=admin_create_product_data)
    print_resp("Admin Create Product", resp)
    product_id = None
    try:
        product_id = resp.json().get("productId")
    except Exception:
        pass

    if product_id:
        # 12. Admin: Modify Product
        admin_modify_product_data = {**admin_creds, "productId": product_id, "name": "Modified Test Product"}
        resp = requests.post(base_url + "/admin/modifyProduct", json=admin_modify_product_data)
        print_resp("Admin Modify Product", resp)
        # 13. Admin: Delete Product
        admin_delete_product_data = {**admin_creds, "productId": product_id}
        resp = requests.post(base_url + "/admin/deleteProduct", json=admin_delete_product_data)
        print_resp("Admin Delete Product", resp)
    else:
        print("Admin Create Product did not return productId; skipping product modify/delete tests.\n")

if __name__ == '__main__':
    test_all_endpoints()
#TODO:
#Fix Create script to allow cascade delete and remove imagePath
#Add images for each products


