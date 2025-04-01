from flask import Flask, request, jsonify, send_from_directory
from flask_mysqldb import MySQL
from flask_cors import CORS
import typer
import rich
import os

app = Flask(__name__)
CORS(app)

# Configure MySQL connection
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'BOOZY_SERVER'
app.config['MYSQL_PASSWORD'] = 'Boozyadmin1234'
app.config['MYSQL_DB'] = 'boozy_database'
mysql = MySQL(app)

def requestSQL(sqlQuery, params=None):
    cursor = mysql.connection.cursor()
    cursor.execute(sqlQuery, params)
    mysql.connection.commit()  # commit for insert/update queries
    results = cursor.fetchall()
    cursor.close()
    return results

# HOME
@app.route('/')
def home():
    return jsonify("Bienvenu dans l'API.")

# HOME
@app.route('/test')
def test():
    return jsonify("Yes, it updated TWICE, LETS FUCKING GOOOOOO, THREE TIMES WTF, FOUR TIMES OMFG!")

# STORES
@app.route('/getStores', methods=['GET'])
def getStores():
    storeId = request.args.get('storeId')
    if storeId:
        query = "SELECT * FROM StoreLocation WHERE store_id = %s"
        results = requestSQL(query, (storeId,))
    else:
        query = "SELECT * FROM StoreLocation"
        results = requestSQL(query)
    return jsonify(results)

# PRODUCTS
@app.route('/getProducts', methods=['GET'])
def getProducts():
    productId = request.args.get('productId')
    if productId:
        query = "SELECT * FROM Product WHERE product_id = %s"
        results = requestSQL(query, (productId,))
    else:
        query = "SELECT * FROM Product"
        results = requestSQL(query)
    return jsonify(results)

# AVAILABILITY
@app.route('/getAvailability', methods=['GET'])
def getAvailability():
    storeId = request.args.get('storeId')
    productId = request.args.get('productId')
    if storeId and productId:
        query = "SELECT * FROM ProductInventory WHERE store_id = %s AND product_id = %s"
        results = requestSQL(query, (storeId, productId))
    elif storeId:
        query = "SELECT * FROM ProductInventory WHERE store_id = %s"
        results = requestSQL(query, (storeId,))
    elif productId:
        query = "SELECT * FROM ProductInventory WHERE product_id = %s"
        results = requestSQL(query, (productId,))
    else:
        query = "SELECT * FROM ProductInventory"
        results = requestSQL(query)
    return jsonify(results)

# USERS - Create User
@app.route('/createUser', methods=['POST'])
def createUser():
    data = request.get_json()
    email = data["email"]
    password = data["password"]
    lastName = data["lastName"]
    firstName = data["firstName"]
    phoneNumber = data["phoneNumber"]
    userType = data["userType"]
    if userType not in ["customer", "deliverer"]:
        return jsonify({"status": "error", "message": "Invalid userType"}), 400
    licensePlate = data.get("licensePlate") if userType == "deliverer" else None
    existing = requestSQL("SELECT * FROM UserAccount WHERE email = %s", (email,))
    if existing:
        return jsonify({"status": "error", "message": "Email already exists"}), 400
    maxId = requestSQL("SELECT MAX(user_id) FROM UserAccount")
    newUserId = (maxId[0][0] + 1) if maxId and maxId[0][0] else 1
    requestSQL(
        "INSERT INTO UserAccount (user_id, email, password, last_name, first_name, phone_number, user_type, license_plate) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
        (newUserId, email, password, lastName, firstName, phoneNumber, userType, licensePlate)
    )
    return jsonify({"userId": newUserId})

# USERS - Connect User
@app.route('/connectUser', methods=['POST'])
def connectUser():
    data = request.get_json()
    email = data["email"]
    password = data["password"]
    user = requestSQL("SELECT * FROM UserAccount WHERE email = %s AND password = %s", (email, password))
    if user:
        userData = {
            "userId": user[0][0],
            "email": user[0][1],
            "lastName": user[0][3],
            "firstName": user[0][4],
            "phoneNumber": user[0][5],
            "userType": user[0][6],
            "licensePlate": user[0][7]
        }
        return jsonify(userData)
    return jsonify({})

@app.route('/favicon.ico')
def favicon():
    return send_from_directory(os.path.join(app.root_path, 'static'),
        'favicon.ico', mimetype='image/vnd.microsoft.icon')

if __name__ == '__main__':
    app.run(host="0.0.0.0",debug=True)