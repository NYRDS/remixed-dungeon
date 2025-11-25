package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import fi.iki.elonen.NanoHTTPD;

/**
 * WebServer implementation for desktop platforms.
 * This provides the same API as the Android WebServer and actually starts a server.
 */
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

    /**
     * Load template from assets using ModdingMode
     */
    private String loadTemplate(String templateName) {
        try {
            // Use ModdingMode to access the template file from the current mod assets
            java.io.InputStream inputStream = ModdingMode.getInputStream("html/" + templateName);

            if (inputStream != null) {
                StringBuilder content = new StringBuilder();
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(inputStream))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        content.append(line).append("\n");
                    }
                }
                return content.toString();
            } else {
                GLog.w("Template not found: " + templateName + " in current mod: " + ModdingMode.activeMod());
                return "<html><body><h1>Template Error</h1><p>Template not found: " + templateName + "</p></body></html>";
            }
        } catch (Exception e) {
            GLog.w("Failed to load template: " + templateName + ", error: " + e.getMessage());
            return "<html><body><h1>Template Error</h1><p>Failed to load template: " + templateName + "</p></body></html>";
        }
    }

    /**
     * Replace placeholders in template
     */
    private String replacePlaceholders(String template, java.util.Map<String, String> replacements) {
        String result = template;
        for (java.util.Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    @Override
    public void start() throws IOException {
        if (!started) {
            super.start();
            started = true;
        }
    }

    /**
     * Generate the root page HTML using template
     */
    public String serveRoot() {
        String template = loadTemplate("root_template.html");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("GAME_VERSION", com.watabou.pixeldungeon.utils.Utils.format("%s (%d)", GameLoop.version, GameLoop.versionCode));
        replacements.put("MOD_INFO", com.watabou.pixeldungeon.utils.Utils.format("%s (%d)", ModdingMode.activeMod(), ModdingMode.activeModVersion()));

        String levelInfo = "";
        if(Dungeon.level != null) {
            levelInfo = com.watabou.pixeldungeon.utils.Utils.format("<p>Level: %s</p>", Dungeon.level.levelId);
        }
        replacements.put("LEVEL_INFO", levelInfo);

        return replacePlaceholders(template, replacements);
    }

    /**
     * Generate the file listing page HTML
     */
    public String serveList() {
        String template = loadTemplate("list_template.html");

        // We need to generate directory contents dynamically
        StringBuilder dirContent = new StringBuilder();
        listDir(dirContent, "");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("MOD_NAME", ModdingMode.activeMod());
        replacements.put("DIRECTORY_CONTENTS", dirContent.toString());

        return replacePlaceholders(template, replacements);
    }

    /**
     * Generate HTML for directory listing page
     */
    public String generateDirectoryListing(String directoryPath) {
        String template = loadTemplate("directory_listing_template.html");

        // Generate up one level link
        String upOneLevelLink = "";
        if (!directoryPath.isEmpty()) {
            String upOneLevel = directoryPath.contains("/") ? directoryPath.substring(0, directoryPath.lastIndexOf("/")) : "";
            if (upOneLevel.isEmpty()) {
                upOneLevelLink = "<p><a href=\"/list\">..</a></p>";
            } else {
                upOneLevelLink = com.watabou.pixeldungeon.utils.Utils.format("<p><a href=\"/fs/%s/\">..</a></p>", upOneLevel);
            }
        }

        // Generate directory listing content
        StringBuilder dirListing = new StringBuilder();

        // List directory contents with directories first
        String[] contents = listDirectoryContents(directoryPath);

        if (contents != null) {
            // Separate directories and files
            java.util.List<String> directories = new java.util.ArrayList<>();
            java.util.List<String> files = new java.util.ArrayList<>();

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

            // Add upload link for current directory
            String uploadPath = directoryPath.isEmpty() ? "" : directoryPath;
            if (!uploadPath.endsWith("/") && !uploadPath.isEmpty()) {
                uploadPath += "/";
            }
            GLog.debug("Generating upload link for directory in serveFs: '" + uploadPath + "'");
            String encodedUploadPath;
            try {
                encodedUploadPath = java.net.URLEncoder.encode(uploadPath, "UTF-8");
                GLog.debug("Encoded upload path in serveFs: '" + encodedUploadPath + "' from original: '" + uploadPath + "'");
            } catch (Exception e) {
                encodedUploadPath = uploadPath; // Fallback if encoding fails
                GLog.debug("Upload path encoding failed in serveFs, using original: '" + uploadPath + "'");
            }
            dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📤 <a href=\"/upload?path=%s\">Upload files to this directory</a></p>", encodedUploadPath));

            // List directories first
            for (String name : directories) {
                dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📁 <a href=\"/fs/%s%s/\">%s/</a></p>",
                    directoryPath.isEmpty() ? name : directoryPath + "/" + name,
                    "",  // Empty string to complete the format
                    name));
            }
            // Then list files
            for (String name : files) {
                String fullPath = directoryPath.isEmpty() ? name : directoryPath + "/" + name;
                if (name.toLowerCase().endsWith(".json")) {
                    // For JSON files, add both download and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2));
                } else {
                    // For non-JSON files, just show download link
                    dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>",
                        fullPath, name));
                }
            }
        }

        // Prepare replacements for the template
        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("DIRECTORY_PATH", directoryPath.isEmpty() ? "/" : directoryPath);
        try {
            replacements.put("ENCODED_UPLOAD_PATH", java.net.URLEncoder.encode(directoryPath, "UTF-8"));
        } catch (Exception e) {
            replacements.put("ENCODED_UPLOAD_PATH", directoryPath); // Fallback if encoding fails
        }
        replacements.put("UP_ONE_LEVEL_LINK", upOneLevelLink);
        replacements.put("DIRECTORY_LISTING", dirListing.toString());

        return replacePlaceholders(template, replacements);
    }

    /**
     * Generate the file upload form HTML
     */
    public String serveUploadForm(String message, String currentPath) {
        String template = loadTemplate("upload_form_template.html");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();

        // Prepare message div
        String messageDiv = "";
        if (message != null && !message.isEmpty()) {
            if (message.startsWith("ERROR:")) {
                messageDiv = "<div class=\"error\">" + message.substring(6) + "</div>";
            } else {
                messageDiv = "<div class=\"success\">" + message + "</div>";
            }
        }
        replacements.put("MESSAGE_DIV", messageDiv);

        // Prepare upload form content
        String uploadFormContent;
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            uploadFormContent = "<div class=\"error\"><h2>Upload Disabled</h2><p>File upload to the main 'Remixed' mod is disabled for security reasons.</p></div>";
        } else {
            StringBuilder formContent = new StringBuilder();
            formContent.append("<div class=\"upload-form\">");
            formContent.append("<h2>Upload to Mod: ").append(ModdingMode.activeMod()).append("</h2>");
            if (currentPath != null && !currentPath.isEmpty()) {
                formContent.append("<h3>Current Directory: ").append(currentPath).append("</h3>");
            } else {
                formContent.append("<h3>Current Directory: Root</h3>");
            }
            formContent.append("<form method=\"post\" action=\"/upload\" enctype=\"multipart/form-data\">");
            // Make sure we handle null paths
            String safePath = (currentPath != null) ? currentPath : "";
            formContent.append("<input type=\"hidden\" name=\"path\" value=\"").append(safePath).append("\">");
            formContent.append("<label for=\"file\">Select file to upload:</label><br>");
            formContent.append("<input type=\"file\" name=\"file\" id=\"file\" required><br>");
            formContent.append("<button type=\"submit\">Upload File</button>");
            formContent.append("</form>");
            formContent.append("</div>");
            uploadFormContent = formContent.toString();
        }
        replacements.put("UPLOAD_FORM_CONTENT", uploadFormContent);

        return replacePlaceholders(template, replacements);
    }

    /**
     * Generate the JSON editor page HTML using template
     */
    public String serveJsonEditor(String filePath) {
        String template = loadTemplate("json_editor_template.html");

        String uploadPath = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf("/")) : "";

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("UPLOAD_PATH", uploadPath);
        replacements.put("FILE_PATH", filePath);
        replacements.put("ESCAPED_FILE_PATH", javaScriptEscape(filePath));

        return replacePlaceholders(template, replacements);
    }

    /**
     * Generate the "Not Found" error page HTML using template
     */
    public String serveNotFound() {
        String template = loadTemplate("not_found_template.html");
        return replacePlaceholders(template, new java.util.HashMap<>());
    }

    private static String javaScriptEscape(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                  .replace("\"", "\\\"")
                  .replace("'", "\\'")
                  .replace("\n", "\\n")
                  .replace("\r", "\\r")
                  .replace("\t", "\\t")
                  .replace("</", "<\\/"); // Prevent breaking out of script tags
    }

    /**
     * List contents of a directory for the current mod
     */
    private String[] listDirectoryContents(String path) {
        GLog.debug("Listing contents of directory: '" + path + "'");

        // For desktop, get contents from file system
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
        if (modFile.exists()) {
            String[] result = modFile.list();
            GLog.debug("Found " + (result != null ? result.length : 0) + " items in filesystem directory");
            return result;
        }
        GLog.debug("Filesystem directory does not exist: " + modFile.getAbsolutePath());
        return null;
    }

    /**
     * Check if a specific item in a directory is itself a directory
     */
    private boolean isDirectoryItem(String parentPath, String itemName) {
        GLog.debug("Checking if item '" + itemName + "' in directory '" + parentPath + "' is a directory");

        // For desktop, check file system
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + parentPath);
        File item = new File(modFile, itemName);
        boolean result = item.isDirectory();
        GLog.debug("Item '" + itemName + "' is " + (result ? "" : "not ") + "a directory in filesystem");
        return result;
    }

    private void listDir(StringBuilder msg, String path) {
        GLog.debug("listDir called with path: '" + path + "'");
        // This method needs to implement the same logic as the Android version
        java.util.List<String> list = ModdingMode.listResources(path,(dir, name)->true);

        // Separate directories and files
        java.util.List<String> directories = new java.util.ArrayList<>();
        java.util.List<String> files = new java.util.ArrayList<>();

        for (String name : list) {
            // Check if this is a directory by looking at both the filesystem and assets
            boolean isDirectory = false;

            // First check external storage (for mod files)
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path + name);
            if (modFile.exists() && modFile.isDirectory()) {
                isDirectory = true;
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
            msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p><a href=\"/fs/%s\">..</a></p>", upOneLevel));
        }

        msg.append("<div class=\"file-list\">");
        // Add upload link for current directory
        String uploadPath = path.isEmpty() ? "" : path;
        if (!uploadPath.endsWith("/") && !uploadPath.isEmpty()) {
            uploadPath += "/";
        }
        GLog.debug("Generating upload link for directory: '" + uploadPath + "'");
        String encodedUploadPath;
        try {
            encodedUploadPath = java.net.URLEncoder.encode(uploadPath, "UTF-8");
            GLog.debug("Encoded upload path: '" + encodedUploadPath + "' from original: '" + uploadPath + "'");
        } catch (Exception e) {
            encodedUploadPath = uploadPath; // Fallback if encoding fails
            GLog.debug("Upload path encoding failed, using original: '" + uploadPath + "'");
        }
        msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📤 <a href=\"/upload?path=%s\">Upload files to this directory</a></p>", encodedUploadPath));

        // List directories first
        for (String name : directories) {
            // Directory
            if(path.isEmpty()) {
                GLog.debug("Generating directory link for root directory: " + name);
                msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>", name, name));
            } else {
                GLog.debug("Generating directory link for subdirectory: " + path + name);
                msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📁 <a href=\"/fs/%s%s/\">%s%s/</a></p>", path, name, path, name));
            }
        }

        // Then list files
        for (String name : files) {
            // File
            if(path.isEmpty()) {
                if (name.toLowerCase().endsWith(".json")) {
                    // For JSON files, add both download and edit links
                    String encodedPath1;
                    try {
                        encodedPath1 = java.net.URLEncoder.encode(name, "UTF-8");
                    } catch (Exception e) {
                        encodedPath1 = name; // Fallback if encoding fails
                    }
                    msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>", name, name, encodedPath1));
                } else {
                    // For non-JSON files, just show download link
                    msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", name, name));
                }
            } else {
                if (name.toLowerCase().endsWith(".json")) {
                    // For JSON files, add both download and edit links
                    String fullPath = path + "/" + name;
                    String encodedPath;
                    try {
                        encodedPath = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath = fullPath; // Fallback if encoding fails
                    }
                    msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>",
                        path, name, path, name, encodedPath));
                } else {
                    // For non-JSON files, just show download link
                    msg.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s%s</a></p>", path, name, path, name));
                }
            }
        }
        msg.append("</div>");
    }

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        GLog.debug("WebServer: " + uri);

        if (session.getMethod() == Method.GET) {
            if (uri.equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html",
                    serveRoot());
            }

            if(uri.startsWith("/list")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html",
                    serveList());
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
                return newFixedLengthResponse(Response.Status.OK, "text/html",
                    serveUploadForm("", path));
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
                    return newFixedLengthResponse(Response.Status.OK, "text/html",
                        serveJsonEditor(filePath));
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html",
                        serveNotFound());
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

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html",
            serveNotFound());
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

        // For desktop, check file system
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
        boolean result = modFile.exists() && modFile.isDirectory();
        GLog.debug("Path '" + path + "' is " + (result ? "" : "not ") + "a directory in filesystem");
        return result;
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
            try {
                String html = generateDirectoryListing(cleanPath);
                return newFixedLengthResponse(Response.Status.OK, "text/html", html);
            } catch (Exception e) {
                GLog.w("Error in generateDirectoryListing: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                    serveNotFound());
            }
        }
        // Handle file download requests
        else {
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + file);
            if (modFile.exists() && modFile.isFile()) {
                GLog.debug("Serving file: " + file);
                try {
                    FileInputStream fis = new FileInputStream(modFile);
                    Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
                    response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
                    return response;
                } catch (Exception e) {
                    GLog.w("Error reading file: " + e.getMessage());
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                        serveNotFound());
                }
            } else {
                // File or directory doesn't exist
                GLog.debug("File or directory not found: " + file);
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html",
                    serveNotFound());
            }
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
                try (InputStream is = new FileInputStream(tempFile)) {
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
            JSONObject jsonData = new JSONObject(jsonString);
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
                new JSONObject(content);
            } catch (JSONException e) {
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
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(content.getBytes("UTF-8"));
            }

            GLog.debug("=== JSON SAVE COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "application/json",
                "{\"success\":true, \"message\":\"File saved successfully to: " + fullPath + "\"}");

        } catch (JSONException e) {
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
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html",
                    serveNotFound());
            }

            // Open the file as an input stream
            FileInputStream fis = new FileInputStream(logFile);

            // Create response with appropriate headers for file download
            Response response = newFixedLengthResponse(Response.Status.OK, "text/plain", fis, logFile.length());
            response.addHeader("Content-Disposition", "attachment; filename=\"RePdLogFile.log\"");
            return response;

        } catch (Exception e) {
            GLog.debug("Error serving log file: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                serveUploadForm("ERROR: Failed to serve log file - " + e.getMessage(), ""));
        }
    }
}