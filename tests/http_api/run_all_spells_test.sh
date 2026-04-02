#!/bin/bash
#
# Run the comprehensive all-spells test suite
#
# Usage:
#   ./run_all_spells_test.sh [--port PORT] [--class CLASS] [--spell SPELL]
#
# Examples:
#   ./run_all_spells_test.sh                    # Test all spells
#   ./run_all_spells_test.sh --class DOCTOR     # Test Doctor spells only
#   ./run_all_spells_test.sh --spell Heal       # Test Heal spell
#

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"

# Default values
PORT=8080
CLASS=""
SPELL=""
EXTRA_ARGS=""

# Parse arguments
while [[ $# -gt 0 ]]; do
    case $1 in
        --port)
            PORT="$2"
            shift 2
            ;;
        --class)
            CLASS="--class $2"
            shift 2
            ;;
        --spell)
            SPELL="--spell $2"
            shift 2
            ;;
        --help|-h)
            echo "Usage: $0 [--port PORT] [--class CLASS] [--spell SPELL]"
            echo ""
            echo "Options:"
            echo "  --port PORT    WebServer port (default: 8080)"
            echo "  --class CLASS  Test specific hero class only"
            echo "  --spell SPELL  Test specific spell only"
            echo ""
            echo "Examples:"
            echo "  $0                          # Test all spells for all classes"
            echo "  $0 --class DOCTOR           # Test only Doctor spells"
            echo "  $0 --spell BloodTransfusion # Test specific spell"
            echo "  $0 --port 8082              # Use different port"
            exit 0
            ;;
        *)
            EXTRA_ARGS="$EXTRA_ARGS $1"
            shift
            ;;
    esac
done

echo "============================================================"
echo "Remixed Dungeon - All Spells Test Suite"
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
        ./gradlew -p RemixedDungeonDesktop runDesktopGameWithWebServer --args="--webserver=$PORT" &
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
echo "Running spell tests..."
echo ""

# Run the Python test script
python3 "$SCRIPT_DIR/test_all_spells.py" \
    --port "$PORT" \
    $CLASS \
    $SPELL \
    $EXTRA_ARGS

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
