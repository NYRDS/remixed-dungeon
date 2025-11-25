package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.Assets;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.scenes.AboutScene;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {
    private static WebServer instance = null;
    private String serverAddress = null;
    private boolean started = false;
    
    public static boolean isRunning() {
        return instance != null && instance.started;
    }
    
    public static String getServerAddress() {
        if (instance != null && instance.serverAddress != null) {
            return instance.serverAddress;
        }
        return "http://localhost:8080"; // Default address
    }

    public WebServer(int port) {
        super(port);
        instance = this;
        
        // Try to get the IP address
        try {
            for (NetworkInterface networkInterface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
                Enumeration<InetAddress> addrs = networkInterface.getInetAddresses();
                while (addrs.hasMoreElements()) {
                    InetAddress addr = addrs.nextElement();
                    // Skip loopback addresses
                    if (!addr.isLoopbackAddress() && addr.getHostAddress().indexOf(':') == -1) {
                        serverAddress = "http://" + addr.getHostAddress() + ":" + port;
                        GLog.debug("WebServer address: " + serverAddress);
                        break;
                    }
                }
                if (serverAddress != null) {
                    break;
                }
            }
        } catch (SocketException e) {
            GLog.debug("Failed to get IP address: " + e.getMessage());
        }
        
        // Fallback to localhost
        if (serverAddress == null) {
            serverAddress = "http://localhost:" + port;
        }
    }
    
    @Override
    public void start() throws IOException {
        if (!started) {
            super.start();
            started = true;
            // Notify AboutScene to refresh the WebServer link
            GameLoop.pushUiTask(AboutScene::refreshWebServerLink);
        }
    }

    /**
     * Check if a path represents a directory for the current mod
     */
    private boolean isDirectory(String path) {
        GLog.debug("Checking if path is directory: '" + path + "'");
        
        // Special case: empty path or "/" represents the root directory
        if (path.isEmpty() || path.equals("/")) {
            GLog.debug("Path is root directory, returning true");
            return true;
        }
        
        // For Remixed mod, check assets
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            try {
                // Check if this path exists as an asset directory
                String[] assetList = Assets.listAssets(path);
                if (assetList != null && assetList.length > 0) {
                    GLog.debug("Path '" + path + "' is a directory in assets");
                    return true;
                }
            } catch (Exception e) {
                // If we can't list assets, treat as file
                GLog.debug("Failed to list assets for: " + path + " - " + e.getMessage());
            }
            GLog.debug("Path '" + path + "' is not a directory in assets");
            return false;
        } 
        // For other mods, check file system
        else {
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            boolean result = modFile.exists() && modFile.isDirectory();
            GLog.debug("Path '" + path + "' is " + (result ? "" : "not ") + "a directory in filesystem");
            return result;
        }
    }

    private Response serveFs(String file) {
        GLog.debug("serveFs called with file: '" + file + "'");
        
        // Remove trailing slash for directory checking (if present)
        String cleanPath = file;
        if (cleanPath.endsWith("/")) {
            cleanPath = cleanPath.substring(0, cleanPath.length() - 1);
            GLog.debug("Cleaned path from '" + file + "' to '" + cleanPath + "'");
        }
        
        // Handle directory requests
        if (isDirectory(cleanPath)) {
            GLog.debug("Serving directory: " + cleanPath);
            String html = WebServerHtml.generateDirectoryListing(cleanPath);
            return newFixedLengthResponse(Response.Status.OK, "text/html", html);
        }
        // Handle file download requests
        else if(ModdingMode.isResourceExist(file)) {
            GLog.debug("Serving file: " + file);
            InputStream fis = ModdingMode.getInputStream(file);
            Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
            return response;
        } else {
            // File or directory doesn't exist
            GLog.debug("File or directory not found: " + file);
            String html = WebServerHtml.serveNotFound();
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", html);
        }
    }
    

    
    private Response handleFileUpload(IHTTPSession session) {
        try {
            GLog.debug("=== STARTING FILE UPLOAD PROCESS ===");
            GLog.debug("Handling file upload, URI: " + session.getUri());
            
            // Check if we're trying to upload to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                GLog.debug("Upload blocked - attempt to upload to main 'Remixed' mod");
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", 
                    WebServerHtml.serveUploadForm("ERROR: Upload to the main 'Remixed' mod is disabled for security reasons.", ""));
            }
            
            Map<String, String> files = new java.util.HashMap<>();
            session.parseBody(files);
            
            // Get the uploaded file
            String filename = session.getParameters().get("file").get(0);
            GLog.debug("Uploaded filename: " + filename);
            
            String path = "";
            
            // Try to get path from parameters
            if (session.getParameters().containsKey("path")) {
                path = session.getParameters().get("path").get(0);
                GLog.debug("Raw upload path from form parameter: '" + path + "'");
            } else {
                GLog.debug("No path parameter found in form submission");
            }
            
            // Handle null paths
            if (path == null) {
                path = "";
                GLog.debug("Path was null, setting to empty string");
            }
            
            // Sanitize the path
            path = path.replace("..", ""); // Prevent directory traversal
            path = path.replace("//", "/"); // Normalize path separators
            
            // Ensure path ends with / if not empty
            if (!path.isEmpty() && !path.endsWith("/")) {
                path += "/";
            }
            
            GLog.debug("Final sanitized upload path: '" + path + "'");
            
            // Create the full path for the file
            String fullPath = ModdingMode.activeMod() + "/" + path + filename;
            GLog.debug("Full file path: " + fullPath);
            
            // Get the temporary uploaded file
            String tempFilePath = files.get("file");
            GLog.debug("Temp file path: " + tempFilePath);
            if (tempFilePath == null) {
                GLog.debug("No temp file found");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", 
                    WebServerHtml.serveUploadForm("ERROR: No file uploaded.", path));
            }
            
            // Move the file to the correct location
            File tempFile = new File(tempFilePath);
            File destFile = FileSystem.getExternalStorageFile(fullPath);
            GLog.debug("Destination file path: " + destFile.getAbsolutePath());
            
            // Create directories if needed
            File destDir = destFile.getParentFile();
            GLog.debug("Destination directory: " + (destDir != null ? destDir.getAbsolutePath() : "null"));
            if (destDir != null && !destDir.exists()) {
                GLog.debug("Creating destination directory");
                destDir.mkdirs();
            }
            
            // Copy the file
            GLog.debug("Copying file from temp to destination");
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                try (InputStream is = new java.io.FileInputStream(tempFile)) {
                    while ((bytesRead = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, bytesRead);
                    }
                }
            }
            
            // Delete the temporary file
            GLog.debug("Deleting temporary file");
            tempFile.delete();
            
            GLog.debug("=== FILE UPLOAD COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "text/html", 
                WebServerHtml.serveUploadForm("File uploaded successfully to: " + fullPath, path));
                
        } catch (Exception e) {
            GLog.debug("=== FILE UPLOAD FAILED ===");
            GLog.debug("Upload error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", 
                WebServerHtml.serveUploadForm("ERROR: Failed to upload file - " + e.getMessage(), ""));
        }
    }


    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        GLog.debug("WebServer: " + uri);

        if (session.getMethod() == Method.GET) {
            if (uri.equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", WebServerHtml.serveRoot());
            }

            if(uri.startsWith("/list")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", WebServerHtml.serveList());
            }

            if(uri.startsWith("/upload")) {
                // Extract path from query parameters if present
                String path = "";
                GLog.debug("Upload URI: " + uri);

                // Try to get query parameters
                String query = session.getQueryParameterString();
                GLog.debug("Query parameter string: " + query);

                // Parse query parameters manually
                if (query != null && !query.isEmpty()) {
                    // Split by & to get parameter pairs
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("path=")) {
                            path = param.substring(5); // Remove "path=" prefix
                            // URL decode the path
                            try {
                                path = java.net.URLDecoder.decode(path, "UTF-8");
                            } catch (Exception e) {
                                // If decoding fails, use the path as is
                            }
                            // Ensure path is not null
                            if (path == null) {
                                path = "";
                            }
                            break;
                        }
                    }
                }

                GLog.debug("Final upload path: '" + path + "'");
                return newFixedLengthResponse(Response.Status.OK, "text/html", WebServerHtml.serveUploadForm("", path));
            }

            if(uri.startsWith("/edit-json")) {
                // Extract file path from query parameters
                String filePath = "";
                GLog.debug("Edit JSON URI: " + uri);

                // Try to get query parameters
                String query = session.getQueryParameterString();
                GLog.debug("Query parameter string: " + query);

                // Parse query parameters manually
                if (query != null && !query.isEmpty()) {
                    // Split by & to get parameter pairs
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("file=")) {
                            filePath = param.substring(5); // Remove "file=" prefix
                            // URL decode the path
                            try {
                                filePath = java.net.URLDecoder.decode(filePath, "UTF-8");
                            } catch (Exception e) {
                                // If decoding fails, use the path as is
                            }
                            // Ensure path is not null
                            if (filePath == null) {
                                filePath = "";
                            }
                            break;
                        }
                    }
                }

                GLog.debug("File to edit: '" + filePath + "'");
                if (!filePath.isEmpty()) {
                    return newFixedLengthResponse(Response.Status.OK, "text/html", WebServerHtml.serveJsonEditor(filePath));
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", WebServerHtml.serveNotFound());
                }
            }

            if(uri.equals("/log")) {
                return serveLog();
            }

            if(uri.startsWith("/fs/")) {
                return serveFs(uri.substring(4));
            }
        } else if (session.getMethod() == Method.POST) {
            if(uri.startsWith("/upload")) {
                return handleFileUpload(session);
            }

            if(uri.startsWith("/api/save-json")) {
                return handleJsonSave(session);
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "Not Found");
    }
    
    /**
     * Handle saving JSON content to a file
     */
    private Response handleJsonSave(IHTTPSession session) {
        try {
            GLog.debug("Handling JSON save request");

            // Use the same approach as file upload, but for raw JSON data
            Map<String, String> files = new java.util.HashMap<>();

            // This will parse the body and handle both form data and raw data
            // The raw JSON will be available in files.get("postData") for application/json
            session.parseBody(files);

            // Try to get the raw JSON from postData
            String jsonString = files.get("postData");

            // If postData is null, the content may be in the input stream directly
            // This can happen with raw application/json requests
            if (jsonString == null || jsonString.isEmpty()) {
                // Get query parameters in case the data was sent as query parameters
                // (less likely for large JSON but possible for small payloads)
                String body = session.getQueryParameterString();
                if (body != null && !body.isEmpty()) {
                    jsonString = java.net.URLDecoder.decode(body, "UTF-8");
                }
            }

            // If still null, try to read from the input stream directly, but carefully
            if (jsonString == null || jsonString.isEmpty()) {
                GLog.debug("Reading JSON from input stream");
                java.util.Map<String, java.util.List<String>> parms = session.getParameters();

                // If parameters exist, check if we have JSON in parameters
                // This is unlikely but possible depending on how client sends data
                if (!parms.isEmpty()) {
                    for (java.util.Map.Entry<String, java.util.List<String>> entry : parms.entrySet()) {
                        // Look for JSON-like strings in parameters
                        for (String value : entry.getValue()) {
                            if (value.startsWith("{") && value.endsWith("}")) {
                                jsonString = value;
                                break;
                            }
                        }
                        if (jsonString != null) break;
                    }
                }

                // If still not found, try direct input stream reading as last resort
                if (jsonString == null || jsonString.isEmpty()) {
                    try {
                        // Create a smaller buffer and read with timeout
                        byte[] buffer = new byte[4096];
                        java.io.InputStream inputStream = session.getInputStream();

                        // Mark and reset approach to avoid issues with already-read streams
                        if (inputStream.markSupported()) {
                            inputStream.mark(4096);
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead > 0) {
                                jsonString = new String(buffer, 0, bytesRead, "UTF-8");
                            } else {
                                inputStream.reset(); // Reset to marked position
                            }
                        }
                    } catch (Exception e) {
                        GLog.debug("Error reading from input stream: " + e.getMessage());
                    }
                }
            }

            if (jsonString == null || jsonString.isEmpty()) {
                GLog.debug("No JSON data found in request");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Empty request body\"}");
            }

            GLog.debug("Received JSON: " + jsonString.substring(0, Math.min(200, jsonString.length())) + "...");

            // Parse the JSON to extract filePath and content
            org.json.JSONObject jsonData = new org.json.JSONObject(jsonString);
            String filePath = jsonData.getString("filePath");
            String content = jsonData.getString("content");

            GLog.debug("Saving JSON to: " + filePath);

            // Check if we're trying to save to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                GLog.debug("Save blocked - attempt to save to main 'Remixed' mod");
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
                    "{\"error\":\"Save to the main 'Remixed' mod is disabled for security reasons.\"}");
            }

            // Validate that the file path is within the allowed mod directory
            if (filePath.contains("../") || filePath.startsWith("../")) {
                GLog.debug("Directory traversal attempt detected: " + filePath);
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
                    "{\"error\":\"Directory traversal is not allowed.\"}");
            }

            // Validate JSON content
            try {
                new org.json.JSONObject(content);
            } catch (org.json.JSONException e) {
                GLog.debug("Invalid JSON content: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Invalid JSON content: " + e.getMessage() + "\"}");
            }

            // Create the full path for the file
            String fullPath = ModdingMode.activeMod() + "/" + filePath;
            GLog.debug("Full file path: " + fullPath);

            // Create the file
            File destFile = FileSystem.getExternalStorageFile(fullPath);
            GLog.debug("Destination file path: " + destFile.getAbsolutePath());

            // Create directories if needed
            File destDir = destFile.getParentFile();
            GLog.debug("Destination directory: " + (destDir != null ? destDir.getAbsolutePath() : "null"));
            if (destDir != null && !destDir.exists()) {
                GLog.debug("Creating destination directory");
                destDir.mkdirs();
            }

            // Write the content to the file
            GLog.debug("Writing JSON content to file");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile)) {
                fos.write(content.getBytes("UTF-8"));
            }

            GLog.debug("=== JSON SAVE COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "application/json",
                "{\"success\":true, \"message\":\"File saved successfully to: " + fullPath + "\"}");

        } catch (org.json.JSONException e) {
            GLog.debug("=== JSON PARSING ERROR ===");
            GLog.debug("JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"error\":\"Invalid JSON format in request: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            GLog.debug("=== JSON SAVE FAILED ===");
            GLog.debug("JSON save error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                "{\"error\":\"Failed to save JSON file: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Serve the game log file for download
     */
    private Response serveLog() {
        try {
            // Get the log file
            File logFile = FileSystem.getExternalStorageFile("RePdLogFile.log");

            if (!logFile.exists()) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", WebServerHtml.serveNotFound());
            }

            // Open the file as an input stream
            java.io.FileInputStream fis = new java.io.FileInputStream(logFile);

            // Create response with appropriate headers for file download
            Response response = newFixedLengthResponse(Response.Status.OK, "text/plain", fis, logFile.length());
            response.addHeader("Content-Disposition", "attachment; filename=\"RePdLogFile.log\"");
            return response;

        } catch (Exception e) {
            GLog.debug("Error serving log file: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                WebServerHtml.serveUploadForm("ERROR: Failed to serve log file - " + e.getMessage(), ""));
        }
    }
}