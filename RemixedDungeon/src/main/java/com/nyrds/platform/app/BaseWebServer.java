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
                    dirListing.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-lua?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2));
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

            if (inputStream != null) {
                java.util.Scanner scanner = new java.util.Scanner(inputStream, "UTF-8").useDelimiter("\\A");
                String template = scanner.hasNext() ? scanner.next() : "";

                // Process replacements (they come in pairs: placeholder, replacement)
                for (int i = 0; i < replacements.length; i += 2) {
                    template = template.replace(replacements[i], replacements[i + 1]);
                }

                return template;
            } else {
                GLog.w("Template not found: " + templateName + " in current mod: " + ModdingMode.activeMod());
                return "<html><body><h1>Template Error</h1><p>Template not found: " + templateName + "</p></body></html>";
            }
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
                    // Check if it's a Lua file, redirect to Lua editor
                    if (file.toLowerCase().endsWith(".lua")) {
                        String html = serveLuaEditor(file);
                        return newFixedLengthResponse(Response.Status.OK, "text/html", html);
                    }
                    // Check if it's a JSON file, redirect to JSON editor
                    else if (file.toLowerCase().endsWith(".json")) {
                        String html = serveJsonEditor(file);
                        return newFixedLengthResponse(Response.Status.OK, "text/html", html);
                    }
                    // For other files, serve as download
                    else {
                        InputStream fis = ModdingMode.getInputStream(file);
                        Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
                        response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
                        return response;
                    }
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
            FileSystem.reinitModCache();

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
            FileSystem.reinitModCache();

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
            try (java.io.FileOutputStream fos = java.io.FileOutputStream(destFile)) {
                fos.write(content.getBytes("UTF-8"));
            }

            // Reinitialize the mod cache to reflect the newly saved file
            GLog.debug("Reinitializing mod cache after Lua save");
            FileSystem.reinitModCache();

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
            java.io.FileInputStream fis = new java.io.FileInputStream(logFile);

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