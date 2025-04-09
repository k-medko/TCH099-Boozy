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

# Fallback image for shops - using placeholder since Shop table doesn't have image fields
@app.route('/getImages/shop/<shop_id>', methods=['GET'])
def get_shop_image(shop_id):
    # Since Shop table doesn't have image fields in schema, serve default placeholder
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'placeholder.jpg')

@app.route('/getShops', methods=['GET'])
def get_shops():
    shop_id = request.args.get('shopId')
    proximity = request.args.get('proximity')
    
    if shop_id:
        query = """
            SELECT s.shop_id, s.name, a.civic, a.apartment,
                   a.street, a.postal_code, a.city
            FROM Shop s
            JOIN AddressLine a ON s.address_id = a.address_id
            WHERE s.shop_id = %s
        """
        shops = execute_query(query, (shop_id,))
    else:
        query = """
            SELECT s.shop_id, s.name, a.civic, a.apartment,
                   a.street, a.postal_code, a.city
            FROM Shop s
            JOIN AddressLine a ON s.address_id = a.address_id
        """
        shops = execute_query(query)
    
    formatted_shops = []
    for shop in shops:
        apt_str = f" apt {shop[3]}" if shop[3] else ""
        formatted_shops.append({
            "shop_id": shop[0],
            "name": shop[1],
            "address": f"{shop[2]}{apt_str} {shop[4]}, {shop[6]}, {shop[5]}",
            "address_components": {
                "civic": shop[2],
                "apartment": shop[3],
                "street": shop[4],
                "postal_code": shop[5],
                "city": shop[6]
            }
        })
    
    return jsonify(formatted_shops)

@app.route('/getProducts', methods=['GET'])
def get_products():
    product_id = request.args.get('productId')
    category = request.args.get('category')
    
    query_parts = ["SELECT * FROM Product"]
    params = []
    
    if product_id:
        query_parts.append("WHERE product_id = %s")
        params.append(product_id)
    elif category:
        query_parts.append("WHERE category = %s")
        params.append(category)
    
    query = " ".join(query_parts)
    products = execute_query(query, tuple(params) if params else None)
    
    formatted_products = []
    for product in products:
        formatted_products.append({
            "product_id": product[0],
            "name": product[1],
            "description": product[2],
            "price": float(product[3]),
            "category": product[4],
            "alcohol": float(product[5])
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
               p.name as product_name, p.price, p.category, p.alcohol,
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
                "alcohol": float(item[6])
            },
            "shop": {
                "name": item[7],
                "address": f"{item[8]} {item[9]}, {item[10]}, {item[11]}"
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
    
    # Map user_type to match database ENUM values
    user_type_mapping = {
        "customer": "client",
        "deliverer": "carrier",
        "admin": "admin"
    }
    
    if user_type not in user_type_mapping:
        return jsonify({"status": "error", "message": "Invalid userType"}), 400
    
    db_user_type = user_type_mapping[user_type]
    
    # Check if deliverer info is provided when needed
    license_plate = None
    car_brand = None
    if user_type == "deliverer":
        if "licensePlate" not in data:
            return jsonify({"status": "error", "message": "Deliverer requires licensePlate"}), 400
        license_plate = data["licensePlate"]
        car_brand = data.get("carBrand")  # Optional in new schema
    
    existing = execute_query("SELECT * FROM UserAccount WHERE email = %s", (email,))
    if existing:
        return jsonify({"status": "error", "message": "Email already exists"}), 400
    
    address_id = None
    # Handle address based on provided data formats
    if "address" in data and isinstance(data["address"], dict):
        # Handle address as dictionary
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
    elif all(key in data for key in ["civic", "street", "city"]):
        # Handle flat structure with address fields
        new_address_id = get_next_id("AddressLine", "address_id")
        
        execute_query(
            """INSERT INTO AddressLine 
               (address_id, civic, apartment, street, city, postal_code) 
               VALUES (%s, %s, %s, %s, %s, %s)""",
            (new_address_id, data.get("civic"), data.get("apartment"), 
             data.get("street"), data.get("city"), data.get("postalCode", "")),
            fetch=False
        )
        address_id = new_address_id
    
    new_user_id = get_next_id("UserAccount", "user_id")
    
    execute_query(
        """INSERT INTO UserAccount 
           (user_id, email, password, last_name, first_name, phone_number, address_id, user_type, 
            license_plate, car_brand, total_earnings) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (new_user_id, email, password, last_name, first_name, phone_number, address_id, db_user_type, 
         license_plate, car_brand, 0.0),
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
        # Map database user_type back to API user_type
        user_type_mapping = {
            "client": "customer",
            "carrier": "deliverer", 
            "admin": "admin"
        }
        
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
            "userType": user_type_mapping.get(user[0][7], user[0][7]),
            "licensePlate": user[0][8] if user[0][8] else None,
            "carBrand": user[0][9] if user[0][9] else None,
            "totalEarnings": float(user[0][10]) if user[0][10] else 0.0,
            "address": address
        }
        return jsonify({"status": "success", "user": user_data})
    
    return jsonify({"status": "error", "message": "Invalid credentials"}), 401

@app.route('/createOrder', methods=['POST'])
def create_order():
    data = request.get_json()
    
    required_fields = ["userId", "shopId", "items", "paymentMethod"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    user_id = data["userId"]
    shop_id = data["shopId"]
    items = data["items"]
    payment_method = data["paymentMethod"]
    
    # Get user and validate
    user = execute_query("SELECT * FROM UserAccount WHERE user_id = %s", (user_id,))
    if not user:
        return jsonify({"status": "error", "message": "User not found"}), 404
    
    # Get shop and validate
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    
    # Handle address - either use existing addressId or create new one
    address_id = None
    if "addressId" in data:
        address_id = data["addressId"]
        address = execute_query("SELECT * FROM AddressLine WHERE address_id = %s", (address_id,))
        if not address:
            return jsonify({"status": "error", "message": "Address not found"}), 404
    else:
        # Create new address if needed
        address_fields = ["civic", "street", "city", "postalCode"]
        if all(field in data for field in address_fields):
            new_address_id = get_next_id("AddressLine", "address_id")
            
            execute_query(
                """INSERT INTO AddressLine 
                   (address_id, civic, apartment, street, city, postal_code) 
                   VALUES (%s, %s, %s, %s, %s, %s)""",
                (new_address_id, data["civic"], data.get("apartment"), 
                 data["street"], data["city"], data["postalCode"]),
                fetch=False
            )
            address_id = new_address_id
        elif user[0][6]:  # Use user's address if available
            address_id = user[0][6]
        else:
            return jsonify({"status": "error", "message": "Delivery address is required"}), 400
    
    payment_fields = ["cardName", "cardNumber", "cvcCard", "expiryDateMonth", "expiryDateYear"]
    for field in payment_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing payment field: {field}"}), 400
    
    # Find available carrier (deliverer)
    carrier = execute_query(
        """SELECT user_id FROM UserAccount 
           WHERE user_type = 'carrier' 
           AND user_id NOT IN (
               SELECT DISTINCT deliverer_id FROM ClientOrder 
               WHERE status IN ('Searching', 'InRoute')
           )
           LIMIT 1"""
    )
    
    if not carrier:
        # Fallback - get any carrier if no "available" ones
        carrier = execute_query("SELECT user_id FROM UserAccount WHERE user_type = 'carrier' LIMIT 1")
        if not carrier:
            return jsonify({"status": "error", "message": "No carrier available"}), 400
    
    deliverer_id = carrier[0][0]
    
    # Calculate total amount and validate inventory
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
    
    # Create the order
    execute_query(
        """INSERT INTO ClientOrder 
           (client_order_id, creation_date, status, total_amount, address_id, shop_id, customer_id, deliverer_id) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s)""",
        (order_id, creation_date, "Searching", total_amount, address_id, shop_id, user_id, deliverer_id),
        fetch=False
    )
    
    # Create order items and update inventory
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
    
    # Create payment record
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
    
    # Optional email/password verification
    if "email" in data and "password" in data:
        user = execute_query(
            "SELECT * FROM UserAccount WHERE email = %s AND password = %s", 
            (data["email"], data["password"])
        )
        if not user:
            return jsonify({"status": "error", "message": "Invalid credentials"}), 401
    
    order = execute_query(
        "SELECT * FROM ClientOrder WHERE client_order_id = %s", 
        (order_id,)
    )
    
    if not order:
        return jsonify({"status": "error", "message": "Order not found"}), 404
    
    if order[0][2] == "Cancelled":
        return jsonify({"status": "error", "message": "Order is already cancelled"}), 400
    
    # Get order items and return them to inventory
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
    
    # Update order status to cancelled
    execute_query(
        "UPDATE ClientOrder SET status = 'Cancelled' WHERE client_order_id = %s",
        (order_id,),
        fetch=False
    )
    
    return jsonify({"status": "success", "message": "Order cancelled successfully"})

@app.route('/admin/createUser', methods=['POST'])
def admin_create_user():
    data = request.get_json()
    
    admin_email = data.get("adminEmail") or data.get("email")
    admin_password = data.get("adminPassword") or data.get("password")
    
    # Verify admin credentials
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Process user data - handle different input formats
    user_email = data.get("user_email") or data.get("email")
    user_password = data.get("user_password") or data.get("password")
    user_last_name = data.get("user_lastName") or data.get("lastName")
    user_first_name = data.get("user_firstName") or data.get("firstName")  
    user_phone = data.get("user_phone") or data.get("phoneNumber")
    user_type = data.get("user_type") or data.get("userType")
    
    # Remove admin credentials from data to avoid confusion
    cleaned_data = data.copy()
    if "adminEmail" in cleaned_data: cleaned_data.pop("adminEmail")
    if "adminPassword" in cleaned_data: cleaned_data.pop("adminPassword")
    
    # Map user type to database enum values
    user_type_mapping = {
        "customer": "client",
        "deliverer": "carrier",
        "admin": "admin",
        "client": "client",
        "carrier": "carrier"
    }
    
    if user_type not in user_type_mapping:
        return jsonify({"status": "error", "message": f"Invalid userType: {user_type}"}), 400
    
    db_user_type = user_type_mapping[user_type]
    
    # Create address if provided
    address_id = None
    if "address" in cleaned_data:
        address = cleaned_data["address"]
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
    
    # Get license plate and car brand for carrier
    license_plate = None
    car_brand = None
    if db_user_type == "carrier":
        license_plate = cleaned_data.get("licensePlate")
        car_brand = cleaned_data.get("carBrand")
        if not license_plate:
            return jsonify({"status": "error", "message": "Carrier requires licensePlate"}), 400
    
    new_user_id = get_next_id("UserAccount", "user_id")
    
    # Insert new user
    execute_query(
        """INSERT INTO UserAccount 
           (user_id, email, password, last_name, first_name, phone_number, address_id, user_type, 
            license_plate, car_brand, total_earnings) 
           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s)""",
        (new_user_id, user_email, user_password, user_last_name, user_first_name, 
         user_phone, address_id, db_user_type, license_plate, car_brand, 
         cleaned_data.get("totalEarnings", 0.0)),
        fetch=False
    )
    
    return jsonify({"status": "success", "userId": new_user_id})

@app.route('/admin/modifyUser', methods=['POST'])
def admin_modify_user():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
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
    
    # Map API fields to database fields
    field_mapping = {
        "email": "email",
        "password": "password",
        "lastName": "last_name",
        "firstName": "first_name",
        "phoneNumber": "phone_number",
        "userType": "user_type",
        "licensePlate": "license_plate",
        "carBrand": "car_brand",
        "totalEarnings": "total_earnings"
    }
    
    # User type mapping for consistency
    user_type_mapping = {
        "customer": "client",
        "deliverer": "carrier",
        "admin": "admin"
    }
    
    for key, db_field in field_mapping.items():
        if key in data:
            value = data[key]
            # Special handling for userType
            if key == "userType" and value in user_type_mapping:
                value = user_type_mapping[value]
            
            update_fields.append(f"{db_field} = %s")
            params.append(value)
    
    # Handle address updates
    if "address" in data and data["address"]:
        address = data["address"]
        current_address_id = user[0][6]  # address_id is at index 6
        
        if current_address_id:
            # Update existing address
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
            # Create new address
            new_address_id = get_next_id("AddressLine", "address_id")
            
            execute_query(
                """INSERT INTO AddressLine 
                   (address_id, civic, apartment, street, city, postal_code) 
                   VALUES (%s, %s, %s, %s, %s, %s)""",
                (new_address_id, address.get("civic"), address.get("apartment"), 
                 address.get("street"), address.get("city"), address.get("postalCode", "")),
                fetch=False
            )
            
            # Add address_id to user update
            update_fields.append("address_id = %s")
            params.append(new_address_id)
    
    # Execute the update if there are fields to update
    if update_fields:
        params.append(user_id)
        query = f"UPDATE UserAccount SET {', '.join(update_fields)} WHERE user_id = %s"
        execute_query(query, tuple(params), fetch=False)
        return jsonify({"status": "success", "message": "User updated successfully"})
    else:
        return jsonify({"status": "error", "message": "No fields to update"}), 400

@app.route('/admin/deleteUser', methods=['POST'])
def admin_delete_user():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
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
        # Delete the user
        execute_query("DELETE FROM UserAccount WHERE user_id = %s", (user_id,), fetch=False)
        return jsonify({"status": "success", "message": "User deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/admin/createShop', methods=['POST'])
def admin_create_shop():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Check required fields
    required_fields = ["name", "civic", "street", "city"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    # Create address
    address_id = get_next_id("AddressLine", "address_id")
    
    execute_query(
        """INSERT INTO AddressLine 
           (address_id, civic, apartment, street, city, postal_code) 
           VALUES (%s, %s, %s, %s, %s, %s)""",
        (address_id, data["civic"], data.get("apartment"), data["street"], 
         data["city"], data.get("postalCode", "")),
        fetch=False
    )
    
    # Create shop
    shop_id = get_next_id("Shop", "shop_id")
    
    execute_query(
        "INSERT INTO Shop (shop_id, name, address_id) VALUES (%s, %s, %s)",
        (shop_id, data["name"], address_id),
        fetch=False
    )
    
    return jsonify({"status": "success", "shopId": shop_id})

@app.route('/admin/modifyShop', methods=['POST'])
def admin_modify_shop():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Check required fields
    if "shopId" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    
    shop_id = data["shopId"]
    
    # Check if shop exists
    shop = execute_query(
        "SELECT * FROM Shop WHERE shop_id = %s", 
        (shop_id,)
    )
    
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    
    # Update shop name if provided
    shop_update_fields = []
    shop_params = []
    
    if "name" in data:
        shop_update_fields.append("name = %s")
        shop_params.append(data["name"])
    
    if shop_update_fields:
        shop_params.append(shop_id)
        query = f"UPDATE Shop SET {', '.join(shop_update_fields)} WHERE shop_id = %s"
        execute_query(query, tuple(shop_params), fetch=False)
    
    # Update address if needed
    address_id = shop[0][2]  # address_id is at index 2
    address_update_fields = []
    address_params = []
    
    address_field_mapping = {
        "civic": "civic",
        "apartment": "apartment",
        "street": "street",
        "city": "city",
        "postalCode": "postal_code"
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

@app.route('/admin/deleteShop', methods=['POST'])
def admin_delete_shop():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    if "shopId" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    
    shop_id = data["shopId"]
    
    # Check if shop exists
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    
    try:
        # Delete shop - cascade will handle related records in ShopProduct
        execute_query("DELETE FROM Shop WHERE shop_id = %s", (shop_id,), fetch=False)
        return jsonify({"status": "success", "message": "Shop deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/admin/createProduct', methods=['POST'])
def admin_create_product():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'", 
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Check required fields
    required_fields = ["name", "price", "category", "alcohol"]
    for field in required_fields:
        if field not in data:
            return jsonify({"status": "error", "message": f"Missing required field: {field}"}), 400
    
    product_id = get_next_id("Product", "product_id")
    description = data.get("description", "")
    
    # Insert new product
    execute_query(
        """
        INSERT INTO Product (product_id, name, description, price, category, alcohol)
        VALUES (%s, %s, %s, %s, %s, %s)
        """,
        (
            product_id,
            data["name"],
            description,
            data["price"],
            data["category"],
            data["alcohol"]
        ),
        fetch=False
    )
    
    return jsonify({"status": "success", "productId": product_id})

@app.route('/admin/modifyProduct', methods=['POST'])
def admin_modify_product():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
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
    
    update_fields = []
    params = []
    
    # Map API fields to database fields
    field_mapping = {
        "name": "name",
        "description": "description",
        "price": "price",
        "category": "category",
        "alcohol": "alcohol"
    }
    
    for key, db_field in field_mapping.items():
        if key in data:
            update_fields.append(f"{db_field} = %s")
            params.append(data[key])
    
    if not update_fields:
        return jsonify({"status": "error", "message": "No fields to update"}), 400
    
    # Update product
    params.append(product_id)
    query = f"UPDATE Product SET {', '.join(update_fields)} WHERE product_id = %s"
    execute_query(query, tuple(params), fetch=False)
    
    return jsonify({"status": "success", "message": "Product updated successfully"})

@app.route('/admin/deleteProduct', methods=['POST'])
def admin_delete_product():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
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
    
    try:
        # Delete product - cascades will handle related records
        execute_query("DELETE FROM Product WHERE product_id = %s", (product_id,), fetch=False)
        return jsonify({"status": "success", "message": "Product deleted successfully"})
    except Exception as e:
        return jsonify({"status": "error", "message": str(e)}), 500

@app.route('/admin/getUsers', methods=['POST'])
def admin_get_users():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
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
            u.car_brand,
            u.total_earnings,
            a.civic,
            a.apartment,
            a.street,
            a.city,
            a.postal_code
        FROM UserAccount u
        LEFT JOIN AddressLine a ON u.address_id = a.address_id
    """)
    
    # Map database user_type to API user_type
    user_type_mapping = {
        "client": "customer",
        "carrier": "deliverer",
        "admin": "admin"
    }
    
    formatted_users = []
    for user in users:
        user_data = {
            "userId": user[0],
            "email": user[1],
            "password": user[2],  # Including password as per ENDPOINTS.md specification
            "lastName": user[3],
            "firstName": user[4],
            "phoneNumber": user[5],
            "userType": user_type_mapping.get(user[7], user[7]),
            "licensePlate": user[8] if user[8] else None,
            "carBrand": user[9] if user[9] else None,
            "totalEarnings": float(user[10]) if user[10] else 0.0
        }
        
        # Include address if available
        if user[6]:  # address_id is not null
            apt_str = f" apt {user[12]}" if user[12] else ""
            user_data["address"] = {
                "civic": user[11],
                "apartment": user[12],
                "street": user[13],
                "city": user[14],
                "postalCode": user[15],
                "fullAddress": f"{user[11]}{apt_str} {user[13]}, {user[14]}, {user[15]}"
            }
        
        formatted_users.append(user_data)
    
    return jsonify({"status": "success", "users": formatted_users})

@app.route('/admin/modifyAvailability', methods=['POST'])
def admin_modify_availability():
    data = request.get_json()
    
    # Verify admin credentials
    admin_email = data.get("email") or data.get("adminEmail")
    admin_password = data.get("password") or data.get("adminPassword")
    
    admin = execute_query(
        "SELECT * FROM UserAccount WHERE email = %s AND password = %s AND user_type = 'admin'",
        (admin_email, admin_password)
    )
    
    if not admin:
        return jsonify({"status": "error", "message": "Unauthorized access"}), 401
    
    # Required fields
    if "shopId" not in data:
        return jsonify({"status": "error", "message": "Shop ID is required"}), 400
    
    if "products" not in data or not isinstance(data["products"], list):
        return jsonify({"status": "error", "message": "Products list is required"}), 400
    
    shop_id = data["shopId"]
    products = data["products"]
    
    # Check if shop exists
    shop = execute_query("SELECT * FROM Shop WHERE shop_id = %s", (shop_id,))
    if not shop:
        return jsonify({"status": "error", "message": "Shop not found"}), 404
    
    # Process each product
    for product_item in products:
        if "productId" not in product_item or "quantity" not in product_item:
            return jsonify({"status": "error", "message": "Each product must have productId and quantity"}), 400
        
        product_id = product_item["productId"]
        quantity = product_item["quantity"]
        
        # Check if product exists
        product = execute_query("SELECT * FROM Product WHERE product_id = %s", (product_id,))
        if not product:
            return jsonify({"status": "error", "message": f"Product ID {product_id} not found"}), 404
        
        # Check if product is already associated with shop
        shop_product = execute_query(
            "SELECT * FROM ShopProduct WHERE shop_id = %s AND product_id = %s", 
            (shop_id, product_id)
        )
        
        if shop_product:
            # Update existing product quantity
            execute_query(
                "UPDATE ShopProduct SET quantity = %s WHERE shop_id = %s AND product_id = %s",
                (quantity, shop_id, product_id),
                fetch=False
            )
        else:
            # Add new product to shop
            execute_query(
                "INSERT INTO ShopProduct (shop_id, product_id, quantity) VALUES (%s, %s, %s)",
                (shop_id, product_id, quantity),
                fetch=False
            )
    
    return jsonify({"status": "success", "message": "Shop inventory updated successfully"})

##########################################################################################
##########################################################################################
##########################################################################################
# Variable for the absolute path to your WEB_ADMIN folder
ADMIN_FOLDER = os.path.abspath(os.path.join(app.root_path, '..', 'WEB_ADMIN'))

# Force a redirect to ensure the URL has a trailing slash,
# so relative links inside the HTML resolve correctly.
@app.route('/admin/web')
def admin_web_redirect():
    return redirect('/admin/web/')

# When someone visits /admin/web/ (with trailing slash), serve accueil.html
@app.route('/admin/web/')
def admin_index():
    return send_from_directory(ADMIN_FOLDER, 'accueil.html')

# Catch-all route to serve any file under /admin/web/
@app.route('/admin/web/<path:filename>')
def admin_static(filename):
    return send_from_directory(ADMIN_FOLDER, filename)




if __name__ == '__main__':
    app.run(host="0.0.0.0")
