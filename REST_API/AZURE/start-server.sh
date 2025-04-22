#!/bin/bash
# Configuration
HOME_DIR="/home/azureuser"
REPO_URL="https://github.com/k-medko/TCH099-Boozy.git"
APP_DIR="$HOME_DIR/TCH099-Boozy"
SERVER_LOG="$HOME_DIR/server-output.log"
VENV_DIR="$HOME_DIR/flask-venv"
PID_FILE="$HOME_DIR/flask-api.pid"

echo "Starting server setup at $(date)" > "$SERVER_LOG"

# Install required system dependencies
echo "Installing system dependencies..." >> "$SERVER_LOG"
sudo apt-get update >> "$SERVER_LOG" 2>&1
sudo apt-get install -y python3-venv python3-full libmysqlclient-dev pkg-config python3.12-dev build-essential >> "$SERVER_LOG" 2>&1

# Create virtual env if it doesn't exist
if [ ! -d "$VENV_DIR" ]; then
    echo "Creating new Python virtual environment..." >> "$SERVER_LOG"
    python3 -m venv "$VENV_DIR" >> "$SERVER_LOG" 2>&1
fi

# Kill any existing server
if [ -f "$PID_FILE" ]; then
    OLD_PID=$(cat "$PID_FILE")
    if ps -p "$OLD_PID" > /dev/null; then
        echo "Stopping existing server (PID: $OLD_PID)..." >> "$SERVER_LOG"
        kill "$OLD_PID" 2>/dev/null
        sleep 2
        # Force kill if still running
        if ps -p "$OLD_PID" > /dev/null; then
            kill -9 "$OLD_PID" 2>/dev/null
        fi
    fi
    rm -f "$PID_FILE"
fi

# Clone or refresh the repository
if [ -d "$APP_DIR" ]; then
    echo "Removing existing repository..." >> "$SERVER_LOG"
    rm -rf "$APP_DIR"
fi

echo "Cloning fresh repository from $REPO_URL..." >> "$SERVER_LOG"
git clone "$REPO_URL" "$APP_DIR" >> "$SERVER_LOG" 2>&1

if [ ! -d "$APP_DIR" ]; then
    echo "Failed to clone repository. Exiting." >> "$SERVER_LOG"
    exit 1
fi

# Activate virtual environment and install dependencies
echo "Installing Python dependencies..." >> "$SERVER_LOG"
source "$VENV_DIR/bin/activate"

# Make sure requirements are installed
if [ -f "$APP_DIR/REST_API/requirements.txt" ]; then
    pip install -r "$APP_DIR/REST_API/requirements.txt" >> "$SERVER_LOG" 2>&1
else
    echo "No requirements.txt found, installing default packages..." >> "$SERVER_LOG"
    pip install flask flask-cors flask-mysqldb >> "$SERVER_LOG" 2>&1
fi

# Start the server
cd "$APP_DIR/REST_API"
echo "Starting server from $(pwd)..." >> "$SERVER_LOG"

# Launch server with nohup, making sure to handle stdout and stderr
nohup "$VENV_DIR/bin/python" server.py >> "$SERVER_LOG" 2>&1 &
NEW_PID=$!

# Verify the server started
sleep 3
if ps -p "$NEW_PID" > /dev/null; then
    echo $NEW_PID > "$PID_FILE"
    echo "Server started with PID: $NEW_PID" >> "$SERVER_LOG"
    echo "Server started successfully with PID: $NEW_PID"
    echo "View logs with: tail -f $SERVER_LOG"
else
    echo "Server failed to start. Check logs at $SERVER_LOG"
    echo "Server failed to start. See log for details." >> "$SERVER_LOG"
    exit 1
fi
