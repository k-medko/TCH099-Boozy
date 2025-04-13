# TCH099-Boozy REPO
# Remote access server:
ssh azureuser@4.172.252.189
# Serverstart commands:
## Kill all existing connections:
sudo fuser -k 5000/tcp
# Read Output
tail -f /home/azureuser/server-output.log
# Reset database
mysql -u BOOZY_SERVER -pBoozyadmin1234 boozy_database < /home/azureuser/TCH099-Boozy/REST_API/DATABASE/create.sql
mysql -u BOOZY_SERVER -pBoozyadmin1234 boozy_database < /home/azureuser/TCH099-Boozy/REST_API/DATABASE/inserts.sql
