#!/bin/bash

# Configuration
PID_FILE="/home/azureuser/flask-api.pid"

if [ -f "$PID_FILE" ]; then
    PID=$(cat "$PID_FILE")
    echo "Stopping Flask server with PID: $PID"
    kill $PID 2>/dev/null
    sleep 2

    # Check if process is still running and force kill if needed
    if ps -p "$PID" > /dev/null; then
        echo "Force killing process..."
        kill -9 $PID 2>/dev/null
    fi

    # Clean up PID file
    rm -f "$PID_FILE"
    echo "Server stopped successfully"
else
    echo "No PID file found. Server may not be running."

    # Find any Python processes that might be the server
    PYTHON_PIDS=$(ps aux | grep "[p]ython.*server.py" | awk '{print $2}')
    if [ ! -z "$PYTHON_PIDS" ]; then
        echo "Found running Python server processes:"
        echo "$PYTHON_PIDS"
        echo "Killing processes..."
        echo "$PYTHON_PIDS" | xargs kill -9 2>/dev/null
        echo "Done."
    fi
fi