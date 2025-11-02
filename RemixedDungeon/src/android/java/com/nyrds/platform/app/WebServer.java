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
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "Not Found");
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