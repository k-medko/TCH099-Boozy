# TCH099-Boozy REPO
# Remote access server:
ssh azureuser@4.172.252.189
# Serverstart commands:
## Kill all existing commands:
sudo fuser -k 5000/tcp
# Deploy the server on top & silenced:  
/home/azureuser/deploy.sh
nohup /home/azureuser/deploy.sh > deploy.log 2>&1 &
# Read Output
tail -f /home/azureuser/server-output.log

#
mysql -u root -pBoozyadmin1234 boozy_database < /home/azureuser/TCH099-Boozy/REST_API/DATABASE/create.sql
mysql -u root -pBoozyadmin1234 boozy_database < /home/azureuser/TCH099-Boozy/REST_API/DATABASE/inserts.sql
