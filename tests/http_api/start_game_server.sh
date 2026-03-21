#!/bin/bash
# Start the Remixed Dungeon desktop game with webserver in windowed mode
# Usage: ./start_game_server.sh [--port PORT] [--clean]

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
PORT=8080
CLEAN=false

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --clean)
            CLEAN=true
            shift
            ;;
        *)
            echo "Unknown option: $1"
            echo "Usage: $0 [--port PORT] [--clean]"
            exit 1
            ;;
    esac
done

cd "$PROJECT_ROOT"

# Kill any existing game processes
pkill -f "DesktopLauncher" 2>/dev/null
pkill -f "runDesktopGameWithWebServer" 2>/dev/null
sleep 2

# Build and run
if [ "$CLEAN" = true ]; then
    echo "Running clean build..."
    ./gradlew -p RemixedDungeonDesktop clean runDesktopGameWithWebServer --args="--webserver=$PORT --windowed" &
else
    ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=$PORT --windowed" &
fi

echo "Game starting on port $PORT..."
echo "Waiting for webserver to be ready..."

# Wait for server to be ready
for i in {1..60}; do
    if curl -s "http://localhost:$PORT/" > /dev/null 2>&1; then
        echo "Webserver is ready at http://localhost:$PORT/"
        exit 0
    fi
    sleep 2
done

echo "Timeout waiting for webserver to start"
exit 1
