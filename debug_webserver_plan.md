# WebServer Debugging Plan

## Problem Statement
The WebServer is not responding to requests (curl returns HTTP 000 with error 52: Empty reply from server) despite all attempts to fix the display logic. The original issue was about file paths showing incorrectly (e.g., "mobs/BeeHive.png" instead of just "BeeHive.png"), but this has evolved into a server not responding issue.

## Steps to Debug

### 1. Basic connectivity test
- Verify the server process is running
- Check if port 8080 is open and bound
- Test with netcat to see if there's any response

### 2. Log analysis
- Check the application logs or console output for exceptions
- Add more detailed logging to identify where execution stops
- Look for any exceptions during server startup

### 3. Simplified test
- Create a minimal endpoint to test basic functionality 
- Temporarily comment out complex logic to isolate the problem
- Make sure the core server infrastructure works

### 4. Compare with working version
- Check what was changed between the last known working version and current
- Identify specific changes that might have broken functionality
- Revert changes incrementally to find the breaking point

### 5. Test with a simpler listDir implementation
- Go back to the most basic working version of listDir
- Add only the minimal required fixes
- Ensure it doesn't use problematic methods

## Testing Steps

1. Start server with extra logging
2. Make simple request to root endpoint
3. Check server logs for execution flow
4. Verify each method is being called properly
5. Test each endpoint individually
6. Verify path handling logic

## Tools to Use
- netstat or ss to check port binding
- Additional GLog.debug statements in critical paths
- Exception handling to catch and report runtime issues
- Gradle with --info or --debug flags to see build details