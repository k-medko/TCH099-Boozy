from flask import Flask, request, jsonify, send_from_directory
from flask_mysqldb import MySQL
from flask_cors import CORS
import os
from datetime import datetime

app = Flask(__name__)
CORS(app)

app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'BOOZY_SERVER'
app.config['MYSQL_PASSWORD'] = 'Boozyadmin1234'
app.config['MYSQL_DB'] = 'boozy_database'
mysql = MySQL(app)

def execute_query(query, params=None, fetch=True):
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
    max_id = execute_query(f"SELECT MAX({id_column}) FROM {table_name}")
    return (max_id[0][0] + 1) if max_id and max_id[0][0] else 1

def get_address_string(address_id):
    address = execute_query(
        "SELECT civic, apartment, street, city, postal_code FROM AddressLine WHERE address_id = %s", 
        (address_id,)
    )
    if address:
        apt_str = f" apt {address[0][1]}" if address[0][1] else ""
        return f"{address[0][0]}{apt_str} {address[0][2]}, {address[0][3]}, {address[0][4]}"
    return None

@app.route('/')
def home():
    return jsonify({"message": "Welcome to the Boozy API!!!"})

@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'favicon.ico', mimetype='image/vnd.microsoft.icon')

# Fallback image for products - using placeholder since Product table doesn't have image fields
@app.route('/getImages/product/<product_id>', methods=['GET'])
def get_product_image(product_id):
    # Since Product table doesn't have image fields in schema, serve default placeholder
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'placeholder.jpg')

# Fallback image for stores - using placeholder since Shop table doesn't have image fields
@app.route('/getImages/store/<store_id>', methods=['GET'])
def get_store_image(store_id):
    # Since Shop table doesn't have image fields in schema, serve default placeholder
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'placeholder.jpg')

@app.route('/getStores', methods=['GET'])
def get_stores():
    store_id = request.args.get('storeId')
    proximity = request.args.get('proximity')
    
    if store_id:
        query = """
            SELECT s.shop_id, s.name, a.civic, a.apartment,
                   a.street, a.postal_code, a.city
            FROM Shop s
            JOIN AddressLine a ON s.address_id = a.address_id
            WHERE s.shop_id = %s
        """
        stores = execute_query(query, (store_id,))
    else:
        query = """
            SELECT s.shop_id, s.name, a.civic, a.apartment,
                   a.street, a.postal_code, a.city
            FROM Shop s
            JOIN AddressLine a ON s.address_id = a.address_id
        """
        stores = execute_query(query)
    
    formatted_stores = []
    for store in stores:
        apt_str = f" apt {store[3]}" if store[3] else ""
        formatted_stores.append({
            "shop_id": store[0],
            "name": store[1],
            "address": f"{store[2]}{apt_str} {store[4]}, {store[6]}, {store[5]}",
            "address_components": {
                "civic": store[2],
                "apartment": store[3],
                "street": store[4],
                "postal_code": store[5],
                "city": store[6]
            }
        })
    
    return jsonify(formatted_stores)

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
            "alcohol": float(product[6])
        })
    
    return jsonify(formatted_products)

@app.route('/getAvailability', methods=['GET'])
def get_availability():
    shop_id = request.args.get('shopId')
    product_id = request.args.get('productId')
    in_stock = request.args.get('inStock')
    
    params = []
    conditions = []
    
    if shop_id:
        conditions.append("sp.shop_id = %s")
        params.append(shop_id)
    
    if product_id:
        conditions.append("sp.product_id = %s")
        params.append(product_id)
    
    if in_stock:
        try:
            in_stock_value = int(in_stock)
            conditions.append("sp.quantity >= %s")
            params.append(in_stock_value)
        except ValueError:
            pass
    
    query = """
        SELECT sp.shop_id, sp.product_id, sp.quantity, 
               p.name as product_name, p.price, p.category, p.is_available, p.alcohol,
               s.name as shop_name, a.civic, a.street, a.city, a.postal_code
        FROM ShopProduct sp
        JOIN Product p ON sp.product_id = p.product_id
        JOIN Shop s ON sp.shop_id = s.shop_id
        JOIN AddressLine a ON s.address_id = a.address_id
    """
    
    if conditions:
        query += " WHERE " + " AND ".join(conditions)
    
    inventory = execute_query(query, tuple(params) if params else None)
    
    formatted_inventory = []
    for item in inventory:
        formatted_inventory.append({
            "shop_id": item[0],
            "product_id": item[1],
            "quantity": item[2],
            "product": {
                "name": item[3],
                "price": float(item[4]),
                "category": item[5],
                "is_available": bool(item[6]),
                "alcohol": float(item[7])
            },
            "store": {
                "name": item[8],
                "address": f"{item[9]} {item[10]}, {item[11]}, {item[12]}"
            }
        })
    
    return jsonify(formatted_inventory)

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
        if "licensePlate" not in data:
            return jsonify({"status": "error", "message": "Deliverer requires licensePlate"}), 400
        license_plate = data["licensePlate"]
    else:
        license_plate = None
    
    existing = execute_query("SELECT * FROM UserAccount WHERE email = %s", (email,))
    if existing:
        return jsonify({"status": "error", "message": "Email already exists"}), 400
    
    address_id = None
    if "address" in data:
        address = data["address"]
        new_address_id = get_next_id("AddressLine", "address_id")
        
        execute_query(
            """INSERT INTO AddressLine 
               (address_id, civic, apartment, street, city, postal_code) 
               VALUES (%s, %s, %s, %s, %s, %s)""",
            (new_address_id, address.get("civic"), address.get("apartment"), 
             address.get("street"), address.get("city"), address.get("postalCode")),
            fetch=False
        )
        address_id = new_address_id
    
    new_user_id = get_next_id("UserAccount", "user_id")
    
    execute_query(
        """INSERT INTO UserAccount 
           (user_id, email, password, last_name, first_name, phone_number, address_id, user_type, 
            license_plate, total_earnings) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (new_user_id, email, password, last_name, first_name, phone_number, address_id, user_type, 
         license_plate, 0.0),
        fetch=False
    )
    
    return jsonify({"status": "success", "userId": new_user_id})

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
        address = None
        if user[0][6]:  # address_id
            address_data = execute_query(
                "SELECT * FROM AddressLine WHERE address_id = %s",
                (user[0][6],)
            )
            if address_data:
                address = {
                    "civic": address_data[0][1],
                    "apartment": address_data[0][2],
                    "street": address_data[0][3],
                    "city": address_data[0][4],
                    "postalCode": address_data[0][5]
                }
        
        user_data = {
            "userId": user[0][0],
            "email": user[0][1],
            "lastName": user[0][3],
            "firstName": user[0][4],
            "phoneNumber": user[0][5],
            "userType": user[0][7],
            "licensePlate": user[0][8] if user[0][8] else None,
            "totalEarnings": float(user[0][9]) if user[0][9] else 0.0,
            "address": address
        }
        return jsonify({"status": "success", "user": user_data})
    
    return jsonify({"status": "error", "message": "Invalid credentials"}), 401

@app.route('/createOrder', methods=['POST'])
def create_order():
    data = request.get_json()
    
    required_fields = ["userId", "shopId", "addressId", "items", "paymentMethod"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    user_id = data["userId"]
    shop_id = data["shopId"]
    address_id = data["addressId"]
    items = data["items"]
    payment_method = data["paymentMethod"]
    
    payment_fields = ["cardName", "cardNumber", "cvcCard", "expiryDateMonth", "expiryDateYear"]
    for field in payment_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing payment field: {field}"}), 400
    
    # Find available deliverer - improved to check for deliverers actually available
    deliverer = execute_query(
        """SELECT user_id FROM UserAccount 
           WHERE user_type = 'deliverer' 
           AND user_id NOT IN (
               SELECT DISTINCT deliverer_id FROM ClientOrder 
               WHERE status IN ('Searching', 'InRoute')
           )
           LIMIT 1"""
    )
    
    if not deliverer:
        # Fallback - get any deliverer if no "available" ones
        deliverer = execute_query("SELECT user_id FROM UserAccount WHERE user_type = 'deliverer' LIMIT 1")
        if not deliverer:
            return jsonify({"status": "error", "message": "No deliverer available"}), 400
    
    deliverer_id = deliverer[0][0]
    
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    address = execute_query("SELECT * FROM AddressLine WHERE address_id = %s", (address_id,))
    
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
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
            "SELECT quantity FROM ShopProduct WHERE shop_id = %s AND product_id = %s",
            (shop_id, product_id)
        )
        
        if not inventory or inventory[0][0] < quantity:
            return jsonify({"status": "error", "message": f"Product {product_id} not available in requested quantity"}), 400
        
        total_amount += float(product[0][0]) * quantity
    
    order_id = get_next_id("ClientOrder", "client_order_id")
    creation_date = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    
    execute_query(
        """INSERT INTO ClientOrder 
           (client_order_id, creation_date, status, total_amount, address_id, shop_id, customer_id, deliverer_id) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
        (order_id, creation_date, "Searching", total_amount, address_id, shop_id, user_id, deliverer_id),
        fetch=False
    )
    
    for item in items:
        product_id = item["productId"]
        quantity = item["quantity"]
        
        execute_query(
            "INSERT INTO ClientOrderProduct (client_order_id, product_id, quantity) VALUES (%s, %s, %s)",
            (order_id, product_id, quantity),
            fetch=False
        )
        
        execute_query(
            """UPDATE ShopProduct 
               SET quantity = quantity - %s 
               WHERE shop_id = %s AND product_id = %s""",
            (quantity, shop_id, product_id),
            fetch=False
        )
    
    payment_id = get_next_id("Payment", "payment_id")
    
    execute_query(
        """INSERT INTO Payment 
           (payment_id, payment_method, amount, payment_date, is_completed, 
            client_order_id, user_id, card_name, card_number, CVC_card, expiry_date_month, expiry_date_year) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (payment_id, payment_method, total_amount, creation_date, True,
         order_id, user_id, data["cardName"], data["cardNumber"], data["cvcCard"], 
         data["expiryDateMonth"], data["expiryDateYear"]),
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
        "SELECT * FROM ClientOrder WHERE client_order_id = %s", 
        (order_id,)
    )
    
    if not order:
        return jsonify({"status": "error", "message": "Order not found"}), 404
    
    if order[0][2] == "Cancelled":
        return jsonify({"status": "error", "message": "Order is already cancelled"}), 400
    
    items = execute_query(
        "SELECT product_id, quantity FROM ClientOrderProduct WHERE client_order_id = %s", 
        (order_id,)
    )
    
    for item in items:
        product_id = item[0]
        quantity = item[1]
        shop_id = order[0][5]
        
        execute_query(
            """UPDATE ShopProduct 
               SET quantity = quantity + %s 
               WHERE shop_id = %s AND product_id = %s""",
            (quantity, shop_id, product_id),
            fetch=False
        )
    
    execute_query(
        "UPDATE ClientOrder SET status = 'Cancelled' WHERE client_order_id = %s",
        (order_id,),
        fetch=False
    )
    
    return jsonify({"status": "success", "message": "Order cancelled successfully"})

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
    
    user_type = data["userType"]
    if user_type == "deliverer" and "licensePlate" not in data:
        return jsonify({"status": "error", "message": "Deliverer requires licensePlate"}), 400
    
    address_id = None
    if "address" in data:
        address = data["address"]
        new_address_id = get_next_id("AddressLine", "address_id")
        
        execute_query(
            """INSERT INTO AddressLine 
               (address_id, civic, apartment, street, city, postal_code) 
               VALUES (%s, %s, %s, %s, %s, %s)""",
            (new_address_id, address.get("civic"), address.get("apartment"), 
             address.get("street"), address.get("city"), address.get("postalCode")),
            fetch=False
        )
        address_id = new_address_id
    
    new_user_id = get_next_id("UserAccount", "user_id")
    
    execute_query(
        """INSERT INTO UserAccount 
           (user_id, email, password, last_name, first_name, phone_number, address_id, user_type, 
            license_plate, total_earnings) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (new_user_id, data["email"], data["password"], data["lastName"], data["firstName"], 
         data["phoneNumber"], address_id, data["userType"], 
         data.get("licensePlate"), data.get("totalEarnings", 0.0)),
        fetch=False
    )
    
    return jsonify({"status": "success", "userId": new_user_id})

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
        "totalEarnings": "total_earnings"
    }
    
    for key, db_field in field_mapping.items():
        if key in data:
            update_fields.append(f"{db_field} = %s")
            params.append(data[key])
    
    if "address" in data and data["address"]:
        address = data["address"]
        current_address_id = user[0][6]
        
        if current_address_id:
            address_updates = []
            address_params = []
            
            address_field_mapping = {
                "civic": "civic",
                "apartment": "apartment",
                "street": "street",
                "city": "city",
                "postalCode": "postal_code"
            }
            
            for key, db_field in address_field_mapping.items():
                if key in address:
                    address_updates.append(f"{db_field} = %s")
                    address_params.append(address[key])
            
            if address_updates:
                address_params.append(current_address_id)
                query = f"UPDATE AddressLine SET {', '.join(address_updates)} WHERE address_id = %s"
                execute_query(query, tuple(address_params), fetch=False)
        else:
            new_address_id = get_next_id("AddressLine", "address_id")
            
            execute_query(
                """INSERT INTO AddressLine 
                   (address_id, civic, apartment, street, city, postal_code) 
                   VALUES (%s, %s, %s, %s, %s, %s)""",
                (new_address_id, address.get("civic"), address.get("apartment"), 
                 address.get("street"), address.get("city"), address.get("postalCode")),
                fetch=False
            )
            
            update_fields.append("address_id = %s")
            params.append(new_address_id)
    
    if update_fields:
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
    
    required_fields = ["name", "civic", "street", "postalCode", "city"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    address_id = get_next_id("AddressLine", "address_id")
    
    execute_query(
        """INSERT INTO AddressLine 
           (address_id, civic, apartment, street, postal_code, city) 
           VALUES (%s, %s, %s, %s, %s, %s)""",
        (address_id, data["civic"], data.get("apartment"), data["street"], 
         data["postalCode"], data["city"]),
        fetch=False
    )
    
    shop_id = get_next_id("Shop", "shop_id")
    
    execute_query(
        "INSERT INTO Shop (shop_id, name, address_id) VALUES (%s, %s, %s)",
        (shop_id, data["name"], address_id),
        fetch=False
    )
    
    return jsonify({"status": "success", "shopId": shop_id})

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
    
    if "shopId" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    
    shop_id = data["shopId"]
    
    shop = execute_query(
        "SELECT * FROM Shop WHERE shop_id = %s", 
        (shop_id,)
    )
    
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    
    shop_update_fields = []
    shop_params = []
    
    if "name" in data:
        shop_update_fields.append("name = %s")
        shop_params.append(data["name"])
    
    # Removed image_nom as it's not in the schema
    
    if shop_update_fields:
        shop_params.append(shop_id)
        query = f"UPDATE Shop SET {', '.join(shop_update_fields)} WHERE shop_id = %s"
        execute_query(query, tuple(shop_params), fetch=False)
    
    address_id = shop[0][2]
    address_update_fields = []
    address_params = []
    
    address_field_mapping = {
        "civic": "civic",
        "apartment": "apartment",
        "street": "street",
        "postalCode": "postal_code",
        "city": "city"
    }
    
    for key, db_field in address_field_mapping.items():
        if key in data:
            address_update_fields.append(f"{db_field} = %s")
            address_params.append(data[key])
    
    if address_update_fields:
        address_params.append(address_id)
        query = f"UPDATE AddressLine SET {', '.join(address_update_fields)} WHERE address_id = %s"
        execute_query(query, tuple(address_params), fetch=False)
    
    return jsonify({"status": "success", "message": "Shop updated successfully"})

@app.route('/admin/deleteStore', methods=['POST'])
def admin_delete_store():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    # Check admin credentials
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "shopId" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    
    shop_id = data["shopId"]
    
    # Check if the shop exists
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    
    # Delete shop-related products, then the shop
    try:
        execute_query("DELETE FROM ShopProduct WHERE shop_id = %s", (shop_id,), fetch=False)
        execute_query("DELETE FROM Shop WHERE shop_id = %s", (shop_id,), fetch=False)
        return jsonify({"status": "success", "message": "Shop deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/admin/createProduct', methods=['POST'])
def admin_create_product():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    # Check admin credentials
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    required_fields = ["name", "price", "category", "isAvailable", "alcohol"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    product_id = get_next_id("Product", "product_id")
    description = data.get("description", "")
    
    execute_query(
        """
        INSERT INTO Product (product_id, name, description, price, category, is_available, alcohol)
        VALUES (%s, %s, %s, %s, %s, %s, %s)
        """,
        (
            product_id,
            data["name"],
            description,
            data["price"],
            data["category"],
            data["isAvailable"],
            data["alcohol"]
        ),
        fetch=False
    )
    
    return jsonify({"status": "success", "productId": product_id})


@app.route('/admin/modifyProduct', methods=['POST'])
def admin_modify_product():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    # Check admin credentials
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
        (admin_email, admin_password)
    )
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "productId" not in data:
        return jsonify({"status": "error", "message": "Product ID is required"}), 400
    
    product_id = data["productId"]
    
    # Check if the product exists
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
        "alcohol": "alcohol"
    }
    
    for key, db_field in field_mapping.items():
        if key in data:
            update_fields.append(f"{db_field} = %s")
            params.append(data[key])
    
    if not update_fields:
        return jsonify({"status": "error", "message": "No fields to update"}), 400
    
    # Finalize query
    params.append(product_id)
    query = f"UPDATE Product SET {', '.join(update_fields)} WHERE product_id = %s"
    execute_query(query, tuple(params), fetch=False)
    
    return jsonify({"status": "success", "message": "Product updated successfully"})


@app.route('/admin/deleteProduct', methods=['POST'])
def admin_delete_product():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    # Check admin credentials
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
        (admin_email, admin_password)
    )
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "productId" not in data:
        return jsonify({"status": "error", "message": "Product ID is required"}), 400
    
    product_id = data["productId"]
    
    # Check if product exists
    product = execute_query("SELECT * FROM Product WHERE product_id = %s", (product_id,))
    if not product:
        return jsonify({"status": "error", "message": "Product not found"}), 404
    
    # Delete product references, then product
    try:
        execute_query("DELETE FROM ShopProduct WHERE product_id = %s", (product_id,), fetch=False)
        execute_query("DELETE FROM Product WHERE product_id = %s", (product_id,), fetch=False)
        return jsonify({"status": "success", "message": "Product deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500


@app.route('/admin/getUsers', methods=['POST'])
def admin_get_users():
    data = request.get_json()
    
    admin_email = data.get("adminEmail")
    admin_password = data.get("adminPassword")
    
    # Verify admin in UserAccount
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
        (admin_email, admin_password)
    )
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Get all users with their addresses
    users = execute_query("""
        SELECT 
            u.user_id,
            u.email,
            u.password,
            u.last_name,
            u.first_name,
            u.phone_number,
            u.address_id,
            u.user_type,
            u.license_plate,
            u.total_earnings,
            a.address_id,
            a.civic,
            a.apartment,
            a.street,
            a.city,
            a.postal_code
        FROM UserAccount u
        LEFT JOIN AddressLine a ON u.address_id = a.address_id
    """)
    
    formatted_users = []
    for user in users:
        user_data = {
            "userId": user[0],
            "email": user[1],
            # password = user[2] but omitting for security
            "lastName": user[3],
            "firstName": user[4],
            "phoneNumber": user[5],
            "userType": user[7],
            "licensePlate": user[8] if user[8] else None,
            "totalEarnings": float(user[9]) if user[9] else 0.0
        }
        
        # If address_id is not null, include address details
        if user[6]:
            user_data["address"] = {
                "addressId": user[10],
                "civic": user[11],
                "apartment": user[12] if user[12] else None,
                "street": user[13],
                "city": user[14],
                "postalCode": user[15],
                "fullAddress": (
                    f"{user[11]} {user[13]}"
                    + (f", {user[12]}" if user[12] else "")
                    + f", {user[14]}, {user[15]}"
                )
            }
        
        formatted_users.append(user_data)
    
    return jsonify({"status": "success", "users": formatted_users})


if __name__ == '__main__':
    app.run(host="0.0.0.0")
