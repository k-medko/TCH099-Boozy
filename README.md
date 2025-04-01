# TCH099-Boozy REPO
# Remote access server:
ssh azureuser@4.172.255.120
# Serverstart commands:
## Kill all existing commands: 
sudo fuser -k 5000/tcp 
# Deploy the server on top & silenced:  
/home/azureuser/deploy.sh
nohup /home/azureuser/deploy.sh > deploy.log 2>&1 &