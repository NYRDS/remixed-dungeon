package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.Assets;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.scenes.AboutScene;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
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
            GameLoop.pushUiTask(() -> AboutScene.refreshWebServerLink());
        }
    }

    private String defaultHead() {
        return "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>body { font-family: Arial, sans-serif; margin: 20px; } input, select, button { margin: 5px; padding: 5px; } .upload-form { background: #f0f0f0; padding: 15px; border-radius: 5px; margin: 10px 0; } .file-list { margin: 10px 0; } .error { color: red; } .success { color: green; } a { text-decoration: none; color: #007bff; } a:hover { text-decoration: underline; }</style></head>";
    }
    
    private String serveRoot() {
        String msg = "<html><body>";
        msg += defaultHead();
        msg += "<h1>Remixed Dungeon Web Interface</h1>";
        msg += Utils.format("<p>RemixedDungeon: %s (%d)</p>" ,GameLoop.version, GameLoop.versionCode);
        msg += Utils.format("<p>Mod: %s (%d)</p>", ModdingMode.activeMod(), ModdingMode.activeModVersion());
        if(Dungeon.level != null) {
            msg += Utils.format("<p>Level: %s</p>", Dungeon.level.levelId);
        }
        msg += "<p><a href=\"/list\">📁 Browse Files</a></p>";
        msg += "<p><a href=\"/upload?path=\">📤 Upload Files</a></p>";
        msg += "</body></html>";
        return msg;
    }

    private String serveList() {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>File Browser</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/upload?path=\">📤 Upload Files</a></p>");
        msg.append("<h2>Files in Active Mod: ").append(ModdingMode.activeMod()).append("</h2>");
        listDir(msg, "");
        msg.append("</body></html>");

        return msg.toString();
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
    
    /**
     * List contents of a directory for the current mod
     */
    private String[] listDirectoryContents(String path) {
        GLog.debug("Listing contents of directory: '" + path + "'");
        
        // For Remixed mod, get contents from assets
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            try {
                String[] result = Assets.listAssets(path);
                GLog.debug("Found " + (result != null ? result.length : 0) + " items in assets directory");
                return result;
            } catch (Exception e) {
                GLog.debug("Failed to list assets for directory: " + path + " - " + e.getMessage());
                return null;
            }
        } 
        // For other mods, get contents from file system
        else {
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            if (modFile.exists()) {
                String[] result = modFile.list();
                GLog.debug("Found " + (result != null ? result.length : 0) + " items in filesystem directory");
                return result;
            }
            GLog.debug("Filesystem directory does not exist: " + modFile.getAbsolutePath());
            return null;
        }
    }
    
    /**
     * Check if a specific item in a directory is itself a directory
     */
    private boolean isDirectoryItem(String parentPath, String itemName) {
        GLog.debug("Checking if item '" + itemName + "' in directory '" + parentPath + "' is a directory");
        
        // For Remixed mod, check assets
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            try {
                String assetPath = (parentPath.isEmpty() ? "" : parentPath + "/") + itemName;
                String[] assetList = Assets.listAssets(assetPath);
                if (assetList != null && assetList.length > 0) {
                    GLog.debug("Item '" + itemName + "' is a directory in assets");
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Failed to check if asset is directory: " + itemName + " - " + e.getMessage());
            }
            GLog.debug("Item '" + itemName + "' is not a directory in assets");
            return false;
        } 
        // For other mods, check file system
        else {
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + parentPath);
            File item = new File(modFile, itemName);
            boolean result = item.isDirectory();
            GLog.debug("Item '" + itemName + "' is " + (result ? "" : "not ") + "a directory in filesystem");
            return result;
        }
    }

    private static void listDir(StringBuilder msg, String path) {
        GLog.debug("listDir called with path: '" + path + "'");
        List<String> list = ModdingMode.listResources(path,(dir, name)->true);
        
        // Separate directories and files
        List<String> directories = new java.util.ArrayList<>();
        List<String> files = new java.util.ArrayList<>();
        
        for (String name : list) {
            // Check if this is a directory by looking at both the filesystem and assets
            boolean isDirectory = false;
            
            // First check external storage (for mod files)
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path + name);
            if (modFile.exists() && modFile.isDirectory()) {
                isDirectory = true;
            } 
            // For the Remixed mod, also check assets
            else if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                try {
                    // Check if this path exists as an asset directory
                    String assetPath = (path.isEmpty() ? "" : path + "/") + name;
                    String[] assetList = Assets.listAssets(assetPath);
                    if (assetList != null && assetList.length > 0) {
                        isDirectory = true;
                    }
                } catch (Exception e) {
                    // If we can't list assets, treat as file
                    GLog.debug("Failed to list assets for: " + name + " - " + e.getMessage());
                }
            }
            
            if (isDirectory) {
                directories.add(name);
            } else {
                files.add(name);
            }
        }
        
        // Sort directories and files separately
        Collections.sort(directories);
        Collections.sort(files);

        if (!path.isEmpty() && !path.equals("/")) {
            String upOneLevel = path.contains("/") ? path.substring(0, path.lastIndexOf("/")) : "";
            msg.append(Utils.format("<p><a href=\"/fs/%s\">..</a></p>", upOneLevel));
        }

        msg.append("<div class=\"file-list\">");
        // Add upload link for current directory
        String uploadPath = path.isEmpty() ? "" : path;
        if (!uploadPath.endsWith("/") && !uploadPath.isEmpty()) {
            uploadPath += "/";
        }
        GLog.debug("Generating upload link for directory: '" + uploadPath + "'");
        String encodedUploadPath = "";
        try {
            encodedUploadPath = java.net.URLEncoder.encode(uploadPath, "UTF-8");
            GLog.debug("Encoded upload path: '" + encodedUploadPath + "' from original: '" + uploadPath + "'");
        } catch (Exception e) {
            encodedUploadPath = uploadPath; // Fallback if encoding fails
            GLog.debug("Upload path encoding failed, using original: '" + uploadPath + "'");
        }
        msg.append(Utils.format("<p>📤 <a href=\"/upload?path=%s\">Upload files to this directory</a></p>", encodedUploadPath));
        
        // List directories first
        for (String name : directories) {
            // Directory
            if(path.isEmpty()) {
                GLog.debug("Generating directory link for root directory: " + name);
                msg.append(Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>", name, name));
            } else {
                GLog.debug("Generating directory link for subdirectory: " + path + name);
                msg.append(Utils.format("<p>📁 <a href=\"/fs/%s%s/\">%s%s/</a></p>", path, name, path, name));
            }
        }
        
        // Then list files
        for (String name : files) {
            // File
            if(path.isEmpty()) {
                msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", name, name));
            } else {
                msg.append(Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s%s</a></p>", path, name, path, name));
            }
        }
        msg.append("</div>");
    }

    /**
     * Generate HTML for directory listing page
     */
    private String generateDirectoryListing(String directoryPath) {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>Directory: ").append(directoryPath.isEmpty() ? "/" : directoryPath).append("</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/upload?path=");
        String encodedPath = "";
        try {
            encodedPath = java.net.URLEncoder.encode(directoryPath, "UTF-8");
            GLog.debug("Encoded directory path for upload link: '" + encodedPath + "' from original: '" + directoryPath + "'");
        } catch (Exception e) {
            encodedPath = directoryPath; // Fallback if encoding fails
            GLog.debug("Directory path encoding failed, using original: '" + directoryPath + "'");
        }
        msg.append(encodedPath);
        msg.append("\">📤 Upload Files</a></p>");
        
        // Add "up one level" link if not at root
        if (!directoryPath.isEmpty()) {
            String upOneLevel = directoryPath.contains("/") ? directoryPath.substring(0, directoryPath.lastIndexOf("/")) : "";
            if (upOneLevel.isEmpty()) {
                msg.append("<p><a href=\"/list\">..</a></p>");
            } else {
                msg.append(Utils.format("<p><a href=\"/fs/%s/\">..</a></p>", upOneLevel));
            }
        }
        
        // List directory contents with directories first
        String[] contents = listDirectoryContents(directoryPath);
        
        if (contents != null) {
            // Separate directories and files
            List<String> directories = new java.util.ArrayList<>();
            List<String> files = new java.util.ArrayList<>();
            
            for (String name : contents) {
                if (isDirectoryItem(directoryPath, name)) {
                    directories.add(name);
                } else {
                    files.add(name);
                }
            }
            
            // Sort directories and files separately
            Collections.sort(directories);
            Collections.sort(files);
            
            msg.append("<div class=\"file-list\">");
            // Add upload link for current directory
            String uploadPath = directoryPath.isEmpty() ? "" : directoryPath;
            if (!uploadPath.endsWith("/") && !uploadPath.isEmpty()) {
                uploadPath += "/";
            }
            GLog.debug("Generating upload link for directory in serveFs: '" + uploadPath + "'");
            String encodedUploadPath = "";
            try {
                encodedUploadPath = java.net.URLEncoder.encode(uploadPath, "UTF-8");
                GLog.debug("Encoded upload path in serveFs: '" + encodedUploadPath + "' from original: '" + uploadPath + "'");
            } catch (Exception e) {
                encodedUploadPath = uploadPath; // Fallback if encoding fails
                GLog.debug("Upload path encoding failed in serveFs, using original: '" + uploadPath + "'");
            }
            msg.append(Utils.format("<p>📤 <a href=\"/upload?path=%s\">Upload files to this directory</a></p>", encodedUploadPath));
            
            // List directories first
            for (String name : directories) {
                msg.append(Utils.format("<p>📁 <a href=\"/fs/%s%s/\">%s/</a></p>", 
                    directoryPath.isEmpty() ? name : directoryPath + "/" + name,
                    "",  // Empty string to complete the format
                    name));
            }
            // Then list files
            for (String name : files) {
                msg.append(Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s</a></p>", 
                    directoryPath.isEmpty() ? name : directoryPath + "/" + name,
                    "",  // Empty string to complete the format
                    name));
            }
            msg.append("</div>");
        }
        
        msg.append("</body></html>");
        return msg.toString();
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
            String html = generateDirectoryListing(cleanPath);
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
            StringBuilder msg = new StringBuilder("<html><body>");
            msg.append(defaultHead());
            msg.append("<h1>Not Found</h1>");
            msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/upload\">📤 Upload Files</a></p>");
            msg.append("<p>The requested file or directory was not found.</p>");
            msg.append("</body></html>");
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", msg.toString());
        }
    }
    
    private String serveUploadForm(String message, String currentPath) {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>File Upload</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a></p>");
        
        if (message != null && !message.isEmpty()) {
            if (message.startsWith("ERROR:")) {
                msg.append("<div class=\"error\">").append(message.substring(6)).append("</div>");
            } else {
                msg.append("<div class=\"success\">").append(message).append("</div>");
            }
        }
        
        // Check if upload is allowed
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            msg.append("<div class=\"error\"><h2>Upload Disabled</h2><p>File upload to the main 'Remixed' mod is disabled for security reasons.</p></div>");
        } else {
            msg.append("<div class=\"upload-form\">");
            msg.append("<h2>Upload to Mod: ").append(ModdingMode.activeMod()).append("</h2>");
            if (currentPath != null && !currentPath.isEmpty()) {
                msg.append("<h3>Current Directory: ").append(currentPath).append("</h3>");
            } else {
                msg.append("<h3>Current Directory: Root</h3>");
            }
            msg.append("<form method=\"post\" action=\"/upload\" enctype=\"multipart/form-data\">");
            // Make sure we handle null paths
            String safePath = (currentPath != null) ? currentPath : "";
            msg.append("<input type=\"hidden\" name=\"path\" value=\"").append(safePath).append("\">");
            msg.append("<label for=\"file\">Select file to upload:</label><br>");
            msg.append("<input type=\"file\" name=\"file\" id=\"file\" required><br>");
            msg.append("<button type=\"submit\">Upload File</button>");
            msg.append("</form>");
            msg.append("</div>");
        }
        
        msg.append("</body></html>");
        return msg.toString();
    }
    
    private Response handleFileUpload(IHTTPSession session) {
        try {
            GLog.debug("=== STARTING FILE UPLOAD PROCESS ===");
            GLog.debug("Handling file upload, URI: " + session.getUri());
            
            // Check if we're trying to upload to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                GLog.debug("Upload blocked - attempt to upload to main 'Remixed' mod");
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", 
                    serveUploadForm("ERROR: Upload to the main 'Remixed' mod is disabled for security reasons.", ""));
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
                    serveUploadForm("ERROR: No file uploaded.", path));
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
                serveUploadForm("File uploaded successfully to: " + fullPath, path));
                
        } catch (Exception e) {
            GLog.debug("=== FILE UPLOAD FAILED ===");
            GLog.debug("Upload error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", 
                serveUploadForm("ERROR: Failed to upload file - " + e.getMessage(), ""));
        }
    }


    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        GLog.debug("WebServer: " + uri);
        
        if (session.getMethod() == Method.GET) {
            if (uri.equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveRoot());
            }

            if(uri.startsWith("/list")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveList());
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
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveUploadForm("", path));
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
}