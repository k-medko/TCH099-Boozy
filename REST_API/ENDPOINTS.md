# ALL NEEDED ENDPOINTS AND THEIR INFORMATION:

# PUBLIC [GET] ENDPOINTS:
/getShops
	*Returns a list of shops with all of their information & adress information*
	?shopId (Filter out a single shop)
	
/getProducts
	*Returns a list of products with all of their informations*
	?productId (Filter out a single product)
	?category (Filter out a single category)

/getAvailability
	*Returns a list of products available at certain shops*
	*Contains the specific informations about the shop & the product*
	?shopId (Filter out a single shop)
	?productId (Filter out a single product)

# PRIVATE [POST] ENDPOINTS:
/createUser
	*Returns success or failure for the account creation*
	data.email
	data.password
	data.last_name
	data.first_name
	data.phone_number
	data.user_type
	if user is deliverer
		data.liscense_plate
		data.car_brand
	else
		data.apartment
		data.civic
		data.street
		data.city

/connectUser
	*Returns all of the users information & adress information (except userId)*
	data.email
	data.password
	
/createOrder
	*Returns success or failure for the order creation and the orderId*
	data.email
	data.password
	data.apartment
	data.civic
	data.street
	data.city
	*If adress already exists use the original*
	data.shopId
	data.productsIds
	data.card_name
	data.card_number
	data.CVC_card
	data.expiry_date_month
	data.expiry_date_year
	
/cancelOrder
	*Returns success or failure for the order cancelation*
	data.email
	data.password
	data.orderId

# ADMIN [POST] ENDPOINTS:
/admin/getUsers
	*Returns all personal information about every user in the database*
	data.email
	data.password

/admin/createUser
	*Returns the userId*
	data.email
	data.password
	data.user_email
	data.user_password
	data.user_firstName
	data.user_lastName
	data.user_phone
	data.user_type
	
/admin/modifyUser
	*Returns success or failure for the changes*
	data.email
	data.password
	data.userId
	data.user_email
	data.user_password
	data.user_firstName
	data.user_lastName
	data.user_phone
	data.user_type

	
/admin/deleteUser
	*Returns success or failure for the deletion*
	data.email
	data.password
	data.userId

/admin/createShop
	*Returns success or failure with the shopId*
	data.email
	data.password
	data.name
	data.civic
	data.street
	data.city
	*Always create new adressLine*
	data.image
	*Starts with no products*
	
/admin/modifyShop
	*Returns success or failure for the changes*
	data.email
	data.password
	data.shopId
	data.name
	data.civic
	data.street
	data.city
	data.image

/admin/deleteShop
	*Return success or failure for the deletion*
	data.email
	data.password
	data.shopId

/admin/modifyAvailability
	*Returns success or failure for the changes*
	data.email
	data.password
	data.shopId
	data.products (complete list of all productIds with their amounts)

/admin/createProduct
	*Returns success or failure with the productId*
	data.email
	data.password
	data.name
	data.description
	data.price
	data.category
	data.alcohol
	data.image

/admin/modifyProduct
	*Returns success or failure for the changes*
	data.email
	data.password
	data.productId
	data.name
	data.description
	data.price
	data.category
	data.alcohol
	data.image

/admin/deleteProduct
	*Return success or failure for the deletion*
	data.email
	data.password
	data.productId