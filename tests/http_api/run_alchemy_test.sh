#!/bin/bash
#
# Run the alchemy test suite
#
# Usage:
#   ./run_alchemy_test.sh [--category CATEGORY] [--port PORT]
#
# Examples:
#   ./run_alchemy_test.sh                    # Run all alchemy tests
#   ./run_alchemy_test.sh --category items   # Test only item recipes
#   ./run_alchemy_test.sh --category mobs    # Test only mob recipes
#   ./run_alchemy_test.sh --port 8082        # Use different port
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
PORT=8080
CATEGORY=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --category)
            CATEGORY="--category $2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [--port PORT] [--category CATEGORY]"
            echo ""
            echo "Options:"
            echo "  --port PORT       WebServer port (default: 8080)"
            echo "  --category CAT    Test specific category only"
            echo "                    Valid categories: validation, matching, items, mobs, bulk, edge"
            echo ""
            echo "Examples:"
            echo "  $0                          # Run all alchemy tests (42 tests)"
            echo "  $0 --category items         # Test only item recipes (10 tests)"
            echo "  $0 --category mobs          # Test only mob recipes (6 tests)"
            echo "  $0 --category validation    # Test only validation (8 tests)"
            echo "  $0 --port 8082              # Use different port"
            exit 0
            ;;
        *)
            echo "Unknown option: $1"
            echo "Use --help for usage information"
            exit 1
            ;;
    esac
done

echo "============================================================"
echo "Remixed Dungeon - Alchemy System Test Suite"
echo "============================================================"
echo ""

# Check if server is running
echo "Checking if WebServer is running on port $PORT..."
if ! curl -s "http://localhost:$PORT/ready" > /dev/null 2>&1; then
    echo ""
    echo "⚠ WebServer is not running on port $PORT"
    echo ""
    echo "Please start the game with webserver first:"
    echo "  ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer"
    echo ""
    echo "Or use the helper script:"
    echo "  ./start_game_server.sh"
    echo ""
    read -p "Do you want to start the server now? [y/N] " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Starting WebServer..."
        cd "$PROJECT_ROOT"
        ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=$PORT --minimized" &
        SERVER_PID=$!

        echo "Waiting for server to start..."
        for i in {1..30}; do
            if curl -s "http://localhost:$PORT/ready" | grep -q "ready"; then
                echo "✓ Server is ready!"
                break
            fi
            sleep 1
        done
    else
        echo "Exiting..."
        exit 1
    fi
fi

echo ""
echo "Running alchemy tests..."
echo ""

# Run the Python test script
python3 "$SCRIPT_DIR/test_alchemy.py" \
    --port "$PORT" \
    $CATEGORY

EXIT_CODE=$?

echo ""
echo "============================================================"
if [ $EXIT_CODE -eq 0 ]; then
    echo "All tests completed successfully!"
else
    echo "Some tests failed. Check the output above for details."
fi
echo "============================================================"

exit $EXIT_CODE
