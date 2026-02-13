package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Map;
import java.util.Scanner;

import fi.iki.elonen.NanoHTTPD;

/**
 * Base WebServer implementation that provides common functionality for all platforms.
 * Platform-specific implementations should extend this class and implement platform-specific functionality.
 */
public abstract class BaseWebServer extends NanoHTTPD {
    protected static BaseWebServer instance = null;
    protected String serverAddress = null;
    protected boolean started = false;

    public static boolean isRunning() {
        return instance != null && instance.started;
    }

    public static String getServerAddress() {
        if (instance != null && instance.serverAddress != null) {
            return instance.serverAddress;
        }
        return "http://localhost:8080"; // Default address
    }

    public BaseWebServer(int port) {
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
            onServerStarted();
        }
    }

    /**
     * Called when the server is successfully started
     */
    protected abstract void onServerStarted();

    /**
     * Check if a path represents a directory for the current mod
     */
    protected abstract boolean isDirectory(String path);

    /**
     * Generate the root page HTML
     */
    protected String serveRoot() {
        return generateHtmlWithTemplate("root_template.html",
            "{{GAME_VERSION}}", Utils.format("%s (%d)", GameLoop.version, GameLoop.versionCode),
            "{{MOD_INFO}}", Utils.format("%s (%d)", ModdingMode.activeMod(), ModdingMode.activeModVersion()),
            "{{LEVEL_INFO}}", Dungeon.level != null ? Utils.format("<p>Level: %s</p>", Dungeon.level.levelId) : "");
    }

    /**
     * Generate the file listing page HTML
     */
    protected String serveList() {
        StringBuilder dirContent = new StringBuilder();
        listDir(dirContent, "");

        return generateHtmlWithTemplate("list_template.html",
            "{{MOD_NAME}}", ModdingMode.activeMod(),
            "{{DIRECTORY_CONTENTS}}", dirContent.toString());
    }

    /**
     * Generate HTML for directory listing page
     */
    protected String generateDirectoryListing(String directoryPath) {
        // Generate up one level link
        String upOneLevelLink = "";
        if (!directoryPath.isEmpty()) {
            String upOneLevel = directoryPath.contains("/") ? directoryPath.substring(0, directoryPath.lastIndexOf("/")) : "";
            if (upOneLevel.isEmpty()) {
                upOneLevelLink = "<p><a href=\"/list\">..</a></p>";
            } else {
                upOneLevelLink = Utils.format("<p><a href=\"/fs/%s/\">..</a></p>", upOneLevel);
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
            GLog.debug("Generating upload link for directory: '" + uploadPath + "'");
            String encodedUploadPath;
            try {
                encodedUploadPath = java.net.URLEncoder.encode(uploadPath, "UTF-8");
                GLog.debug("Encoded upload path: '" + encodedUploadPath + "' from original: '" + uploadPath + "'");
            } catch (Exception e) {
                encodedUploadPath = uploadPath; // Fallback if encoding fails
                GLog.debug("Upload path encoding failed, using original: '" + uploadPath + "'");
            }
            dirListing.append(Utils.format("<p>📤 <a href=\"/upload?path=%s\">Upload files to this directory</a></p>", encodedUploadPath));

            // List directories first
            for (String name : directories) {
                String fullPath = directoryPath.isEmpty() ? name : directoryPath + "/" + name;
                dirListing.append(Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>",
                    fullPath, name));
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
                    dirListing.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2));
                } else if (name.toLowerCase().endsWith(".lua")) {
                    // For Lua files, add both download and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirListing.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-lua?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>",
                        fullPath, name, encodedPath2, fullPath));
                } else if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")) {
                    // For image files, add download, preview, and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirListing.append(Utils.format("<p>🖼️ <a href=\"/fs/%s\">%s</a> (<a href=\"/preview-image?file=%s\">preview</a>) (<a href=\"/edit-png?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2, encodedPath2));
                } else {
                    // For non-JSON files, just show download link
                    dirListing.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>",
                        fullPath, name));
                }
            }
        }

        // Prepare replacements for the template
        return generateHtmlWithTemplate("directory_listing_template.html",
            "{{DIRECTORY_PATH}}", directoryPath.isEmpty() ? "/" : directoryPath,
            "{{ENCODED_UPLOAD_PATH}}", getEncodedPath(directoryPath),
            "{{UP_ONE_LEVEL_LINK}}", upOneLevelLink,
            "{{DIRECTORY_LISTING}}", dirListing.toString());
    }

    /**
     * Helper method to get encoded path for URL
     */
    private String getEncodedPath(String path) {
        try {
            return java.net.URLEncoder.encode(path, "UTF-8");
        } catch (Exception e) {
            return path; // Fallback if encoding fails
        }
    }

    /**
     * Generate the file upload form HTML
     */
    protected String serveUploadForm(String message, String currentPath) {
        // Prepare message div
        String messageDiv = "";
        if (message != null && !message.isEmpty()) {
            if (message.startsWith("ERROR:")) {
                messageDiv = "<div class=\"error\">" + message.substring(6) + "</div>";
            } else {
                messageDiv = "<div class=\"success\">" + message + "</div>";
            }
        }

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

        return generateHtmlWithTemplate("upload_form_template.html",
            "{{MESSAGE_DIV}}", messageDiv,
            "{{UPLOAD_FORM_CONTENT}}", uploadFormContent);
    }

    /**
     * Generate the JSON editor page HTML
     */
    protected String serveJsonEditor(String filePath) {
        String uploadPath = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf("/")) : "";

        return generateHtmlWithTemplate("json_editor_template.html",
            "{{UPLOAD_PATH}}", uploadPath,
            "{{FILE_PATH}}", filePath,
            "{{ESCAPED_FILE_PATH}}", javaScriptEscape(filePath));
    }

    /**
     * Generate the Lua editor page HTML
     */
    protected String serveLuaEditor(String filePath) {
        String uploadPath = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf("/")) : "";

        return generateHtmlWithTemplate("lua_editor_template.html",
            "{{UPLOAD_PATH}}", uploadPath,
            "{{FILE_PATH}}", filePath,
            "{{ESCAPED_FILE_PATH}}", javaScriptEscape(filePath));
    }

    /**
     * Generate the "Not Found" error page HTML
     */
    protected String serveNotFound() {
        return generateHtmlWithTemplate("not_found_template.html");
    }

    /**
     * Helper to escape string for use in JavaScript
     */
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
     * Generate HTML from a template with placeholder replacements
     */
    private String generateHtmlWithTemplate(String templateName, String... replacements) {
        try {
            // Use ModdingMode to access the template file from the current mod assets
            InputStream inputStream = ModdingMode.getInputStream("html/" + templateName);

            Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A");
            String template = scanner.hasNext() ? scanner.next() : "";

            // Process replacements (they come in pairs: placeholder, replacement)
            for (int i = 0; i < replacements.length; i += 2) {
                template = template.replace(replacements[i], replacements[i + 1]);
            }

            return template;
        } catch (Exception e) {
            GLog.w("Failed to generate HTML from template: " + templateName + ", error: " + e.getMessage());
            return "<html><body><h1>Template Error</h1><p>Failed to process template: " + templateName + "</p></body></html>";
        }
    }

    /**
     * List contents of a directory for the current mod
     */
    protected abstract String[] listDirectoryContents(String path);

    /**
     * Check if a specific item in a directory is itself a directory
     */
    protected abstract boolean isDirectoryItem(String parentPath, String itemName);

    /**
     * List directory contents with proper formatting
     */
    protected void listDir(StringBuilder msg, String path) {
        GLog.debug("listDir called with path: '" + path + "'");

        // Use the listDirectoryContents method to get direct children
        String[] contents = listDirectoryContents(path);

        GLog.debug("listDirectoryContents returned " + (contents != null ? contents.length : "null") + " items for path: '" + path + "'");

        if (contents != null) {
            // Separate directories and files
            java.util.List<String> directories = new java.util.ArrayList<>();
            java.util.List<String> files = new java.util.ArrayList<>();

            for (String name : contents) {
                GLog.debug("Processing item: '" + name + "' in path: '" + path + "'");
                if (isDirectoryItem(path, name)) {
                    directories.add(name);
                    GLog.debug("Identified '" + name + "' as directory");
                } else {
                    files.add(name);
                    GLog.debug("Identified '" + name + "' as file");
                }
            }

            // Sort directories and files separately
            java.util.Collections.sort(directories);
            java.util.Collections.sort(files);

            msg.append("<div class=\"file-list\">");

            // List directories first
            for (String name : directories) {
                if(path.isEmpty()) {
                    GLog.debug("Generating directory link for root directory: " + name);
                    msg.append(Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>", name, name));
                } else {
                    String fullPath = path + "/" + name; // Proper path for accessing the directory
                    msg.append(Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>", fullPath, name));
                }
            }

            // Then list files
            for (String name : files) {
                if(path.isEmpty()) {
                    if (name.toLowerCase().endsWith(".json")) {
                        String encodedPath1;
                        try {
                            encodedPath1 = java.net.URLEncoder.encode(name, "UTF-8");
                        } catch (Exception e) {
                            encodedPath1 = name; // Fallback if encoding fails
                        }
                        msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>", name, name, encodedPath1, name));
                    } else if (name.toLowerCase().endsWith(".lua")) {
                        String encodedPath1;
                        try {
                            encodedPath1 = java.net.URLEncoder.encode(name, "UTF-8");
                        } catch (Exception e) {
                            encodedPath1 = name; // Fallback if encoding fails
                        }
                        msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-lua?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>", name, name, encodedPath1, name));
                    } else {
                        msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", name, name));
                    }
                } else {
                    String fullPath = path + "/" + name; // Proper path for accessing the file
                    if (name.toLowerCase().endsWith(".json")) {
                        String encodedPath;
                        try {
                            encodedPath = java.net.URLEncoder.encode(fullPath, "UTF-8");
                        } catch (Exception e) {
                            encodedPath = fullPath; // Fallback if encoding fails
                        }
                        // FIXED: Show just the filename, but use full path for href
                        msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>",
                            fullPath, name, encodedPath, fullPath));
                    } else if (name.toLowerCase().endsWith(".lua")) {
                        String encodedPath;
                        try {
                            encodedPath = java.net.URLEncoder.encode(fullPath, "UTF-8");
                        } catch (Exception e) {
                            encodedPath = fullPath; // Fallback if encoding fails
                        }
                        // FIXED: Show just the filename, but use full path for href
                        msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-lua?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>",
                            fullPath, name, encodedPath, fullPath));
                    } else {
                        // FIXED: Show just the filename, but use full path for href
                        msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", fullPath, name));
                    }
                }
            }
            msg.append("</div>");
        } else {
            GLog.debug("listDirectoryContents returned null for path: '" + path + "'");
        }
    }

    protected Response serveFs(String file) {
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
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", serveNotFound());
            }
        }
        // Handle file download requests
        else if(ModdingMode.isResourceExist(file)) {
            GLog.debug("Resource exists check passed for: " + file);

            // Double-check that it's not a directory to avoid attempting to read directory as file
            boolean isDir = isDirectory(file);
            GLog.debug("isDirectory(" + file + ") returned: " + isDir);

            if (isDir) {
                GLog.debug("Resource exists but is a directory, serving directory listing instead: " + file);
                try {
                    String html = generateDirectoryListing(file);
                    return newFixedLengthResponse(Response.Status.OK, "text/html", html);
                } catch (Exception e) {
                    GLog.w("Error in generateDirectoryListing: " + e.getMessage());
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", serveNotFound());
                }
            } else {
                GLog.debug("Serving file: " + file);
                try {
                    // For all other files, serve as download (this is the default for non-JSON/Lua files)
                    // Actual download parameter handling will be done in the actual serve method that receives the session
                    InputStream fis = ModdingMode.getInputStream(file);
                    Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
                    response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
                    return response;
                } catch (Exception e) {
                    GLog.w("Error serving file " + file + ": " + e.getMessage());
                    // If it's a directory causing the error, serve it as a directory instead
                    if (e.toString().contains("Is a directory")) {
                        GLog.debug("File access failed because it's a directory, serving as directory: " + file);
                        try {
                            String html = generateDirectoryListing(file);
                            return newFixedLengthResponse(Response.Status.OK, "text/html", html);
                        } catch (Exception dirE) {
                            GLog.w("Error in fallback directory listing: " + dirE.getMessage());
                            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", serveNotFound());
                        }
                    }
                    // Otherwise return error
                    return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", serveNotFound());
                }
            }
        } else {
            // File or directory doesn't exist
            GLog.debug("File or directory not found: " + file);
            return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", serveNotFound());
        }
    }

    protected Response handleFileUpload(IHTTPSession session) {
        try {
            GLog.debug("=== STARTING FILE UPLOAD PROCESS ===");
            GLog.debug("Handling file upload, URI: " + session.getUri());

            // Check if we're trying to upload to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                GLog.debug("Upload blocked - attempt to upload to main 'Remixed' mod");
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", serveUploadForm("ERROR: Upload to the main 'Remixed' mod is disabled for security reasons.", ""));
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
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", serveUploadForm("ERROR: No file uploaded.", path));
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

            // Reinitialize the mod cache to reflect the newly uploaded file
            GLog.debug("Reinitializing mod cache after file upload");
            // FileSystem.reinitModCache(); // Not available on Android platform

            GLog.debug("=== FILE UPLOAD COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "text/html", serveUploadForm("File uploaded successfully to: " + fullPath, path));

        } catch (Exception e) {
            GLog.debug("=== FILE UPLOAD FAILED ===");
            GLog.debug("Upload error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", serveUploadForm("ERROR: Failed to upload file - " + e.getMessage(), ""));
        }
    }

    /**
     * Handle saving JSON content to a file
     */
    protected Response handleJsonSave(IHTTPSession session) {
        try {
            GLog.debug("Handling JSON save request");

            // Use the same approach as file upload, but for raw JSON data
            Map<String, String> files = new java.util.HashMap<>();

            // This will parse the body and handle both form data and raw data
            session.parseBody(files);

            // Try to get the raw JSON from postData
            String jsonString = files.get("postData");

            // If postData is null, the content may be in the input stream directly
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

            // Reinitialize the mod cache to reflect the newly saved file
            GLog.debug("Reinitializing mod cache after JSON save");
            // FileSystem.reinitModCache(); // Not available on Android platform

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
     * Handle saving Lua content to a file
     */
    protected Response handleLuaSave(IHTTPSession session) {
        try {
            GLog.debug("Handling Lua save request");

            // Use the same approach as JSON save, but for raw Lua data
            Map<String, String> files = new java.util.HashMap<>();

            // This will parse the body and handle both form data and raw data
            session.parseBody(files);

            // Try to get the raw Lua from postData
            String luaString = files.get("postData");

            // If postData is null, the content may be in the input stream directly
            if (luaString == null || luaString.isEmpty()) {
                // Get query parameters in case the data was sent as query parameters
                String body = session.getQueryParameterString();
                if (body != null && !body.isEmpty()) {
                    luaString = java.net.URLDecoder.decode(body, "UTF-8");
                }
            }

            // If still null, try to read from the input stream directly
            if (luaString == null || luaString.isEmpty()) {
                GLog.debug("Reading Lua from input stream");
                java.util.Map<String, java.util.List<String>> parms = session.getParameters();

                if (!parms.isEmpty()) {
                    for (java.util.Map.Entry<String, java.util.List<String>> entry : parms.entrySet()) {
                        // Look for Lua-like strings in parameters
                        for (String value : entry.getValue()) {
                            if (value.trim().startsWith("local ") || value.trim().startsWith("function ") || 
                                value.trim().startsWith("--") || value.trim().contains("=")) { // Basic Lua indicators
                                luaString = value;
                                break;
                            }
                        }
                        if (luaString != null) break;
                    }
                }

                // If still not found, try direct input stream reading as last resort
                if (luaString == null || luaString.isEmpty()) {
                    try {
                        // Create a smaller buffer and read with timeout
                        byte[] buffer = new byte[4096];
                        java.io.InputStream inputStream = session.getInputStream();

                        // Mark and reset approach to avoid issues with already-read streams
                        if (inputStream.markSupported()) {
                            inputStream.mark(4096);
                            int bytesRead = inputStream.read(buffer);
                            if (bytesRead > 0) {
                                luaString = new String(buffer, 0, bytesRead, "UTF-8");
                            } else {
                                inputStream.reset(); // Reset to marked position
                            }
                        }
                    } catch (Exception e) {
                        GLog.debug("Error reading from input stream: " + e.getMessage());
                    }
                }
            }

            if (luaString == null || luaString.isEmpty()) {
                GLog.debug("No Lua data found in request");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Empty request body\"}");
            }

            GLog.debug("Received Lua: " + luaString.substring(0, Math.min(200, luaString.length())) + "...");

            // Parse the data to extract filePath and content
            // For Lua, we expect the data to be in the format: {"filePath":"path","content":"lua code here"}
            org.json.JSONObject jsonData = new org.json.JSONObject(luaString);
            String filePath = jsonData.getString("filePath");
            String content = jsonData.getString("content");

            GLog.debug("Saving Lua to: " + filePath);

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
            GLog.debug("Writing Lua content to file");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile)) {
                fos.write(content.getBytes("UTF-8"));
            }

            // Reinitialize the mod cache to reflect the newly saved file
            GLog.debug("Reinitializing mod cache after Lua save");
            // FileSystem.reinitModCache(); // Not available on Android platform

            GLog.debug("=== LUA SAVE COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "application/json",
                "{\"success\":true, \"message\":\"File saved successfully to: " + fullPath + "\"}");

        } catch (org.json.JSONException e) {
            GLog.debug("=== JSON PARSING ERROR ===");
            GLog.debug("JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"error\":\"Invalid JSON format in request: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            GLog.debug("=== LUA SAVE FAILED ===");
            GLog.debug("Lua save error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                "{\"error\":\"Failed to save Lua file: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Serve the game log file for download
     */
    protected Response serveLog() {
        try {
            // Get the log file
            File logFile = FileSystem.getExternalStorageFile("RePdLogFile.log");

            if (!logFile.exists()) {
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", serveNotFound());
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

    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        GLog.debug("WebServer: " + uri);

        // Handle debugging endpoints first
        if (uri.startsWith("/debug/change_level")) {
            return DebugEndpoints.handleDebugChangeLevel(session);
        } else if (uri.startsWith("/debug/create_mob")) {
            return DebugEndpoints.handleDebugCreateMob(session);
        } else if (uri.startsWith("/debug/create_item")) {
            return DebugEndpoints.handleDebugCreateItem(session);
        } else if (uri.startsWith("/debug/change_map")) {
            return DebugEndpoints.handleDebugChangeMap(session);
        } else if (uri.startsWith("/debug/give_item")) {
            return DebugEndpoints.handleDebugGiveItem(session);
        } else if (uri.startsWith("/debug/spawn_at")) {
            return DebugEndpoints.handleDebugSpawnAt(session);
        } else if (uri.startsWith("/debug/start_game")) {
            return DebugEndpoints.handleDebugStartGame(session);
        } else if (uri.startsWith("/debug/get_game_state")) {
            return DebugEndpoints.handleDebugGetGameState(session);
        } else if (uri.startsWith("/debug/get_hero_info")) {
            return DebugEndpoints.handleDebugGetHeroInfo(session);
        } else if (uri.startsWith("/debug/get_level_info")) {
            return DebugEndpoints.handleDebugGetLevelInfo(session);
        } else if (uri.startsWith("/debug/get_mobs")) {
            return DebugEndpoints.handleDebugGetMobs(session);
        } else if (uri.startsWith("/debug/get_items")) {
            return DebugEndpoints.handleDebugGetItems(session);
        } else if (uri.startsWith("/debug/get_inventory")) {
            return DebugEndpoints.handleDebugGetInventory(session);
        } else if (uri.startsWith("/debug/set_hero_stat")) {
            return DebugEndpoints.handleDebugSetHeroStat(session);
        } else if (uri.startsWith("/debug/kill_mob")) {
            return DebugEndpoints.handleDebugKillMob(session);
        } else if (uri.startsWith("/debug/remove_item")) {
            return DebugEndpoints.handleDebugRemoveItem(session);
        } else if (uri.startsWith("/debug/reset_level")) {
            return DebugEndpoints.handleDebugResetLevel(session);
        } else if (uri.startsWith("/debug/get_dungeon_seed")) {
            return DebugEndpoints.handleDebugGetDungeonSeed(session);
        } else if (uri.startsWith("/debug/set_dungeon_seed")) {
            return DebugEndpoints.handleDebugSetDungeonSeed(session);
        } else if (uri.startsWith("/debug/get_tile_info")) {
            return DebugEndpoints.handleDebugGetTileInfo(session);
        } else if (uri.startsWith("/debug/handle_cell")) {
            return DebugEndpoints.handleDebugHandleCell(session);
        } else if (uri.startsWith("/debug/cast_spell")) {
            return DebugEndpoints.handleDebugCastSpell(session);
        } else if (uri.startsWith("/debug/cast_spell_on_target")) {
            return DebugEndpoints.handleDebugCastSpellOnTarget(session);
        } else if (uri.startsWith("/debug/get_available_spells")) {
            return DebugEndpoints.handleDebugGetAvailableSpells(session);
        }

        // Original serve method logic continues here...
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

            if(uri.startsWith("/edit-lua")) {
                // Extract file path from query parameters
                String filePath = "";
                GLog.debug("Edit Lua URI: " + uri);

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

                GLog.debug("Lua file to edit: '" + filePath + "'");
                if (!filePath.isEmpty()) {
                    return newFixedLengthResponse(Response.Status.OK, "text/html", serveLuaEditor(filePath));
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", serveNotFound());
                }
            }

            if(uri.startsWith("/preview-image")) {
                // Extract file path from query parameters
                String filePath = "";
                GLog.debug("Preview image URI: " + uri);

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

                GLog.debug("Image to preview: '" + filePath + "'");
                if (!filePath.isEmpty()) {
                    return serveImagePreview(filePath);
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", serveNotFound());
                }
            }

            if(uri.startsWith("/edit-png")) {
                // Extract file path from query parameters
                String filePath = "";
                GLog.debug("Edit PNG URI: " + uri);

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

                GLog.debug("PNG file to edit: '" + filePath + "'");
                if (!filePath.isEmpty()) {
                    return servePngEditor(filePath);
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", serveNotFound());
                }
            }

            if(uri.equals("/log")) {
                return serveLog();
            }

            if(uri.startsWith("/fs/")) {
                // Check for download parameter
                String file = uri.substring(4);
                String downloadParam = session.getParameters().get("download") != null ?
                    session.getParameters().get("download").get(0) : null;

                if ("true".equals(downloadParam) || "1".equals(downloadParam)) {
                    // Force download of file regardless of type
                    try {
                        InputStream fis = ModdingMode.getInputStream(file);
                        Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
                        response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
                        return response;
                    } catch (Exception e) {
                        GLog.w("Error serving raw file " + file + ": " + e.getMessage());
                        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", serveNotFound());
                    }
                }

                return serveFs(file);
            }

            // Handle PixelCraft editor requests
            if(uri.startsWith("/web/pixelcraft/")) {
                // Extract the specific file path after /web/pixelcraft/
                String file = uri.substring("/web/pixelcraft/".length());
                if (file.isEmpty()) {
                    file = "index.html"; // Default to index.html if no specific file requested
                }

                try {
                    // Try to get the file from assets/web/pixelcraft/
                    InputStream fis = ModdingMode.getInputStream("web/pixelcraft/" + file);

                    // Determine the content type based on file extension
                    String mimeType = "application/octet-stream";
                    if (file.endsWith(".html")) {
                        mimeType = "text/html";
                    } else if (file.endsWith(".css")) {
                        mimeType = "text/css";
                    } else if (file.endsWith(".js")) {
                        mimeType = "application/javascript";
                    } else if (file.endsWith(".png")) {
                        mimeType = "image/png";
                    } else if (file.endsWith(".jpg") || file.endsWith(".jpeg")) {
                        mimeType = "image/jpeg";
                    } else if (file.endsWith(".gif")) {
                        mimeType = "image/gif";
                    } else if (file.endsWith(".json")) {
                        mimeType = "application/json";
                    } else if (file.endsWith(".txt")) {
                        mimeType = "text/plain";
                    }

                    Response response = newChunkedResponse(Response.Status.OK, mimeType, fis);
                    return response;
                } catch (Exception e) {
                    GLog.w("Error serving PixelCraft file " + file + ": " + e.getMessage());
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", serveNotFound());
                }
            }

            if(uri.startsWith("/raw/")) {
                // Handle raw file content requests (for editor content loading)
                String file = uri.substring(5); // "/raw/".length() = 5
                try {
                    InputStream fis = ModdingMode.getInputStream(file);
                    Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
                    return response;
                } catch (Exception e) {
                    GLog.w("Error serving raw file " + file + ": " + e.getMessage());
                    return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", serveNotFound());
                }
            }

            if(uri.equals("/debug-list")) {
                return serveDebugList("");
            } else if(uri.startsWith("/debug-list/")) {
                String path = uri.substring(12); // "/debug-list/".length() = 12
                return serveDebugList(path);
            }
        } else if (session.getMethod() == Method.POST) {
            if(uri.startsWith("/upload")) {
                return handleFileUpload(session);
            }

            if(uri.startsWith("/api/save-json")) {
                return handleJsonSave(session);
            }

            if(uri.startsWith("/api/save-lua")) {
                return handleLuaSave(session);
            }

            if (uri.equals("/api/save_texture") && session.getMethod() == Method.POST) {
                return handleTextureSave(session);
            }
        }

        // Add handling for GET requests to the texture API
        if (session.getMethod() == Method.GET) {
            if (uri.startsWith("/api/get_texture")) {
                // Extract file path from query parameters
                String filePath = "";
                String query = session.getQueryParameterString();

                if (query != null && !query.isEmpty()) {
                    String[] params = query.split("&");
                    for (String param : params) {
                        if (param.startsWith("file=")) {
                            filePath = param.substring(5); // Remove "file=" prefix
                            try {
                                filePath = java.net.URLDecoder.decode(filePath, "UTF-8");
                            } catch (Exception e) {
                                // If decoding fails, use the path as is
                            }
                            if (filePath == null) {
                                filePath = "";
                            }
                            break;
                        }
                    }
                }

                if (!filePath.isEmpty()) {
                    return handleTextureGet(filePath);
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", serveNotFound());
                }
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html",
            serveNotFound());
    }

    /**
     * Serve image preview page (needs to be implemented in subclasses)
     */
    protected Response serveImagePreview(String filePath) {
        // This method should be implemented in the platform-specific subclass
        // For now, return a not found response
        return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, "text/html", serveNotFound());
    }

    /**
     * Serve PNG editor page (needs to be implemented in subclasses)
     */
    protected Response servePngEditor(String filePath) {
        // This method should be implemented in the platform-specific subclass
        // For now, return a not found response
        return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, "text/html", serveNotFound());
    }

    /**
     * Debug endpoint to test ModdingMode.listResources directly
     */
    protected Response serveDebugList(String path) {
        // This method should be implemented in the platform-specific subclass
        // For now, return a not found response
        return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, "text/html", serveNotFound());
    }

    /**
     * Handle saving texture content from PixelCraft editor
     */
    protected Response handleTextureSave(IHTTPSession session) {
        // This method should be implemented in the platform-specific subclass
        // For now, return a not found response
        return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, "application/json", "{}");
    }

    /**
     * Handle getting texture content for PixelCraft editor
     */
    protected Response handleTextureGet(String filePath) {
        // This method should be implemented in the platform-specific subclass
        // For now, return a not found response
        return newFixedLengthResponse(Response.Status.NOT_IMPLEMENTED, "application/json", "{}");
    }

    /**
     * Handle debug endpoint to change player's level/depth
     */
    protected Response handleDebugChangeLevel(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            int level = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("level=")) {
                        String levelStr = param.substring(6); // Remove "level=" prefix
                        try {
                            level = Integer.parseInt(java.net.URLDecoder.decode(levelStr, "UTF-8"));
                        } catch (Exception e) {
                            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                                "{\"error\":\"Invalid level parameter\"}");
                        }
                        break;
                    }
                }
            }

            if (level < 0) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid level parameter\"}");
            }

            // Attempt to change the dungeon level
            try {
                // Import necessary classes
                Class<?> dungeonClass = Class.forName("com.watabou.pixeldungeon.Dungeon");
                
                // Check if game state is initialized
                Object currentLevel = dungeonClass.getField("level").get(null);
                if (currentLevel == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        "{\"error\":\"Game state not initialized - start a game first\"}");
                }
                
                // Get the new level using DungeonGenerator
                Class<?> dungeonGeneratorClass = Class.forName("com.nyrds.pixeldungeon.utils.DungeonGenerator");
                Class<?> positionClass = Class.forName("com.nyrds.pixeldungeon.utils.DungeonGenerator$Position");
                
                // Create a position object for the level
                Object position = positionClass.newInstance();
                java.lang.reflect.Field levelIdField = positionClass.getDeclaredField("levelId");
                levelIdField.setAccessible(true);
                levelIdField.set(position, String.valueOf(level));
                
                // Use the createLevel method
                Object newLevel = dungeonGeneratorClass.getDeclaredMethod("createLevel", positionClass).invoke(null, position);

                // Change the level
                dungeonClass.getDeclaredMethod("switchLevel", Class.forName("com.watabou.pixeldungeon.levels.Level"), int[].class)
                    .invoke(null, newLevel, new int[]{1, 1}); // Start at position 1,1
                
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Changed to level %d\",\"level\":%d}", level, level));
            } catch (Exception e) {
                GLog.w("Error changing level: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to change level: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugChangeLevel: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    /**
     * Handle debug endpoint to create a mob at the current level
     */
    protected Response handleDebugCreateMob(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String mobType = null;
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        mobType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (mobType == null || mobType.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing mob type parameter\"}");
            }

            // Attempt to create the mob
            try {
                // Import necessary classes
                Class<?> dungeonClass = Class.forName("com.watabou.pixeldungeon.Dungeon");
                
                // Check if game state is initialized
                Object currentLevel = dungeonClass.getField("level").get(null);
                if (currentLevel == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        "{\"error\":\"Game state not initialized - start a game first\"}");
                }
                
                Class<?> mobFactoryClass = Class.forName("com.nyrds.pixeldungeon.mobs.common.MobFactory");
                Class<?> actorClass = Class.forName("com.watabou.pixeldungeon.actors.Actor");

                // Generate random coordinates if not specified
                if (x < 0 || y < 0) {
                    // Find a random free cell
                    java.lang.reflect.Method getEmptyCellMethod = currentLevel.getClass().getMethod("getEmptyCell");
                    int[] coords = (int[]) getEmptyCellMethod.invoke(currentLevel);
                    x = coords[0];
                    y = coords[1];
                }

                // Create the mob using the factory
                Object mob = mobFactoryClass.getDeclaredMethod("mobByName", String.class).invoke(null, mobType);
                
                // Set the mob's position
                java.lang.reflect.Field posField = mob.getClass().getField("pos");
                posField.set(mob, x + y * (Integer) currentLevel.getClass().getField("width").get(currentLevel));
                
                // Add the mob to the game
                actorClass.getDeclaredMethod("occupyCell", Class.forName("com.watabou.pixeldungeon.actors.Char")).invoke(null, mob);
                
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Created mob '%s' at (%d,%d)\",\"mobType\":\"%s\",\"x\":%d,\"y\":%d}", 
                        mobType, x, y, mobType, x, y));
            } catch (Exception e) {
                GLog.w("Error creating mob: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to create mob: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugCreateMob: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    /**
     * Handle debug endpoint to create an item at the current level
     */
    protected Response handleDebugCreateItem(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String itemType = null;
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        itemType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (itemType == null || itemType.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing item type parameter\"}");
            }

            // Attempt to create the item
            try {
                // Import necessary classes
                Class<?> dungeonClass = Class.forName("com.watabou.pixeldungeon.Dungeon");
                
                // Check if game state is initialized
                Object currentLevel = dungeonClass.getField("level").get(null);
                if (currentLevel == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        "{\"error\":\"Game state not initialized - start a game first\"}");
                }
                
                Class<?> itemFactoryClass = Class.forName("com.nyrds.pixeldungeon.items.common.ItemFactory");

                // Generate random coordinates if not specified
                if (x < 0 || y < 0) {
                    // Find a random free cell
                    java.lang.reflect.Method getEmptyCellMethod = currentLevel.getClass().getMethod("getEmptyCell");
                    int[] coords = (int[]) getEmptyCellMethod.invoke(currentLevel);
                    x = coords[0];
                    y = coords[1];
                }

                // Create the item using the factory
                Object item = itemFactoryClass.getDeclaredMethod("itemByName", String.class).invoke(null, itemType);

                // Drop the item at the specified location
                java.lang.reflect.Method dropMethod = currentLevel.getClass().getMethod("drop",
                    Class.forName("com.watabou.pixeldungeon.items.Item"), int.class);
                java.lang.reflect.Method getWidthMethod = currentLevel.getClass().getMethod("getWidth");
                int width = (Integer) getWidthMethod.invoke(currentLevel);
                dropMethod.invoke(currentLevel, item, x + y * width);
                
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Created item '%s' at (%d,%d)\",\"itemType\":\"%s\",\"x\":%d,\"y\":%d}", 
                        itemType, x, y, itemType, x, y));
            } catch (Exception e) {
                GLog.w("Error creating item: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to create item: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugCreateItem: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    /**
     * Handle debug endpoint to change the current map/level layout
     */
    protected Response handleDebugChangeMap(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String mapType = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        mapType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    }
                }
            }

            if (mapType == null || mapType.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing map type parameter\"}");
            }

            // Attempt to change the map
            try {
                // Import necessary classes
                Class<?> dungeonClass = Class.forName("com.watabou.pixeldungeon.Dungeon");
                
                // Check if game state is initialized
                Object currentLevel = dungeonClass.getField("level").get(null);
                if (currentLevel == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        "{\"error\":\"Game state not initialized - start a game first\"}");
                }
                
                // Get the current depth
                int depth = (Integer) dungeonClass.getField("depth").get(null);

                // For map type changes, we'll use the DungeonGenerator approach
                // This is a simplified approach - we'll create a position based on the map type
                Class<?> dungeonGeneratorClass = Class.forName("com.nyrds.pixeldungeon.utils.DungeonGenerator");
                Class<?> positionClass = Class.forName("com.nyrds.pixeldungeon.utils.DungeonGenerator$Position");
                
                // Create a position object for the level
                Object position = positionClass.newInstance();
                java.lang.reflect.Field levelIdField = positionClass.getDeclaredField("levelId");
                levelIdField.setAccessible(true);
                
                // Map the type to a known level ID
                String levelId;
                switch(mapType.toLowerCase()) {
                    case "sewer":
                    case "sewers":
                        levelId = "1"; // Sewer level
                        break;
                    case "prison":
                        levelId = "4"; // Prison level
                        break;
                    case "caves":
                    case "cave":
                        levelId = "7"; // Caves level
                        break;
                    case "city":
                        levelId = "10"; // City level
                        break;
                    case "halls":
                        levelId = "13"; // Halls level
                        break;
                    default:
                        levelId = "1"; // Default to sewer level
                        break;
                }
                
                levelIdField.set(position, levelId);
                
                // Use the createLevel method
                Object newLevel = dungeonGeneratorClass.getDeclaredMethod("createLevel", positionClass).invoke(null, position);

                if (newLevel == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Unknown map type: %s\"}", mapType));
                }
                
                // Switch to the new level
                dungeonClass.getDeclaredMethod("switchLevel", 
                    Class.forName("com.watabou.pixeldungeon.levels.Level"), int[].class)
                    .invoke(null, newLevel, new int[]{1, 1});
                
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Changed map to type '%s'\",\"mapType\":\"%s\"}", 
                        mapType, mapType));
            } catch (Exception e) {
                GLog.w("Error changing map: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to change map: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugChangeMap: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    /**
     * Handle debug endpoint to give an item to the hero
     */
    protected Response handleDebugGiveItem(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String itemType = null;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("type=")) {
                        itemType = java.net.URLDecoder.decode(param.substring(5), "UTF-8"); // Remove "type=" prefix
                    }
                }
            }

            if (itemType == null || itemType.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing item type parameter\"}");
            }

            // Attempt to give the item to the hero
            try {
                // Import necessary classes
                Class<?> dungeonClass = Class.forName("com.watabou.pixeldungeon.Dungeon");
                
                // Check if game state is initialized
                Object currentHero = dungeonClass.getField("hero").get(null);
                if (currentHero == null || currentHero.toString().contains("DUMMY_HERO")) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        "{\"error\":\"Hero not initialized - start a game first\"}");
                }
                
                Class<?> itemFactoryClass = Class.forName("com.nyrds.pixeldungeon.items.common.ItemFactory");

                // Create the item using the factory
                Object item = itemFactoryClass.getDeclaredMethod("itemByName", String.class).invoke(null, itemType);

                // Give the item to the hero
                java.lang.reflect.Method getBelongingsMethod = currentHero.getClass().getMethod("getBelongings");
                Object belongings = getBelongingsMethod.invoke(currentHero);
                java.lang.reflect.Method collectMethod = belongings.getClass().getMethod("collect",
                    Class.forName("com.watabou.pixeldungeon.items.Item"));
                collectMethod.invoke(belongings, item);
                
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Gave item '%s' to hero\",\"itemType\":\"%s\"}", 
                        itemType, itemType));
            } catch (Exception e) {
                GLog.w("Error giving item to hero: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to give item to hero: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugGiveItem: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    /**
     * Handle debug endpoint to spawn an entity at specific coordinates
     */
    protected Response handleDebugSpawnAt(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String entityType = null;
            String entityValue = null;
            int x = -1, y = -1;

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("entity=")) {
                        entityType = java.net.URLDecoder.decode(param.substring(7), "UTF-8"); // Remove "entity=" prefix
                    } else if (param.startsWith("value=")) {
                        entityValue = java.net.URLDecoder.decode(param.substring(6), "UTF-8"); // Remove "value=" prefix
                    } else if (param.startsWith("x=")) {
                        try {
                            x = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "x=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    } else if (param.startsWith("y=")) {
                        try {
                            y = Integer.parseInt(java.net.URLDecoder.decode(param.substring(2), "UTF-8")); // Remove "y=" prefix
                        } catch (NumberFormatException e) {
                            // Ignore invalid coordinate
                        }
                    }
                }
            }

            if (entityType == null || entityType.isEmpty() || entityValue == null || entityValue.isEmpty()) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing entity type or value parameter\"}");
            }

            if (x < 0 || y < 0) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Missing or invalid coordinates\"}");
            }

            // Attempt to spawn the entity
            try {
                Class<?> dungeonClass = Class.forName("com.watabou.pixeldungeon.Dungeon");
                
                // Check if game state is initialized
                Object currentLevel = dungeonClass.getField("level").get(null);
                if (currentLevel == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        "{\"error\":\"Game state not initialized - start a game first\"}");
                }
                
                int levelWidth = (Integer) currentLevel.getClass().getField("width").get(currentLevel);
                
                int cellPos = x + y * levelWidth;
                
                if ("mob".equalsIgnoreCase(entityType)) {
                    // Spawn a mob
                    Class<?> mobFactoryClass = Class.forName("com.nyrds.pixeldungeon.mobs.common.MobFactory");
                    Class<?> actorClass = Class.forName("com.watabou.pixeldungeon.actors.Actor");

                    Object mob = mobFactoryClass.getDeclaredMethod("mobByName", String.class).invoke(null, entityValue);

                    // Set the mob's position
                    java.lang.reflect.Field posField = mob.getClass().getField("pos");
                    posField.set(mob, cellPos);

                    // Add the mob to the game
                    actorClass.getDeclaredMethod("occupyCell", Class.forName("com.watabou.pixeldungeon.actors.Char")).invoke(null, mob);
                    
                    return newFixedLengthResponse(Response.Status.OK, "application/json",
                        String.format("{\"success\":true,\"message\":\"Spawned mob '%s' at (%d,%d)\",\"entityType\":\"%s\",\"entityValue\":\"%s\",\"x\":%d,\"y\":%d}", 
                            entityValue, x, y, entityType, entityValue, x, y));
                } else if ("item".equalsIgnoreCase(entityType)) {
                    // Spawn an item
                    Class<?> itemFactoryClass = Class.forName("com.nyrds.pixeldungeon.items.common.ItemFactory");

                    Object item = itemFactoryClass.getDeclaredMethod("itemByName", String.class).invoke(null, entityValue);

                    // Drop the item at the specified location
                    java.lang.reflect.Method dropMethod = currentLevel.getClass().getMethod("drop",
                        Class.forName("com.watabou.pixeldungeon.items.Item"), int.class);
                    dropMethod.invoke(currentLevel, item, cellPos);
                    
                    return newFixedLengthResponse(Response.Status.OK, "application/json",
                        String.format("{\"success\":true,\"message\":\"Spawned item '%s' at (%d,%d)\",\"entityType\":\"%s\",\"entityValue\":\"%s\",\"x\":%d,\"y\":%d}", 
                            entityValue, x, y, entityType, entityValue, x, y));
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Unknown entity type: %s\"}", entityType));
                }
            } catch (Exception e) {
                GLog.w("Error spawning entity: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to spawn entity: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugSpawnAt: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }

    /**
     * Handle debug endpoint to start a new game
     */
    protected Response handleDebugStartGame(IHTTPSession session) {
        try {
            String query = session.getQueryParameterString();
            String heroClass = "WARRIOR"; // Default hero class (using enum name)
            int difficulty = 0; // Default difficulty

            if (query != null && !query.isEmpty()) {
                String[] params = query.split("&");
                for (String param : params) {
                    if (param.startsWith("class=")) {
                        heroClass = java.net.URLDecoder.decode(param.substring(6), "UTF-8").toUpperCase(); // Remove "class=" prefix and convert to uppercase
                    } else if (param.startsWith("difficulty=")) {
                        try {
                            difficulty = Integer.parseInt(java.net.URLDecoder.decode(param.substring(11), "UTF-8")); // Remove "difficulty=" prefix
                        } catch (NumberFormatException e) {
                            // Use default difficulty if parsing fails
                        }
                    }
                }
            }

            // Attempt to start a new game
            try {
                Class<?> gameControlClass = Class.forName("com.nyrds.pixeldungeon.utils.GameControl");
                Class<?> heroClassEnum = Class.forName("com.watabou.pixeldungeon.actors.hero.HeroClass");
                
                // Get the hero class enum value
                Object heroClassValue = null;
                Object[] enumConstants = (Object[]) heroClassEnum.getEnumConstants();
                for (Object constant : enumConstants) {
                    if (constant.toString().equals(heroClass)) {
                        heroClassValue = constant;
                        break;
                    }
                }
                
                if (heroClassValue == null) {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                        String.format("{\"error\":\"Unknown hero class: %s. Valid classes: WARRIOR, MAGE, ROGUE, HUNTRESS\"}", heroClass));
                }
                
                // Call the startNewGame method
                gameControlClass.getDeclaredMethod("startNewGame", String.class, int.class, boolean.class)
                    .invoke(null, heroClass, difficulty, false);
                
                return newFixedLengthResponse(Response.Status.OK, "application/json",
                    String.format("{\"success\":true,\"message\":\"Started new game with %s\",\"heroClass\":\"%s\",\"difficulty\":%d}", 
                        heroClass, heroClass, difficulty));
            } catch (Exception e) {
                GLog.w("Error starting game: " + e.getMessage());
                return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                    String.format("{\"error\":\"Failed to start game: %s\"}", e.getMessage()));
            }
        } catch (Exception e) {
            GLog.w("Error in handleDebugStartGame: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                String.format("{\"error\":\"Internal error: %s\"}", e.getMessage()));
        }
    }
}