from typing import Optional, Any, Tuple, List, Dict
from flask import Flask, request, jsonify, Response
from flask_cors import CORS
import typer
from rich.console import Console
from flasgger import Swagger
import os
import psycopg2
from psycopg2.extras import RealDictCursor

console = Console()

app = Flask(__name__)
CORS(app)

# Swagger configuration (default URL: /apidocs)
app.config['SWAGGER'] = {
    'title': 'Boozy API (PostgreSQL)',
    'uiversion': 3
}
swagger = Swagger(app)

# Configure PostgreSQL connection using an environment variable.
# Recommended: DATABASE_URL=postgres://neondb_owner:... (see your Neon configuration)
DATABASE_URL = os.environ.get("DATABASE_URL", "postgres://neondb_owner:npg_Zq74XbeHTVtY@ep-restless-sun-a5vbch0g-pooler.us-east-2.aws.neon.tech/neondb?sslmode=require")
if not DATABASE_URL:
    raise ValueError("DATABASE_URL environment variable is not set.")

# Establish a connection with SSL enabled (as recommended)
conn = psycopg2.connect(DATABASE_URL, sslmode='require')
conn.autocommit = True  # Automatically commit each statement

def requestSQL(sqlQuery: str, params: Optional[Tuple[Any, ...]] = None) -> List[Dict[str, Any]]:
    """
    Executes the given SQL query with optional parameters and returns a list of dictionaries.
    """
    with conn.cursor(cursor_factory=RealDictCursor) as cur:
        cur.execute(sqlQuery, params)
        results = cur.fetchall()
        return results

# -------------------- API Routes -------------------- #

@app.route('/')
def home() -> Response:
    """
    Home endpoint to check if the API is running.
    ---
    responses:
      200:
        description: Returns True if the API is running.
        schema:
          type: boolean
    """
    return jsonify(True)

@app.route('/getStores', methods=['GET'])
def getStores() -> Response:
    """
    Retrieve store information.
    ---
    parameters:
      - name: storeId
        in: query
        type: string
        required: false
        description: ID of the store to fetch.
    responses:
      200:
        description: A list of stores.
        schema:
          type: array
          items:
            type: object
    """
    storeId: Optional[str] = request.args.get('storeId')
    if storeId:
        query = "SELECT * FROM StoreLocation WHERE store_id = %s"
        results = requestSQL(query, (storeId,))
    else:
        query = "SELECT * FROM StoreLocation"
        results = requestSQL(query)
    return jsonify(results)

@app.route('/getProducts', methods=['GET'])
def getProducts() -> Response:
    """
    Retrieve product information.
    ---
    parameters:
      - name: productId
        in: query
        type: string
        required: false
        description: ID of the product to fetch.
    responses:
      200:
        description: A list of products.
        schema:
          type: array
          items:
            type: object
    """
    productId: Optional[str] = request.args.get('productId')
    if productId:
        query = "SELECT * FROM Product WHERE product_id = %s"
        results = requestSQL(query, (productId,))
    else:
        query = "SELECT * FROM Product"
        results = requestSQL(query)
    return jsonify(results)

@app.route('/getAvailability', methods=['GET'])
def getAvailability() -> Response:
    """
    Retrieve product availability.
    ---
    parameters:
      - name: storeId
        in: query
        type: string
        required: false
        description: Store ID for filtering availability.
      - name: productId
        in: query
        type: string
        required: false
        description: Product ID for filtering availability.
    responses:
      200:
        description: A list of product inventory records.
        schema:
          type: array
          items:
            type: object
    """
    storeId: Optional[str] = request.args.get('storeId')
    productId: Optional[str] = request.args.get('productId')
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

@app.route('/createUser', methods=['POST'])
def createUser() -> Response:
    """
    Create a new user.
    ---
    parameters:
      - in: body
        name: body
        schema:
          required:
            - email
            - password
            - lastName
            - firstName
            - phoneNumber
            - userType
          properties:
            email:
              type: string
            password:
              type: string
            lastName:
              type: string
            firstName:
              type: string
            phoneNumber:
              type: string
            userType:
              type: string
              enum: ["customer", "deliverer"]
            licensePlate:
              type: string
    responses:
      200:
        description: Returns the new user's ID.
        schema:
          type: object
          properties:
            userId:
              type: integer
      400:
        description: Error message.
    """
    data: Dict[str, Any] = request.get_json()
    email: str = data["email"]
    password: str = data["password"]
    lastName: str = data["lastName"]
    firstName: str = data["firstName"]
    phoneNumber: str = data["phoneNumber"]
    userType: str = data["userType"]

    if userType not in ["customer", "deliverer"]:
        return jsonify({"status": "error", "message": "Invalid userType"}), 400

    licensePlate: Optional[str] = data.get("licensePlate") if userType == "deliverer" else None
    existing = requestSQL("SELECT * FROM UserAccount WHERE email = %s", (email,))
    if existing:
        return jsonify({"status": "error", "message": "Email already exists"}), 400

    maxId_result = requestSQL("SELECT MAX(user_id) AS max_id FROM UserAccount")
    max_id = maxId_result[0]['max_id'] if maxId_result and maxId_result[0]['max_id'] is not None else 0
    newUserId: int = max_id + 1
    requestSQL(
        "INSERT INTO UserAccount (user_id, email, password, last_name, first_name, phone_number, user_type, license_plate) VALUES (%s, %s, %s, %s, %s, %s, %s, %s)",
        (newUserId, email, password, lastName, firstName, phoneNumber, userType, licensePlate)
    )
    return jsonify({"userId": newUserId})

@app.route('/connectUser', methods=['POST'])
def connectUser() -> Response:
    """
    Authenticate a user.
    ---
    parameters:
      - in: body
        name: body
        schema:
          required:
            - email
            - password
          properties:
            email:
              type: string
            password:
              type: string
    responses:
      200:
        description: Returns user data if authentication is successful.
        schema:
          type: object
          properties:
            userId:
              type: integer
            email:
              type: string
            lastName:
              type: string
            firstName:
              type: string
            phoneNumber:
              type: string
            userType:
              type: string
            licensePlate:
              type: string
      200:
        description: Empty object if authentication fails.
    """
    data: Dict[str, Any] = request.get_json()
    email: str = data["email"]
    password: str = data["password"]
    user = requestSQL("SELECT * FROM UserAccount WHERE email = %s AND password = %s", (email, password))
    if user:
        userData = {
            "userId": user[0]["user_id"],
            "email": user[0]["email"],
            "lastName": user[0]["last_name"],
            "firstName": user[0]["first_name"],
            "phoneNumber": user[0]["phone_number"],
            "userType": user[0]["user_type"],
            "licensePlate": user[0].get("license_plate")
        }
        return jsonify(userData)
    return jsonify({})

# -------------------- CLI Entry Point -------------------- #

def main(host: str = "0.0.0.0", port: int = 5000, debug: bool = True) -> None:
    """
    Start the Flask server using Typer CLI.
    
    Example usage:
        python app_postgres.py --host 127.0.0.1 --port 5000 --debug False
    """
    console.log(f"[bold green]Starting Flask server on {host}:{port}[/bold green]")
    app.run(host=host, port=port, debug=debug)

if __name__ == '__main__':
    typer.run(main)
