from flask import Flask, request, jsonify, send_from_directory
from flask_mysqldb import MySQL
from flask_cors import CORS
import os
from datetime import datetime

app = Flask(__name__)
CORS(app)

# Configuration
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'BOOZY_SERVER'
app.config['MYSQL_PASSWORD'] = 'Boozyadmin1234'
app.config['MYSQL_DB'] = 'boozy_database'
mysql = MySQL(app)

# Database Helper Functions
def execute_query(query, params=None, fetch=True):
    """Execute SQL query and return results if applicable"""
    cursor = mysql.connection.cursor()
    cursor.execute(query, params)
    
    if fetch:
        results = cursor.fetchall()
    else:
        mysql.connection.commit()
        results = cursor.lastrowid if cursor.lastrowid else True
    
    cursor.close()
    return results

def get_next_id(table_name, id_column):
    """Get the next available ID for a table"""
    max_id = execute_query(f"SELECT MAX({id_column}) FROM {table_name}")
    return (max_id[0][0] + 1) if max_id and max_id[0][0] else 1

def get_address_string(address_id):
    """Convert address_id to a full address string"""
    address = execute_query(
        "SELECT house_number, street, postal_code, city FROM CustomerAddress WHERE address_id = %s", 
        (address_id,)
    )
    if address:
        return f"{address[0][0]} {address[0][1]}, {address[0][3]}, {address[0][2]}"
    return None

# Basic Routes
@app.route('/')
def home():
    return jsonify({"message": "Welcome to the Boozy API!!!"})

@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'favicon.ico', mimetype='image/vnd.microsoft.icon')

# IMAGE ENDPOINTS
@app.route('/getImages/product/<product_id>', methods=['GET'])
def get_product_image(product_id):
    # Get the image name from the product table
    query = "SELECT image_nom FROM Product WHERE product_id = %s"
    result = execute_query(query, (product_id,))
    
    if result and result[0][0]:
        # Try to serve the specific product image
        image_path = os.path.join(app.root_path, 'static', 'productImages', result[0][0])
        if os.path.exists(image_path):
            return send_from_directory(os.path.join(app.root_path, 'static', 'productImages'),
                result[0][0])
    
    # Return placeholder if no image found or product doesn't exist
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'placeholder.jpg')

@app.route('/getImages/store/<store_id>', methods=['GET'])
def get_store_image(store_id):
    # Get the image name from the store table
    query = "SELECT image_nom FROM StoreLocation WHERE store_id = %s"
    result = execute_query(query, (store_id,))
    
    if result and result[0][0]:
        # Try to serve the specific store image
        image_path = os.path.join(app.root_path, 'static', 'storeImages', result[0][0])
        if os.path.exists(image_path):
            return send_from_directory(os.path.join(app.root_path, 'static', 'storeImages'),
                result[0][0])
    
    # Return placeholder if no image found or store doesn't exist
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'placeholder.jpg')
# STORE ENDPOINTS
@app.route('/getStores', methods=['GET'])
def get_stores():
    store_id = request.args.get('storeId')
    proximity = request.args.get('proximity')  # Not implemented yet, would need geolocation
    
    if store_id:
        query = """
            SELECT s.store_id, s.name, s.image_nom, a.house_number, 
                   a.street, a.postal_code, a.city, a.civic_number
            FROM StoreLocation s
            JOIN CustomerAddress a ON s.address_id = a.address_id
            WHERE s.store_id = %s
        """
        stores = execute_query(query, (store_id,))
    else:
        query = """
            SELECT s.store_id, s.name, s.image_nom, a.house_number, 
                   a.street, a.postal_code, a.city, a.civic_number
            FROM StoreLocation s
            JOIN CustomerAddress a ON s.address_id = a.address_id
        """
        stores = execute_query(query)
    
    # Format store data - include both full address string and individual components
    formatted_stores = []
    for store in stores:
        formatted_stores.append({
            "store_id": store[0],
            "name": store[1],
            "image_nom": store[2],
            "address": f"{store[3]} {store[4]}, {store[6]}, {store[5]}",
            "address_components": {
                "house_number": store[3],
                "street": store[4],
                "postal_code": store[5],
                "city": store[6],
                "civic_number": store[7]
            }
        })
    
    return jsonify(formatted_stores)

# PRODUCT ENDPOINTS
@app.route('/getProducts', methods=['GET'])
def get_products():
    product_id = request.args.get('productId')
    
    if product_id:
        query = "SELECT * FROM Product WHERE product_id = %s"
        products = execute_query(query, (product_id,))
    else:
        query = "SELECT * FROM Product"
        products = execute_query(query)
    
    formatted_products = []
    for product in products:
        formatted_products.append({
            "product_id": product[0],
            "name": product[1],
            "description": product[2],
            "price": float(product[3]),
            "category": product[4],
            "is_available": bool(product[5]),
            "image_nom": product[6]
        })
    
    return jsonify(formatted_products)

# INVENTORY ENDPOINTS
@app.route('/getAvailability', methods=['GET'])
def get_availability():
    store_id = request.args.get('storeId')
    product_id = request.args.get('productId')
    in_stock = request.args.get('inStock')
    
    params = []
    conditions = []
    
    if store_id:
        conditions.append("pi.store_id = %s")
        params.append(store_id)
    
    if product_id:
        conditions.append("pi.product_id = %s")
        params.append(product_id)
    
    if in_stock:
        try:
            in_stock_value = int(in_stock)
            conditions.append("pi.quantity >= %s")
            params.append(in_stock_value)
        except ValueError:
            pass
    
    query = """
        SELECT pi.store_id, pi.product_id, pi.quantity, 
               p.name as product_name, p.price, s.name as store_name
        FROM ProductInventory pi
        JOIN Product p ON pi.product_id = p.product_id
        JOIN StoreLocation s ON pi.store_id = s.store_id
    """
    
    if conditions:
        query += " WHERE " + " AND ".join(conditions)
    
    inventory = execute_query(query, tuple(params) if params else None)
    
    formatted_inventory = []
    for item in inventory:
        formatted_inventory.append({
            "store_id": item[0],
            "product_id": item[1],
            "quantity": item[2],
            "product_name": item[3],
            "price": float(item[4]),
            "store_name": item[5]
        })
    
    return jsonify(formatted_inventory)

# USER ENDPOINTS
@app.route('/createUser', methods=['POST'])
def create_user():
    data = request.get_json()
    
    required_fields = ["email", "password", "lastName", "firstName", "phoneNumber", "userType"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    email = data["email"]
    password = data["password"]
    last_name = data["lastName"]
    first_name = data["firstName"]
    phone_number = data["phoneNumber"]
    user_type = data["userType"]
    
    if user_type not in ["customer", "deliverer", "admin"]:
        return jsonify({"status": "error", "message": "Invalid userType"}), 400
    
    if user_type == "deliverer":
        if "licensePlate" not in data or "licenseNumber" not in data:
            return jsonify({"status": "error", "message": "Deliverer requires licensePlate and licenseNumber"}), 400
        license_plate = data["licensePlate"]
        license_number = data["licenseNumber"]
    else:
        license_plate = None
        license_number = 0
    
    existing = execute_query("SELECT * FROM UserAccount WHERE email = %s", (email,))
    if existing:
        return jsonify({"status": "error", "message": "Email already exists"}), 400
    
    new_user_id = get_next_id("UserAccount", "user_id")
    
    execute_query(
        """INSERT INTO UserAccount 
           (user_id, email, password, last_name, first_name, phone_number, user_type, 
            license_plate, license_number, total_earnings) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (new_user_id, email, password, last_name, first_name, phone_number, user_type, 
         license_plate, license_number, 0.0),
        fetch=False
    )
    
    return jsonify({"status": "success"})

@app.route('/connectUser', methods=['POST'])
def connect_user():
    data = request.get_json()
    
    if "email" not in data or "password" not in data:
        return jsonify({"status": "error", "message": "Email and password are required"}), 400
    
    email = data["email"]
    password = data["password"]
    
    user = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s", 
        (email, password)
    )
    
    if user:
        user_data = {
            "email": user[0][1],
            "lastName": user[0][3],
            "firstName": user[0][4],
            "phoneNumber": user[0][5],
            "userType": user[0][6],
            "licensePlate": user[0][7] if user[0][7] else None,
            "licenseNumber": user[0][8] if user[0][8] else None,
            "totalEarnings": float(user[0][9]) if user[0][9] else 0.0
        }
        return jsonify({"status": "success", "user": user_data})
    
    return jsonify({"status": "error", "message": "Invalid credentials"}), 401

# ORDER ENDPOINTS
@app.route('/createOrder', methods=['POST'])
def create_order():
    data = request.get_json()
    
    required_fields = ["userId", "storeId", "addressId", "items", "paymentMethod"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    user_id = data["userId"]
    store_id = data["storeId"]
    address_id = data["addressId"]
    items = data["items"]
    payment_method = data["paymentMethod"]
    
    payment_fields = ["cardNumber", "cvcCard", "expiryDate"]
    for field in payment_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing payment field: {field}"}), 400
    
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    store = execute_query("SELECT * FROM StoreLocation WHERE store_id = %s", (store_id,))
    address = execute_query("SELECT * FROM CustomerAddress WHERE address_id = %s", (address_id,))
    
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    if not store:
        return jsonify({"status": "error", "message": "Store not found"}), 404
    if not address:
        return jsonify({"status": "error", "message": "Address not found"}), 404
    
    total_amount = 0
    for item in items:
        product_id = item["productId"]
        quantity = item["quantity"]
        
        product = execute_query(
            "SELECT price FROM Product WHERE product_id = %s", 
            (product_id,)
        )
        
        if not product:
            return jsonify({"status": "error", "message": f"Product {product_id} not found"}), 404
        
        inventory = execute_query(
            "SELECT quantity FROM ProductInventory WHERE store_id = %s AND product_id = %s",
            (store_id, product_id)
        )
        
        if not inventory or inventory[0][0] < quantity:
            return jsonify({"status": "error", "message": f"Product {product_id} not available in requested quantity"}), 400
        
        total_amount += float(product[0][0]) * quantity
    
    order_id = get_next_id("CustomerOrder", "order_id")
    creation_date = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    execute_query(
        """INSERT INTO CustomerOrder 
           (order_id, creation_date, status, total_amount, address_id, store_id, user_id) 
           VALUES (%s, %s, %s, %s, %s, %s, %s)""",
        (order_id, creation_date, "pending", total_amount, address_id, store_id, user_id),
        fetch=False
    )
    
    for item in items:
        product_id = item["productId"]
        quantity = item["quantity"]
        
        execute_query(
            "INSERT INTO OrderItems (order_id, product_id, quantity) VALUES (%s, %s, %s)",
            (order_id, product_id, quantity),
            fetch=False
        )
        
        execute_query(
            """UPDATE ProductInventory 
               SET quantity = quantity - %s 
               WHERE store_id = %s AND product_id = %s""",
            (quantity, store_id, product_id),
            fetch=False
        )
    
    payment_id = get_next_id("PaymentTransaction", "payment_id")
    
    execute_query(
        """INSERT INTO PaymentTransaction 
           (payment_id, payment_method, amount, payment_date, is_completed, 
            order_id, user_id, card_number, CVC_card, expiry_date) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (payment_id, payment_method, total_amount, creation_date, True,
         order_id, user_id, data["cardNumber"], data["cvcCard"], data["expiryDate"]),
        fetch=False
    )
    
    return jsonify({"status": "success", "orderId": order_id})

@app.route('/cancelOrder', methods=['POST'])
def cancel_order():
    data = request.get_json()
    
    if "orderId" not in data:
        return jsonify({"status": "error", "message": "Order ID is required"}), 400
    
    order_id = data["orderId"]
    
    order = execute_query(
        "SELECT * FROM CustomerOrder WHERE order_id = %s", 
        (order_id,)
    )
    
    if not order:
        return jsonify({"status": "error", "message": "Order not found"}), 404
    
    if order[0][2] == "cancelled":
        return jsonify({"status": "error", "message": "Order is already cancelled"}), 400
    
    items = execute_query(
        "SELECT product_id, quantity FROM OrderItems WHERE order_id = %s", 
        (order_id,)
    )
    
    for item in items:
        product_id = item[0]
        quantity = item[1]
        store_id = order[0][5]
        
        execute_query(
            """UPDATE ProductInventory 
               SET quantity = quantity + %s 
               WHERE store_id = %s AND product_id = %s""",
            (quantity, store_id, product_id),
            fetch=False
        )
    
    execute_query(
        "UPDATE CustomerOrder SET status = 'cancelled' WHERE order_id = %s",
        (order_id,),
        fetch=False
    )
    
    return jsonify({"status": "success", "message": "Order cancelled successfully"})

# ADMIN ENDPOINTS - USER MANAGEMENT
@app.route('/admin/createUser', methods=['POST'])
def admin_create_user():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    data.pop("adminEmail", None)
    data.pop("adminPassword", None)
    
    required_fields = ["email", "password", "lastName", "firstName", "phoneNumber", "userType"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    email = data["email"]
    password = data["password"]
    last_name = data["lastName"]
    first_name = data["firstName"]
    phone_number = data["phoneNumber"]
    user_type = data["userType"]
    
    if user_type not in ["customer", "deliverer", "admin"]:
        return jsonify({"status": "error", "message": "Invalid userType"}), 400
    
    if user_type == "deliverer":
        if "licensePlate" not in data or "licenseNumber" not in data:
            return jsonify({"status": "error", "message": "Deliverer requires licensePlate and licenseNumber"}), 400
        license_plate = data["licensePlate"]
        license_number = data["licenseNumber"]
    else:
        license_plate = None
        license_number = 0
    
    existing = execute_query("SELECT * FROM UserAccount WHERE email = %s", (email,))
    if existing:
        return jsonify({"status": "error", "message": "Email already exists"}), 400
    
    new_user_id = get_next_id("UserAccount", "user_id")
    
    execute_query(
        """INSERT INTO UserAccount 
           (user_id, email, password, last_name, first_name, phone_number, user_type, 
            license_plate, license_number, total_earnings) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (new_user_id, email, password, last_name, first_name, phone_number, user_type, 
         license_plate, license_number, 0.0),
        fetch=False
    )
    
    return jsonify({"status": "success"})

@app.route('/admin/modifyUser', methods=['POST'])
def admin_modify_user():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    data.pop("adminEmail", None)
    data.pop("adminPassword", None)
    
    if "userId" not in data:
        return jsonify({"status": "error", "message": "User ID is required"}), 400
    
    user_id = data["userId"]
    
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    
    update_fields = []
    params = []
    
    field_mapping = {
        "email": "email",
        "password": "password",
        "lastName": "last_name",
        "firstName": "first_name",
        "phoneNumber": "phone_number",
        "userType": "user_type",
        "licensePlate": "license_plate",
        "licenseNumber": "license_number",
        "totalEarnings": "total_earnings"
    }
    
    for key, db_field in field_mapping.items():
        if key in data:
            update_fields.append(f"{db_field} = %s")
            params.append(data[key])
    
    if not update_fields:
        return jsonify({"status": "error", "message": "No fields to update"}), 400
    
    params.append(user_id)
    
    query = f"UPDATE UserAccount SET {', '.join(update_fields)} WHERE user_id = %s"
    execute_query(query, tuple(params), fetch=False)
    
    return jsonify({"status": "success", "message": "User updated successfully"})

@app.route('/admin/deleteUser', methods=['POST'])
def admin_delete_user():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "userId" not in data:
        return jsonify({"status": "error", "message": "User ID is required"}), 400
    
    user_id = data["userId"]
    
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    
    try:
        execute_query("DELETE FROM UserAccount WHERE user_id = %s", (user_id,), fetch=False)
        return jsonify({"status": "success", "message": "User deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# ADMIN ENDPOINTS - STORE MANAGEMENT
@app.route('/admin/createStore', methods=['POST'])
def admin_create_store():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    required_fields = ["name", "houseNumber", "street", "postalCode", "city", "civicNumber"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    address_id = get_next_id("CustomerAddress", "address_id")
    
    execute_query(
        """INSERT INTO CustomerAddress 
           (address_id, house_number, street, postal_code, city, user_id, civic_number) 
           VALUES (%s, %s, %s, %s, %s, %s, %s)""",
        (address_id, data["houseNumber"], data["street"], data["postalCode"], 
         data["city"], admin[0][0], data["civicNumber"]),
        fetch=False
    )
    
    store_id = get_next_id("StoreLocation", "store_id")
    image_nom = data.get("imageNom", "placeholder")
    
    execute_query(
        "INSERT INTO StoreLocation (store_id, name, address_id, image_nom) VALUES (%s, %s, %s, %s)",
        (store_id, data["name"], address_id, image_nom),
        fetch=False
    )
    
    return jsonify({"status": "success", "storeId": store_id})

@app.route('/admin/modifyStore', methods=['POST'])
def admin_modify_store():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "storeId" not in data:
        return jsonify({"status": "error", "message": "Store ID is required"}), 400
    
    store_id = data["storeId"]
    
    store = execute_query(
        "SELECT * FROM StoreLocation WHERE store_id = %s", 
        (store_id,)
    )
    
    if not store:
        return jsonify({"status": "error", "message": "Store not found"}), 404
    
    store_update_fields = []
    store_params = []
    
    if "name" in data:
        store_update_fields.append("name = %s")
        store_params.append(data["name"])
    
    if "imageNom" in data:
        store_update_fields.append("image_nom = %s")
        store_params.append(data["imageNom"])
    
    if store_update_fields:
        store_params.append(store_id)
        query = f"UPDATE StoreLocation SET {', '.join(store_update_fields)} WHERE store_id = %s"
        execute_query(query, tuple(store_params), fetch=False)
    
    address_id = store[0][2]
    address_update_fields = []
    address_params = []
    
    address_field_mapping = {
        "houseNumber": "house_number",
        "street": "street",
        "postalCode": "postal_code",
        "city": "city",
        "civicNumber": "civic_number"
    }
    
    for key, db_field in address_field_mapping.items():
        if key in data:
            address_update_fields.append(f"{db_field} = %s")
            address_params.append(data[key])
    
    if address_update_fields:
        address_params.append(address_id)
        query = f"UPDATE CustomerAddress SET {', '.join(address_update_fields)} WHERE address_id = %s"
        execute_query(query, tuple(address_params), fetch=False)
    
    return jsonify({"status": "success", "message": "Store updated successfully"})

@app.route('/admin/deleteStore', methods=['POST'])
def admin_delete_store():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "storeId" not in data:
        return jsonify({"status": "error", "message": "Store ID is required"}), 400
    
    store_id = data["storeId"]
    
    store = execute_query("SELECT * FROM StoreLocation WHERE store_id = %s", (store_id,))
    if not store:
        return jsonify({"status": "error", "message": "Store not found"}), 404
    
    address_id = store[0][2]
    
    try:
        execute_query("DELETE FROM ProductInventory WHERE store_id = %s", (store_id,), fetch=False)
        execute_query("DELETE FROM StoreLocation WHERE store_id = %s", (store_id,), fetch=False)
        execute_query("DELETE FROM CustomerAddress WHERE address_id = %s", (address_id,), fetch=False)
        
        return jsonify({"status": "success", "message": "Store deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

# ADMIN ENDPOINTS - PRODUCT MANAGEMENT
@app.route('/admin/createProduct', methods=['POST'])
def admin_create_product():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    required_fields = ["name", "price", "category", "isAvailable"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    product_id = get_next_id("Product", "product_id")
    description = data.get("description", "")
    image_nom = data.get("imageNom", "placeholder")
    
    execute_query(
        """INSERT INTO Product 
           (product_id, name, description, price, category, is_available, image_nom) 
           VALUES (%s, %s, %s, %s, %s, %s, %s)""",
        (product_id, data["name"], description, data["price"], 
         data["category"], data["isAvailable"], image_nom),
        fetch=False
    )
    
    return jsonify({"status": "success", "productId": product_id})

@app.route('/admin/modifyProduct', methods=['POST'])
def admin_modify_product():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "productId" not in data:
        return jsonify({"status": "error", "message": "Product ID is required"}), 400
    
    product_id = data["productId"]
    
    product = execute_query("SELECT * FROM Product WHERE product_id = %s", (product_id,))
    if not product:
        return jsonify({"status": "error", "message": "Product not found"}), 404
    
    update_fields = []
    params = []
    
    field_mapping = {
        "name": "name",
        "description": "description",
        "price": "price",
        "category": "category",
        "isAvailable": "is_available",
        "imageNom": "image_nom"
    }
    
    for key, db_field in field_mapping.items():
        if key in data:
            update_fields.append(f"{db_field} = %s")
            params.append(data[key])
    
    if not update_fields:
        return jsonify({"status": "error", "message": "No fields to update"}), 400
    
    params.append(product_id)
    
    query = f"UPDATE Product SET {', '.join(update_fields)} WHERE product_id = %s"
    execute_query(query, tuple(params), fetch=False)
    
    return jsonify({"status": "success", "message": "Product updated successfully"})

@app.route('/admin/deleteProduct', methods=['POST'])
def admin_delete_product():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "productId" not in data:
        return jsonify({"status": "error", "message": "Product ID is required"}), 400
    
    product_id = data["productId"]
    
    product = execute_query("SELECT * FROM Product WHERE product_id = %s", (product_id,))
    if not product:
        return jsonify({"status": "error", "message": "Product not found"}), 404
    
    try:
        # First remove product from all inventories
        execute_query("DELETE FROM ProductInventory WHERE product_id = %s", (product_id,), fetch=False)
        # Then delete the product itself
        execute_query("DELETE FROM Product WHERE product_id = %s", (product_id,), fetch=False)
        
        return jsonify({"status": "success", "message": "Product deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/admin/getUsers', methods=['POST'])
def admin_get_users():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    # Check if the user exists and is an admin
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Get all users from the database
    users = execute_query("SELECT * FROM UserAccount")
    
    # Format user data for response
    formatted_users = []
    for user in users:
        formatted_users.append({
            "userId": user[0],
            "email": user[1],
            # Note: Omitting password for security reasons
            "lastName": user[3],
            "firstName": user[4],
            "phoneNumber": user[5],
            "userType": user[6],
            "licensePlate": user[7] if user[7] else None,
            "licenseNumber": user[8] if user[8] else None,
            "totalEarnings": float(user[9]) if user[9] else 0.0
        })
    
    return jsonify({"status": "success", "users": formatted_users})


if __name__ == '__main__':
    app.run(host="0.0.0.0")

#TODO:
#Fix Create script to allow cascade delete and remove imagePath
#Add images for each products