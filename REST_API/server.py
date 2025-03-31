from flask import Flask, request, jsonify
from flask_mysqldb import MySQL
from flask_cors import CORS
app = Flask(__name__)
CORS(app)

#CLOUDFLARE CMD LINE> cloudflared tunnel --url http://localhost:5000

# Configure MySQL connection
app.config['MYSQL_HOST'] = 'localhost'
app.config['MYSQL_USER'] = 'BOOZY_SERVER'
app.config['MYSQL_PASSWORD'] = 'admin'
app.config['MYSQL_DB'] = 'boozy_database'
mysql = MySQL(app)

def requestSQL(sqlQuery, params=None):
    cursor = mysql.connection.cursor()
    cursor.execute(sqlQuery, params)
    results = cursor.fetchall()
    cursor.close()
    return results

@app.route('/')
def home():
    return jsonify(True)

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

if __name__ == '__main__':
    app.run(debug=True)
