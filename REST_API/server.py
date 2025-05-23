from flask import Flask, redirect, request, jsonify, send_from_directory
from flask_mysqldb import MySQL
from flask_cors import CORS
import os
import base64
import re
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
    results = cursor.fetchall() if fetch else (mysql.connection.commit() or (cursor.lastrowid if cursor.lastrowid else True))
    cursor.close()
    return results

def get_address_string(address_id):
    addr = execute_query("SELECT civic, apartment, street, city, postal_code FROM AddressLine WHERE address_id = %s", (address_id,))
    if addr:
        apt = f" apt {addr[0][1]}" if addr[0][1] else ""
        return f"{addr[0][0]}{apt} {addr[0][2]}, {addr[0][3]}, {addr[0][4]}"
    return None

def get_dummy_carrier_id():
    # Try to find an existing dummy carrier.
    dummy = execute_query("SELECT user_id FROM UserAccount WHERE email = %s", ("unassigned@boozy.com",))
    if dummy:
        return dummy[0][0]
    # If not found, create one.
    dummy_id = execute_query(
        "INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, user_type, total_earnings) VALUES (%s, %s, %s, %s, %s, %s, %s)",
        ("unassigned@boozy.com", "dummy", "Dummy", "Unassigned", "0000000000", "carrier", 0.0),
        fetch=False
    )
    return dummy_id

def changeImage(image_data, item_id, item_type):
    """
    Handles saving image for products or shops
    
    Parameters:
    - image_data: Base64 encoded image data (without the prefix)
    - item_id: ID of the product or shop
    - item_type: Either 'product' or 'shop'
    
    Returns:
    - Tuple (success, message)
    """
    # Validate parameters
    if not image_data:
        return False, "No image data provided"
    
    if not item_id:
        return False, "No item ID provided"
    
    if item_type not in ['product', 'shop']:
        return False, "Invalid item type. Must be 'product' or 'shop'"
    
    # Validate base64 format
    try:
        # Check if the data starts with base64 prefix, remove if present
        if "base64," in image_data:
            image_data = image_data.split("base64,")[1]
        
        # Decode base64 string
        image_bytes = base64.b64decode(image_data)
        
        # Check if it's a PNG by looking at the magic bytes
        if not image_bytes.startswith(b'\x89PNG\r\n\x1a\n'):
            return False, "Image must be in PNG format"
        
        # Determine the target directory based on item type
        if item_type == 'product':
            target_dir = os.path.join(app.root_path, 'static', 'productImages')
        else:  # shop
            target_dir = os.path.join(app.root_path, 'static', 'shopImages')
        
        # Ensure target directory exists
        os.makedirs(target_dir, exist_ok=True)
        
        # Create the file path with the correct filename format
        file_path = os.path.join(target_dir, f"{item_id}.png")
        
        # Save the image
        with open(file_path, 'wb') as image_file:
            image_file.write(image_bytes)
        
        return True, "Image saved successfully"
    
    except Exception as e:
        return False, f"Error processing image: {str(e)}"

######## Public Routes ########
@app.route('/')
def home():
    return jsonify({"message": "Welcome to the Boozy API!!!"})

@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'),
                               'favicon.ico', mimetype='image/vnd.microsoft.icon')

@app.route('/getImages/product/<product_id>', methods=['GET'])
def get_product_image(product_id):
    prod_folder = os.path.join(app.root_path, 'static', 'productImages')
    filename = f'{product_id}.png'
    if os.path.exists(os.path.join(prod_folder, filename)):
        return send_from_directory(prod_folder, filename)
    return send_from_directory(os.path.join(app.root_path, 'static'), 'placeholder.jpg')

@app.route('/getImages/shop/<shop_id>', methods=['GET'])
def get_shop_image(shop_id):
    shop_folder = os.path.join(app.root_path, 'static', 'shopImages')
    filename = f'{shop_id}.png'
    if os.path.exists(os.path.join(shop_folder, filename)):
        return send_from_directory(shop_folder, filename)
    return send_from_directory(os.path.join(app.root_path, 'static'), 'placeholder.jpg')


@app.route('/getShops', methods=['GET'])
def get_shops():
    shop_id = request.args.get('shop_id')
    if shop_id:
        q = ("SELECT s.shop_id, s.name, a.civic, a.apartment, a.street, a.city, a.postal_code "
             "FROM Shop s JOIN AddressLine a ON s.address_id = a.address_id WHERE s.shop_id = %s")
        shops = execute_query(q, (shop_id,))
    else:
        q = ("SELECT s.shop_id, s.name, a.civic, a.apartment, a.street, a.city, a.postal_code "
             "FROM Shop s JOIN AddressLine a ON s.address_id = a.address_id")
        shops = execute_query(q)
    res = []
    for s in shops:
        apt = f" apt {s[3]}" if s[3] else ""
        res.append({
            "shop_id": s[0],
            "name": s[1],
            "address": f"{s[2]}{apt} {s[4]}, {s[5]}, {s[6]}",
            "address_components": {"civic": s[2], "apartment": s[3], "street": s[4], "city": s[5], "postal_code": s[6]}
        })
    return jsonify(res)

@app.route('/getProducts', methods=['GET'])
def get_products():
    product_id = request.args.get('product_id')
    category = request.args.get('category')
    q_parts = ["SELECT * FROM Product"]
    params = []
    if product_id:
        q_parts.append("WHERE product_id = %s")
        params.append(product_id)
    elif category:
        q_parts.append("WHERE category = %s")
        params.append(category)
    q = " ".join(q_parts)
    products = execute_query(q, tuple(params) if params else None)
    res = []
    for p in products:
        res.append({
            "product_id": p[0],
            "name": p[1],
            "description": p[2],
            "price": float(p[3]),
            "category": p[4],
            "alcohol": float(p[5])
        })
    return jsonify(res)

@app.route('/getAvailability', methods=['GET'])
def get_availability():
    shop_id = request.args.get('shop_id')
    product_id = request.args.get('product_id')
    in_stock = request.args.get('in_stock')
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
            in_stock_val = int(in_stock)
            conditions.append("sp.quantity >= %s")
            params.append(in_stock_val)
        except:
            pass
    q = ("SELECT sp.shop_id, sp.product_id, sp.quantity, p.name, p.price, p.category, p.alcohol, s.name, "
         "a.civic, a.apartment, a.street, a.city, a.postal_code FROM ShopProduct sp "
         "JOIN Product p ON sp.product_id = p.product_id JOIN Shop s ON sp.shop_id = s.shop_id "
         "JOIN AddressLine a ON s.address_id = a.address_id")
    if conditions:
        q += " WHERE " + " AND ".join(conditions)
    inv = execute_query(q, tuple(params) if params else None)
    res = []
    for i in inv:
        apt = f" apt {i[9]}" if i[9] else ""
        res.append({
            "shop_id": i[0],
            "product_id": i[1],
            "quantity": i[2],
            "product": {"name": i[3], "price": float(i[4]), "category": i[5], "alcohol": float(i[6])},
            "shop": {"name": i[7], "address": f"{i[8]}{apt} {i[10]}, {i[11]}, {i[12]}"}
        })
    return jsonify(res)

@app.route('/getOrders', methods=['GET'])
def get_orders():
    # Only show orders that are "Searching" (not yet claimed).
    orders = execute_query(
        "SELECT co.client_order_id, co.total_amount, co.shop_id, s.name as shop_name, "
        "a.civic, a.apartment, a.street, a.city, a.postal_code "
        "FROM ClientOrder co "
        "JOIN Shop s ON co.shop_id = s.shop_id "
        "JOIN AddressLine a ON s.address_id = a.address_id "
        "WHERE co.status = 'Searching'"
    )
    
    result = []
    for order in orders:
        apt = f" apt {order[5]}" if order[5] else ""
        result.append({
            "order_id": order[0],
            "total_amount": float(order[1]),
            "shop_id": order[2],
            "shop_name": order[3],
            "shop_address": f"{order[4]}{apt} {order[6]}, {order[7]}, {order[8]}"
        })
    
    return jsonify(result)

@app.route('/getUserOrders', methods=['POST'])
def get_user_order():
    data = request.get_json()
    # Validate required fields
    required_fields = ["email", "password"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400

    email = data["email"]
    password = data["password"]
    
    # Verify user credentials and get user role
    user_query = """
    SELECT user_id, user_type 
    FROM UserAccount 
    WHERE email = %s AND password = %s
    """
    user_result = execute_query(user_query, (email, password))
    
    if not user_result:
        return jsonify({"status": "error", "message": "Invalid credentials"}), 401

    user_id = user_result[0][0]
    user_role = user_result[0][1]
    
    # Different queries based on user role
    if user_role == 'client':
        # Client query: Get all orders for this client including address information
        query = """
        SELECT co.client_order_id, co.total_amount, co.status, co.shop_id, 
               ua.first_name AS carrier_first_name, ua.last_name AS carrier_last_name, 
               co.creation_date,
               al.address_id, al.civic, al.apartment, al.street, al.city, al.postal_code,
               s.name AS shop_name, 
               sal.address_id AS shop_address_id, sal.civic AS shop_civic, 
               sal.apartment AS shop_apartment, sal.street AS shop_street, 
               sal.city AS shop_city, sal.postal_code AS shop_postal_code
        FROM ClientOrder co
        JOIN UserAccount ua ON co.carrier_id = ua.user_id
        JOIN AddressLine al ON co.address_id = al.address_id
        JOIN Shop s ON co.shop_id = s.shop_id
        JOIN AddressLine sal ON s.address_id = sal.address_id
        WHERE co.client_id = %s
        ORDER BY co.creation_date DESC
        """
        orders = execute_query(query, (user_id,))
        
        if not orders:
            return jsonify({"status": "success", "message": "No orders found", "orders": []}), 200
        
        # Format the results for client view with address information
        formatted_orders = []
        for order in orders:
            formatted_orders.append({
                "order_id": order[0],
                "total_amount": float(order[1]),
                "status": order[2],
                "shop_id": order[3],
                "carrier_name": f"{order[4]} {order[5]}",
                "creation_date": order[6].strftime("%Y-%m-%d %H:%M:%S") if order[6] else None,
                "delivery_address": {
                    "address_id": order[7],
                    "civic": order[8],
                    "apartment": order[9],
                    "street": order[10],
                    "city": order[11],
                    "postal_code": order[12],
                    "full_address": f"{order[8]} {order[10]}{', '+order[9] if order[9] else ''}, {order[11]}, {order[12]}"
                },
                "shop": {
                    "name": order[13],
                    "address": {
                        "address_id": order[14],
                        "civic": order[15],
                        "apartment": order[16],
                        "street": order[17],
                        "city": order[18],
                        "postal_code": order[19],
                        "full_address": f"{order[15]} {order[17]}{', '+order[16] if order[16] else ''}, {order[18]}, {order[19]}"
                    }
                }
            })
        
    elif user_role == 'carrier':
        # Carrier query: Get all deliveries carried by this carrier with address information
        query = """
        SELECT co.client_order_id, co.total_amount, co.status, co.shop_id,
               ua.first_name AS client_first_name, ua.last_name AS client_last_name,
               al.address_id, al.civic, al.apartment, al.street, al.city, al.postal_code,
               s.name AS shop_name, 
               sal.address_id AS shop_address_id, sal.civic AS shop_civic, 
               sal.apartment AS shop_apartment, sal.street AS shop_street, 
               sal.city AS shop_city, sal.postal_code AS shop_postal_code,
               co.creation_date
        FROM ClientOrder co
        JOIN UserAccount ua ON co.client_id = ua.user_id
        JOIN AddressLine al ON co.address_id = al.address_id
        JOIN Shop s ON co.shop_id = s.shop_id
        JOIN AddressLine sal ON s.address_id = sal.address_id
        WHERE co.carrier_id = %s
        ORDER BY co.creation_date DESC
        """
        orders = execute_query(query, (user_id,))
        
        if not orders:
            return jsonify({"status": "success", "message": "No deliveries found", "orders": []}), 200
        
        # Format the results for carrier view with address information
        formatted_orders = []
        for order in orders:
            formatted_orders.append({
                "order_id": order[0],
                "total_amount": float(order[1]),
                "status": order[2],
                "shop_id": order[3],
                "client_name": f"{order[4]} {order[5]}",
                "delivery_address": {
                    "address_id": order[6],
                    "civic": order[7],
                    "apartment": order[8],
                    "street": order[9],
                    "city": order[10],
                    "postal_code": order[11],
                    "full_address": f"{order[7]} {order[9]}{', '+order[8] if order[8] else ''}, {order[10]}, {order[11]}"
                },
                "shop": {
                    "name": order[12],
                    "address": {
                        "address_id": order[13],
                        "civic": order[14],
                        "apartment": order[15],
                        "street": order[16],
                        "city": order[17],
                        "postal_code": order[18],
                        "full_address": f"{order[14]} {order[16]}{', '+order[15] if order[15] else ''}, {order[17]}, {order[18]}"
                    }
                },
                "creation_date": order[19].strftime("%Y-%m-%d %H:%M:%S") if order[19] else None
            })
    else:
        # Not a client or carrier
        return jsonify({"status": "error", "message": "Unauthorized access. Only clients and carriers can view orders."}), 403
    
    # Add products for each order
    for order in formatted_orders:
        products_query = """
        SELECT p.product_id, p.name, p.price, cop.quantity
        FROM ClientOrderProduct cop
        JOIN Product p ON cop.product_id = p.product_id
        WHERE cop.client_order_id = %s
        """
        products = execute_query(products_query, (order["order_id"],))
        
        order_products = []
        for product in products:
            order_products.append({
                "product_id": product[0],
                "name": product[1],
                "price": float(product[2]),
                "quantity": product[3],
                "subtotal": float(product[2]) * product[3]
            })
        
        order["products"] = order_products
    
    return jsonify({
        "status": "success", 
        "user_role": user_role,
        "orders": formatted_orders
    }), 200

######## User Routes ########
@app.route('/createUser', methods=['POST'])
def create_user():
    data = request.get_json()
    req = ["email", "password", "last_name", "first_name", "phone_number", "user_type"]
    for r in req:
        if r not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {r}"}), 400
    email = data["email"]
    password = data["password"]
    last_name = data["last_name"]
    first_name = data["first_name"]
    phone_number = data["phone_number"]
    user_type = data["user_type"]
    if user_type not in ["client", "carrier"]:
        return jsonify({"status": "error", "message": "Invalid user_type, must be 'client' or 'carrier'"}), 400
    license_plate = None
    car_brand = None
    if user_type == "carrier":
        if "license_plate" not in data:
            return jsonify({"status": "error", "message": "Carrier requires license_plate"}), 400
        license_plate = data["license_plate"]
        car_brand = data.get("car_brand")
    address_id = None
    # Allow address only if provided during user creation but do not return it.
    if "address" in data and isinstance(data["address"], dict):
        addr = data["address"]
        new_addr_id = execute_query(
            "INSERT INTO AddressLine (civic, apartment, street, city, postal_code) VALUES (%s, %s, %s, %s, %s)",
            (addr.get("civic"), addr.get("apartment"), addr.get("street"), addr.get("city"), addr.get("postal_code")),
            fetch=False
        )
        address_id = new_addr_id
    elif all(key in data for key in ["civic", "street", "city"]):
        new_addr_id = execute_query(
            "INSERT INTO AddressLine (civic, apartment, street, city, postal_code) VALUES (%s, %s, %s, %s, %s)",
            (data.get("civic"), data.get("apartment"), data.get("street"), data.get("city"), data.get("postal_code", "")),
            fetch=False
        )
        address_id = new_addr_id
    execute_query(
        "INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, car_brand, total_earnings) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
        (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, car_brand, 0.0),
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
    user = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s", (email, password))
    if user:
        addr = None
        if user[0][6]:
            ad = execute_query("SELECT * FROM AddressLine WHERE address_id = %s", (user[0][6],))
            if ad:
                addr = {"civic": ad[0][1], "apartment": ad[0][2], "street": ad[0][3], "city": ad[0][4], "postal_code": ad[0][5]}
        user_data = {"email": user[0][1], "last_name": user[0][3], "first_name": user[0][4],
                     "phone_number": user[0][5], "user_type": user[0][7], "license_plate": user[0][8] if user[0][8] else None,
                     "car_brand": user[0][9] if user[0][9] else None, "total_earnings": float(user[0][10]) if user[0][10] else 0.0,
                     "address": addr}
        return jsonify({"status": "success", "user": user_data})
    return jsonify({"status": "error", "message": "Invalid credentials"}), 401


@app.route('/createOrder', methods=['POST'])
def create_order():
    data = request.get_json()
    req = ["email", "password", "shop_id", "items", "payment_method", "card_name", "card_number", "CVC_card", "expiry_date_month", "expiry_date_year"]
    for r in req:
        if r not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {r}"}), 400

    user = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s", (data["email"], data["password"]))
    if not user:
        return jsonify({"status": "error", "message": "Invalid credentials"}), 401

    # Use only the user's attached address; ignore any address provided in the request.
    if not user[0][6]:
        return jsonify({"status": "error", "message": "Delivery address is required"}), 400
    address_id = user[0][6]

    shop_id = data["shop_id"]
    items = data["items"]
    payment_method = data["payment_method"]

    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404

    subtotal = 0
    for item in items:
        prod_id = item["product_id"]
        qty = item["quantity"]
        prod = execute_query("SELECT price FROM Product WHERE product_id = %s", (prod_id,))
        if not prod:
            return jsonify({"status": "error", "message": f"Product {prod_id} not found"}), 404
        inv = execute_query("SELECT quantity FROM ShopProduct WHERE shop_id = %s AND product_id = %s", (shop_id, prod_id))
        if not inv or inv[0][0] < qty:
            return jsonify({"status": "error", "message": f"Product {prod_id} not available in requested quantity"}), 400
        subtotal += float(prod[0][0]) * qty

    # Calculate the total with 15% tip (assuming subtotal already includes tax)
    total_amount = subtotal
    tip_amount = total_amount * 0.15
    final_total = total_amount + tip_amount


    creation_date = datetime.now().strftime('%Y-%m-%d %H:%M:%S')
    # Get a valid dummy carrier id.
    dummy_carrier_id = get_dummy_carrier_id()
    order_id = execute_query(
        "INSERT INTO ClientOrder (creation_date, status, total_amount, tip_amount, address_id, shop_id, client_id, carrier_id) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
        (creation_date, "Searching", final_total, tip_amount, address_id, shop_id, user[0][0], dummy_carrier_id),
        fetch=False
    )

    for item in items:
        prod_id = item["product_id"]
        qty = item["quantity"]
        execute_query(
            "INSERT INTO ClientOrderProduct (client_order_id, product_id, quantity) VALUES (%s, %s, %s)",
            (order_id, prod_id, qty), 
            fetch=False
        )
        execute_query(
            "UPDATE ShopProduct SET quantity = quantity - %s WHERE shop_id = %s AND product_id = %s",
            (qty, shop_id, prod_id), 
            fetch=False
        )

    execute_query(
        "INSERT INTO Payment (payment_method, amount, payment_date, is_completed, client_order_id, user_id, card_name, card_number, CVC_card, expiry_date_month, expiry_date_year) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
        (payment_method, final_total, creation_date, True, order_id, user[0][0], data["card_name"], data["card_number"], data["CVC_card"], data["expiry_date_month"], data["expiry_date_year"]),
        fetch=False
    )

    return jsonify({"status": "success"})


@app.route('/modifyUser', methods=['POST'])
def modify_user():
    data = request.get_json()
    if "email" not in data or "password" not in data:
        return jsonify({"status": "error", "message": "Email and password are required"}), 400

    user = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s", (data["email"], data["password"]))
    if not user:
        return jsonify({"status": "error", "message": "Invalid credentials"}), 401

    user_id = user[0][0]
    user_type = user[0][7]

    updates = []
    params = []

    if "new_email" in data:
        updates.append("email = %s")
        params.append(data["new_email"])

    if "new_password" in data:
        updates.append("password = %s")
        params.append(data["new_password"])

    if "last_name" in data:
        updates.append("last_name = %s")
        params.append(data["last_name"])

    if "first_name" in data:
        updates.append("first_name = %s")
        params.append(data["first_name"])

    if "phone_number" in data:
        updates.append("phone_number = %s")
        params.append(data["phone_number"])

    if user_type == "carrier":
        if "license_plate" in data:
            updates.append("license_plate = %s")
            params.append(data["license_plate"])
        if "car_brand" in data:
            updates.append("car_brand = %s")
            params.append(data["car_brand"])

    # Handle address update: update only fields provided in the request.
    address_id = user[0][6]
    if any(key in data for key in ["civic", "apartment", "street", "city", "postal_code"]):
        if address_id:
            addr_updates = []
            addr_params = []
            if "civic" in data:
                addr_updates.append("civic = %s")
                addr_params.append(data["civic"])
            if "apartment" in data:
                addr_updates.append("apartment = %s")
                addr_params.append(data["apartment"])
            if "street" in data:
                addr_updates.append("street = %s")
                addr_params.append(data["street"])
            if "city" in data:
                addr_updates.append("city = %s")
                addr_params.append(data["city"])
            if "postal_code" in data:
                addr_updates.append("postal_code = %s")
                addr_params.append(data["postal_code"])
            if addr_updates:
                addr_params.append(address_id)
                execute_query(f"UPDATE AddressLine SET {', '.join(addr_updates)} WHERE address_id = %s", tuple(addr_params), fetch=False)
        else:
            if all(key in data for key in ["civic", "street", "city"]):
                new_addr_id = execute_query(
                    "INSERT INTO AddressLine (civic, apartment, street, city, postal_code) VALUES (%s, %s, %s, %s, %s)",
                    (data.get("civic"), data.get("apartment"), data.get("street"), data.get("city"), data.get("postal_code")),
                    fetch=False
                )
                updates.append("address_id = %s")
                params.append(new_addr_id)

    if updates:
        params.append(user_id)
        execute_query(f"UPDATE UserAccount SET {', '.join(updates)} WHERE user_id = %s", tuple(params), fetch=False)
        return jsonify({"status": "success", "message": "User updated successfully"})

    return jsonify({"status": "error", "message": "No fields to update"}), 400

@app.route('/cancelOrder', methods=['POST'])
def cancel_order():
    data = request.get_json()
    req = ["email", "password", "order_id"]
    for r in req:
        if r not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {r}"}), 400

    # Get user information including user_type
    user = execute_query("SELECT user_id, user_type FROM UserAccount WHERE email = %s AND password = %s", 
                         (data["email"], data["password"]))
    if not user:
        return jsonify({"status": "error", "message": "Invalid credentials"}), 401

    user_id = user[0][0]
    user_type = user[0][1]
    order_id = data["order_id"]
    
    # Get order with client_id and carrier_id
    order = execute_query("SELECT client_order_id, status, shop_id, client_id, carrier_id FROM ClientOrder WHERE client_order_id = %s", 
                         (order_id,))
    if not order:
        return jsonify({"status": "error", "message": "Order not found"}), 404

    order_status = order[0][1]
    client_id = order[0][3]
    carrier_id = order[0][4]
    shop_id = order[0][2]
    
    # Check if order is already cancelled
    if order_status == "Cancelled":
        return jsonify({"status": "error", "message": "Order is already cancelled"}), 400
    
    # Check if order is completed
    if order_status == "Completed":
        return jsonify({"status": "error", "message": "Cannot cancel a completed order"}), 400
    
    # Handle cancellation based on user role
    if user_type == 'client':
        # Client cancellation - verify ownership
        if client_id != user_id:
            return jsonify({"status": "error", "message": "You are not authorized to cancel this order"}), 403
        
        # Client can cancel: Return products to inventory and set status to Cancelled
        items = execute_query("SELECT product_id, quantity FROM ClientOrderProduct WHERE client_order_id = %s", (order_id,))
        for item in items:
            prod_id = item[0]
            qty = item[1]
            execute_query("UPDATE ShopProduct SET quantity = quantity + %s WHERE shop_id = %s AND product_id = %s", 
                         (qty, shop_id, prod_id), fetch=False)
        
        execute_query("UPDATE ClientOrder SET status = 'Cancelled' WHERE client_order_id = %s", (order_id,), fetch=False)
        return jsonify({"status": "success", "message": "Order cancelled successfully"})
        
    elif user_type == 'carrier':
        # Carrier cancellation - verify assignment
        if carrier_id != user_id:
            return jsonify({"status": "error", "message": "You are not the assigned carrier for this order"}), 403
        
        # Carrier can only unbind themselves if order is InRoute or Shipping
        if order_status not in ["InRoute", "Shipping"]:
            return jsonify({"status": "error", "message": f"Cannot cancel order in {order_status} status as carrier"}), 400
        
        # Unbind carrier and set status back to Searching
        execute_query("""
            UPDATE ClientOrder 
            SET status = 'Searching', carrier_id = NULL 
            WHERE client_order_id = %s
            """, (order_id,), fetch=False)
            
        return jsonify({
            "status": "success", 
            "message": "You have been unassigned from this delivery. Order status is now 'Searching' for a new carrier."
        })
        
    else:
        # Admin or other role
        return jsonify({"status": "error", "message": "You are not authorized to cancel this order"}), 403

######## CARRIER ROUTES ########
@app.route('/takeOrder', methods=['POST'])
def take_order():
    data = request.get_json()
    if "email" not in data or "password" not in data:
        return jsonify({"status": "error", "message": "Email and password are required"}), 400
    if "order_id" not in data:
        return jsonify({"status": "error", "message": "Order ID is required"}), 400
    carrier = execute_query(
        "SELECT user_id FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'carrier'",
        (data["email"], data["password"])
    )
    if not carrier:
        return jsonify({"status": "error", "message": "Invalid credentials or not a carrier"}), 401
    carrier_id = carrier[0][0]
    # Ensure the carrier has no active order.
    active_orders = execute_query(
        "SELECT client_order_id FROM ClientOrder WHERE carrier_id = %s AND status IN ('Searching', 'InRoute')",
        (carrier_id,)
    )
    if active_orders:
        return jsonify({"status": "error", "message": "Carrier is already assigned to an active order"}), 400
    order_id = data["order_id"]
    order = execute_query(
        "SELECT client_order_id, status FROM ClientOrder WHERE client_order_id = %s",
        (order_id,)
    )
    if not order:
        return jsonify({"status": "error", "message": "Order not found"}), 404
    if order[0][1] != "Searching":
        return jsonify({"status": "error", "message": "Order is not available for pickup"}), 400
    # Automatically update order to InRoute
    execute_query(
        "UPDATE ClientOrder SET carrier_id = %s, status = 'InRoute' WHERE client_order_id = %s",
        (carrier_id, order_id), fetch=False
    )
    # Return updated order info (same format as getOrders)
    updated_order = execute_query(
        "SELECT co.client_order_id, co.total_amount, co.shop_id, s.name, CONCAT(a.civic, IF(a.apartment!='', CONCAT(' apt ', a.apartment), ''), ' ', a.street, ', ', a.city, ', ', a.postal_code) as shop_address FROM ClientOrder co JOIN Shop s ON co.shop_id = s.shop_id JOIN AddressLine a ON s.address_id = a.address_id WHERE co.client_order_id = %s",
        (order_id,)
    )
    if updated_order:
        order_info = {
            "order_id": updated_order[0][0],
            "total_amount": float(updated_order[0][1]),
            "shop_id": updated_order[0][2],
            "shop_name": updated_order[0][3],
            "shop_address": updated_order[0][4]
        }
        return jsonify({"status": "success", "order": order_info})
    else:
        return jsonify({"status": "error", "message": "Order update failed"}), 500

@app.route('/updateOrder', methods=['POST'])
def update_order():
    data = request.get_json()
    # Check required fields.
    if "email" not in data or "password" not in data:
        return jsonify({"status": "error", "message": "Email and password are required"}), 400
    if "status" not in data:
        return jsonify({"status": "error", "message": "New status is required"}), 400

    new_status = data["status"]
    # Allowed statuses for forward progression: only "Shipping" and "Completed" (plus "Cancelled" at any time).
    allowed_statuses = ["Shipping", "Completed"]
    if new_status not in allowed_statuses and new_status != "Cancelled":
        return jsonify({"status": "error", "message": "Invalid status. Must be one of: Shipping, Completed or Cancelled"}), 400

    carrier = execute_query(
        "SELECT user_id FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'carrier'",
        (data["email"], data["password"])
    )
    if not carrier:
        return jsonify({"status": "error", "message": "Invalid credentials or not a carrier"}), 401
    carrier_id = carrier[0][0]

    # Retrieve the active order (any order not yet Completed)
    active_order = execute_query(
        "SELECT client_order_id, status, client_id FROM ClientOrder WHERE carrier_id = %s AND status != 'Completed'",
        (carrier_id,)
    )
    if not active_order:
        return jsonify({"status": "error", "message": "No active order found"}), 404
    order_id, current_status, client_id = active_order[0][0], active_order[0][1], active_order[0][2]

    # Validate allowed transitions.
    if new_status != "Cancelled":
        valid_transition = False
        if current_status == "InRoute" and new_status == "Shipping":
            valid_transition = True
        elif current_status == "Shipping" and new_status == "Completed":
            valid_transition = True

        if not valid_transition:
            return jsonify({"status": "error", "message": f"Invalid status transition from {current_status} to {new_status}"}), 400

    # Update the order status.
    execute_query(
        "UPDATE ClientOrder SET status = %s WHERE client_order_id = %s",
        (new_status, order_id), fetch=False
    )

    # If new status is Shipping, return client information.
    if new_status == "Shipping":
        client = execute_query("SELECT first_name, last_name, phone_number, address_id FROM UserAccount WHERE user_id = %s", (client_id,))
        if client:
            client_info = {"first_name": client[0][0], "last_name": client[0][1], "phone_number": client[0][2]}
            addr_id = client[0][3]
            if addr_id:
                addr = execute_query("SELECT civic, apartment, street, city, postal_code FROM AddressLine WHERE address_id = %s", (addr_id,))
                if addr:
                    apt = f" apt {addr[0][1]}" if addr[0][1] else ""
                    client_info["address"] = f"{addr[0][0]}{apt} {addr[0][2]}, {addr[0][3]}, {addr[0][4]}"
            return jsonify({"status": "success", "client_info": client_info})
    # If new status is Completed, update carrier's earnings.
    if new_status == "Completed":
        order = execute_query("SELECT tip_amount FROM ClientOrder WHERE client_order_id = %s", (order_id,))
        if order:
            delivery_tip = float(order[0][0])
            execute_query(
                "UPDATE UserAccount SET total_earnings = total_earnings + %s WHERE user_id = %s",
                (delivery_tip, carrier_id), fetch=False
            )

    return jsonify({"status": "success", "message": f"Order status updated to {new_status}"})

######## Admin Routes ########
@app.route('/admin/createUser', methods=['POST'])
def admin_create_user():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    req = ["email", "password", "last_name", "first_name", "phone_number", "user_type"]
    for r in req:
        if r not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {r}"}), 400
    email = data["email"]
    password = data["password"]
    last_name = data["last_name"]
    first_name = data["first_name"]
    phone_number = data["phone_number"]
    user_type = data["user_type"]
    if user_type not in ["admin", "client", "carrier"]:
        return jsonify({"status": "error", "message": f"Invalid user_type: {user_type}"}), 400
    license_plate = None
    car_brand = None
    if user_type == "carrier":
        if "license_plate" not in data:
            return jsonify({"status": "error", "message": "Carrier requires license_plate"}), 400
        license_plate = data["license_plate"]
        car_brand = data.get("car_brand")
    addr_id = None
    if "address" in data:
        addr = data["address"]
        addr_id = execute_query("INSERT INTO AddressLine (civic, apartment, street, city, postal_code) VALUES (%s, %s, %s, %s, %s)",
                                (addr.get("civic"), addr.get("apartment"), addr.get("street"), addr.get("city"), addr.get("postal_code")), fetch=False)
    execute_query("INSERT INTO UserAccount (email, password, last_name, first_name, phone_number, address_id, user_type, license_plate, car_brand, total_earnings) VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s)",
                  (email, password, last_name, first_name, phone_number, addr_id, user_type, license_plate, car_brand, 0.0), fetch=False)
    return jsonify({"status": "success"})

@app.route('/admin/modifyUser', methods=['POST'])
def admin_modify_user():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "user_id" not in data:
        return jsonify({"status": "error", "message": "User ID is required"}), 400
    user_id = data["user_id"]
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    updates = []
    params = []
    mapping = {"email": "email", "password": "password", "last_name": "last_name", "first_name": "first_name", "phone_number": "phone_number", "user_type": "user_type", "license_plate": "license_plate", "car_brand": "car_brand", "total_earnings": "total_earnings"}
    for key, db_field in mapping.items():
        if key in data:
            updates.append(f"{db_field} = %s")
            params.append(data[key])
    if "address" in data and data["address"]:
        addr = data["address"]
        current_addr = user[0][6]
        if current_addr:
            addr_updates = []
            addr_params = []
            addr_map = {"civic": "civic", "apartment": "apartment", "street": "street", "city": "city", "postal_code": "postal_code"}
            for key, db_field in addr_map.items():
                if key in addr:
                    addr_updates.append(f"{db_field} = %s")
                    addr_params.append(addr[key])
            if addr_updates:
                addr_params.append(current_addr)
                execute_query(f"UPDATE AddressLine SET {', '.join(addr_updates)} WHERE address_id = %s", tuple(addr_params), fetch=False)
        else:
            new_addr_id = execute_query("INSERT INTO AddressLine (civic, apartment, street, city, postal_code) VALUES (%s, %s, %s, %s, %s)",
                                        (addr.get("civic"), addr.get("apartment"), addr.get("street"), addr.get("city"), addr.get("postal_code", "")), fetch=False)
            updates.append("address_id = %s")
            params.append(new_addr_id)
    if updates:
        params.append(user_id)
        execute_query(f"UPDATE UserAccount SET {', '.join(updates)} WHERE user_id = %s", tuple(params), fetch=False)
        return jsonify({"status": "success", "message": "User updated successfully"})
    return jsonify({"status": "error", "message": "No fields to update"}), 400

@app.route('/admin/deleteUser', methods=['POST'])
def admin_delete_user():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "user_id" not in data:
        return jsonify({"status": "error", "message": "User ID is required"}), 400
    user_id = data["user_id"]
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    try:
        execute_query("DELETE FROM UserAccount WHERE user_id = %s", (user_id,), fetch=False)
        return jsonify({"status": "success", "message": "User deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/admin/createShop', methods=['POST'])
def admin_create_shop():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    req = ["name", "civic", "street", "city"]
    for r in req:
        if r not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {r}"}), 400
    new_addr_id = execute_query("INSERT INTO AddressLine (civic, apartment, street, city, postal_code) VALUES (%s, %s, %s, %s, %s)",
                                (data["civic"], data.get("apartment"), data["street"], data["city"], data.get("postal_code", "")), fetch=False)
    new_shop_id = execute_query("INSERT INTO Shop (name, address_id) VALUES (%s, %s)",
                                (data["name"], new_addr_id), fetch=False)
    
    # Handle image upload if provided
    if "image" in data and data["image"]:
        success, message = changeImage(data["image"], new_shop_id, "shop")
        if not success:
            # Continue with shop creation even if image upload fails
            return jsonify({"status": "success", "shop_id": new_shop_id, "image_warning": message})
    
    return jsonify({"status": "success", "shop_id": new_shop_id})

@app.route('/admin/modifyShop', methods=['POST'])
def admin_modify_shop():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "shop_id" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    shop_id = data["shop_id"]
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    shop_updates = []
    shop_params = []
    if "name" in data:
        shop_updates.append("name = %s")
        shop_params.append(data["name"])
    if shop_updates:
        shop_params.append(shop_id)
        execute_query(f"UPDATE Shop SET {', '.join(shop_updates)} WHERE shop_id = %s", tuple(shop_params), fetch=False)
    addr_id = shop[0][2]
    addr_updates = []
    addr_params = []
    for key, db_field in {"civic": "civic", "apartment": "apartment", "street": "street", "city": "city", "postal_code": "postal_code"}.items():
        if key in data:
            addr_updates.append(f"{db_field} = %s")
            addr_params.append(data[key])
    if addr_updates:
        addr_params.append(addr_id)
        execute_query(f"UPDATE AddressLine SET {', '.join(addr_updates)} WHERE address_id = %s", tuple(addr_params), fetch=False)
    
    # Handle image update if provided
    image_message = None
    if "image" in data and data["image"]:
        success, message = changeImage(data["image"], shop_id, "shop")
        if not success:
            image_message = message
    
    response = {"status": "success", "message": "Shop updated successfully"}
    if image_message:
        response["image_warning"] = image_message
    
    return jsonify(response)

@app.route('/admin/deleteShop', methods=['POST'])
def admin_delete_shop():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "shop_id" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    shop_id = data["shop_id"]
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    try:
        execute_query("DELETE FROM Shop WHERE shop_id = %s", (shop_id,), fetch=False)
        return jsonify({"status": "success", "message": "Shop deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/admin/createProduct', methods=['POST'])
def admin_create_product():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    req = ["name", "price", "category", "alcohol"]
    for r in req:
        if r not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {r}"}), 400
    description = data.get("description", "")
    new_prod_id = execute_query("INSERT INTO Product (name, description, price, category, alcohol) VALUES (%s, %s, %s, %s, %s)",
                                (data["name"], description, data["price"], data["category"], data["alcohol"]), fetch=False)
    
    # Handle image upload if provided
    if "image" in data and data["image"]:
        success, message = changeImage(data["image"], new_prod_id, "product")
        if not success:
            # Continue with product creation even if image upload fails
            return jsonify({"status": "success", "product_id": new_prod_id, "image_warning": message})
    
    return jsonify({"status": "success", "product_id": new_prod_id})

@app.route('/admin/modifyProduct', methods=['POST'])
def admin_modify_product():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "product_id" not in data:
        return jsonify({"status": "error", "message": "Product ID is required"}), 400
    prod_id = data["product_id"]
    prod = execute_query("SELECT * FROM Product WHERE product_id = %s", (prod_id,))
    if not prod:
        return jsonify({"status": "error", "message": "Product not found"}), 404
    updates = []
    params = []
    for key, db_field in {"name": "name", "description": "description", "price": "price", "category": "category", "alcohol": "alcohol"}.items():
        if key in data:
            updates.append(f"{db_field} = %s")
            params.append(data[key])
    if updates:
        params.append(prod_id)
        execute_query(f"UPDATE Product SET {', '.join(updates)} WHERE product_id = %s", tuple(params), fetch=False)
    
    # Handle image update if provided
    image_message = None
    if "image" in data and data["image"]:
        success, message = changeImage(data["image"], prod_id, "product")
        if not success:
            image_message = message
    
    response = {"status": "success", "message": "Product updated successfully"}
    if image_message:
        response["image_warning"] = image_message
    elif not updates and "image" not in data:
        response["message"] = "No fields to update"
    
    return jsonify(response)

@app.route('/admin/deleteProduct', methods=['POST'])
def admin_delete_product():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "product_id" not in data:
        return jsonify({"status": "error", "message": "Product ID is required"}), 400
    prod_id = data["product_id"]
    prod = execute_query("SELECT * FROM Product WHERE product_id = %s", (prod_id,))
    if not prod:
        return jsonify({"status": "error", "message": "Product not found"}), 404
    try:
        execute_query("DELETE FROM Product WHERE product_id = %s", (prod_id,), fetch=False)
        return jsonify({"status": "success", "message": "Product deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/admin/getUsers', methods=['POST'])
def admin_get_users():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    users = execute_query(
        "SELECT u.user_id, u.email, u.password, u.last_name, u.first_name, u.phone_number, u.address_id, u.user_type, u.license_plate, u.car_brand, u.total_earnings, a.civic, a.apartment, a.street, a.city, a.postal_code "
        "FROM UserAccount u LEFT JOIN AddressLine a ON u.address_id = a.address_id")
    res = []
    for u in users:
        user_obj = {"user_id": u[0], "email": u[1], "password": u[2], "last_name": u[3], "first_name": u[4],
                    "phone_number": u[5], "user_type": u[7], "license_plate": u[8] if u[8] else None,
                    "car_brand": u[9] if u[9] else None, "total_earnings": float(u[10]) if u[10] else 0.0}
        if u[6]:
            apt = f" apt {u[12]}" if u[12] else ""
            user_obj["address"] = {"civic": u[11], "apartment": u[12], "street": u[13], "city": u[14], "postal_code": u[15],
                                   "fullAddress": f"{u[11]}{apt} {u[13]}, {u[14]}, {u[15]}"}
        res.append(user_obj)
    return jsonify({"status": "success", "users": res})

@app.route('/admin/modifyAvailability', methods=['POST'])
def admin_modify_availability():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    if "shop_id" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    if "products" not in data or not isinstance(data["products"], list):
        return jsonify({"status": "error", "message": "Products list is required"}), 400
    shop_id = data["shop_id"]
    products = data["products"]
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    for prod in products:
        if "product_id" not in prod or "quantity" not in prod:
            return jsonify({"status": "error", "message": "Each product must have product_id and quantity"}), 400
        prod_id = prod["product_id"]
        qty = prod["quantity"]
        product = execute_query("SELECT * FROM Product WHERE product_id = %s", (prod_id,))
        if not product:
            return jsonify({"status": "error", "message": f"Product ID {prod_id} not found"}), 404
        sp = execute_query("SELECT * FROM ShopProduct WHERE shop_id = %s AND product_id = %s", (shop_id, prod_id))
        if sp:
            execute_query("UPDATE ShopProduct SET quantity = %s WHERE shop_id = %s AND product_id = %s", (qty, shop_id, prod_id), fetch=False)
        else:
            execute_query("INSERT INTO ShopProduct (shop_id, product_id, quantity) VALUES (%s, %s, %s)", (shop_id, prod_id, qty), fetch=False)
    return jsonify({"status": "success", "message": "Shop inventory updated successfully"})

@app.route('/admin/getAllOrders', methods=['POST'])
def admin_get_all_orders():
    data = request.get_json()
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                            (data["admin_email"], data["admin_password"]))
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Get all orders with client, carrier, and shop information
    orders_query = """
    SELECT co.client_order_id, co.creation_date, co.status, co.total_amount, co.tip_amount,
           cl.user_id AS client_id, cl.email AS client_email, cl.first_name AS client_first_name, 
           cl.last_name AS client_last_name, cl.phone_number AS client_phone,
           ca.user_id AS carrier_id, ca.email AS carrier_email, ca.first_name AS carrier_first_name, 
           ca.last_name AS carrier_last_name, ca.phone_number AS carrier_phone,
           ca.license_plate, ca.car_brand,
           s.shop_id, s.name AS shop_name,
           del_addr.address_id AS delivery_address_id, del_addr.civic AS delivery_civic, 
           del_addr.apartment AS delivery_apartment, del_addr.street AS delivery_street, 
           del_addr.city AS delivery_city, del_addr.postal_code AS delivery_postal_code,
           shop_addr.civic AS shop_civic, shop_addr.apartment AS shop_apartment, 
           shop_addr.street AS shop_street, shop_addr.city AS shop_city, 
           shop_addr.postal_code AS shop_postal_code
    FROM ClientOrder co
    JOIN UserAccount cl ON co.client_id = cl.user_id
    JOIN UserAccount ca ON co.carrier_id = ca.user_id
    JOIN Shop s ON co.shop_id = s.shop_id
    JOIN AddressLine del_addr ON co.address_id = del_addr.address_id
    JOIN AddressLine shop_addr ON s.address_id = shop_addr.address_id
    ORDER BY co.creation_date DESC
    """
    orders = execute_query(orders_query)
    
    result = []
    for order in orders:
        # Format delivery address
        delivery_apt = f" apt {order[21]}" if order[21] else ""
        delivery_address = f"{order[20]}{delivery_apt} {order[22]}, {order[23]}, {order[24]}"
        
        # Format shop address
        shop_apt = f" apt {order[26]}" if order[26] else ""
        shop_address = f"{order[25]}{shop_apt} {order[27]}, {order[28]}, {order[29]}"
        
        # Get payment information for this order
        payment_query = """
        SELECT payment_method, amount, payment_date, is_completed, card_name, card_number
        FROM Payment
        WHERE client_order_id = %s
        """
        payment = execute_query(payment_query, (order[0],))
        payment_info = None
        if payment:
            payment_info = {
                "payment_method": payment[0][0],
                "amount": float(payment[0][1]),
                "payment_date": payment[0][2].strftime("%Y-%m-%d %H:%M:%S") if payment[0][2] else None,
                "is_completed": bool(payment[0][3]),
                "card_name": payment[0][4],
                "card_number": payment[0][5]  # Note: In production, this should be masked
            }
        
        # Get order items
        items_query = """
        SELECT cop.product_id, p.name, p.price, p.category, p.alcohol, cop.quantity
        FROM ClientOrderProduct cop
        JOIN Product p ON cop.product_id = p.product_id
        WHERE cop.client_order_id = %s
        """
        items_result = execute_query(items_query, (order[0],))
        items = []
        for item in items_result:
            items.append({
                "product_id": item[0],
                "name": item[1],
                "price": float(item[2]),
                "category": item[3],
                "alcohol": float(item[4]),
                "quantity": item[5],
                "subtotal": float(item[2]) * item[5]
            })
        
        # Create the complete order object with all details
        order_obj = {
            "order_id": order[0],
            "creation_date": order[1].strftime("%Y-%m-%d %H:%M:%S") if order[1] else None,
            "status": order[2],
            "total_amount": float(order[3]),
            "tip_amount": float(order[4]),
            "client": {
                "user_id": order[5],
                "email": order[6],
                "first_name": order[7],
                "last_name": order[8],
                "phone_number": order[9]
            },
            "carrier": {
                "user_id": order[10],
                "email": order[11],
                "first_name": order[12],
                "last_name": order[13],
                "phone_number": order[14],
                "license_plate": order[15],
                "car_brand": order[16]
            },
            "shop": {
                "shop_id": order[17],
                "name": order[18],
                "address": shop_address
            },
            "delivery_address": delivery_address,
            "payment": payment_info,
            "items": items,
            "total_items": sum(item["quantity"] for item in items)
        }
        
        result.append(order_obj)
    
    return jsonify({
        "status": "success",
        "total_orders": len(result),
        "orders": result
    })

@app.route('/admin/cancelOrder', methods=['POST'])
def admin_cancel_order():
    data = request.get_json()
    
    # Verify admin credentials
    if "admin_email" not in data or "admin_password" not in data:
        return jsonify({"status": "error", "message": "Admin credentials required"}), 401
    
    admin = execute_query("SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
                         (data["admin_email"], data["admin_password"]))
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Check if order_id is provided
    if "order_id" not in data:
        return jsonify({"status": "error", "message": "Order ID is required"}), 400
    
    order_id = data["order_id"]
    
    # Verify the order exists
    order = execute_query("SELECT * FROM ClientOrder WHERE client_order_id = %s", (order_id,))
    if not order:
        return jsonify({"status": "error", "message": "Order not found"}), 404
    
    # Update the order status to Cancelled
    try:
        execute_query("UPDATE ClientOrder SET status = 'Cancelled' WHERE client_order_id = %s", (order_id,), fetch=False)
        return jsonify({
            "status": "success", 
            "message": "Order cancelled successfully",
            "order_id": order_id
        })
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

######## Admin Web Routes ########
ADMIN_FOLDER = os.path.abspath(os.path.join(app.root_path, '..', 'WEB_ADMIN'))

@app.route('/admin/web')
def admin_web_redirect():
    return redirect('/admin/web/')

@app.route('/admin/web/')
def admin_index():
    return send_from_directory(ADMIN_FOLDER, 'accueil.html')

@app.route('/admin/web/<path:filename>')
def admin_static(filename):
    return send_from_directory(ADMIN_FOLDER, filename)

if __name__ == '__main__':
    app.run(host="0.0.0.0")
