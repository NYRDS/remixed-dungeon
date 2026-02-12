package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * WebServer implementation for desktop platforms.
 * This provides the same API as the Android WebServer and actually starts a server.
 */
public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }

    @Override
    protected void onServerStarted() {
        // Desktop server started without special handling needed
    }

    // Template loading and replacement methods for desktop
    private String loadTemplate(String templateName) {
        try {
            // Use the new FilesystemAccess abstraction to get the input stream
            java.io.InputStream inputStream = FilesystemAccess.getInputStream("html/" + templateName);

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
                GLog.w("Template not found: " + templateName);
                // Provide a default template for error cases
                return "<!DOCTYPE html><html><head><title>Template Error</title></head><body><h1>Template Error</h1><p>Template not found: " + templateName + "</p></body></html>";
            }
        } catch (Exception e) {
            GLog.w("Failed to load template: " + templateName + ", error: " + e.getMessage());
            e.printStackTrace();
            // Provide a default template for error cases
            return "<!DOCTYPE html><html><head><title>Template Error</title></head><body><h1>Template Error</h1><p>Failed to load template: " + templateName + "</p></body></html>";
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
    public String serveRoot() {
        String template = loadTemplate("root_template.html");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("GAME_VERSION", com.watabou.pixeldungeon.utils.Utils.format("%s (%d)", GameLoop.version, GameLoop.versionCode));
        replacements.put("MOD_INFO", com.watabou.pixeldungeon.utils.Utils.format("%s (%d)", ModdingMode.activeMod(), ModdingMode.activeModVersion()));

        String levelInfo = GameLoop.instance() != null && Dungeon.level != null ?
            com.watabou.pixeldungeon.utils.Utils.format("<p>Level: %s</p>", Dungeon.level.levelId) :
            "standalone mode";
        replacements.put("LEVEL_INFO", levelInfo);

        return replacePlaceholders(template, replacements);
    }

    @Override
    public String serveList() {
        String template = loadTemplate("list_template.html");

        // We need to generate directory contents dynamically (for root directory "")
        StringBuilder dirContent = new StringBuilder();

        // List directory contents with directories first (same logic as generateDirectoryListing)
        String[] contents = listDirectoryContents("");

        if (contents != null) {
            // Separate directories and files
            java.util.List<String> directories = new java.util.ArrayList<>();
            java.util.List<String> files = new java.util.ArrayList<>();

            for (String name : contents) {
                if (isDirectoryItem("", name)) {
                    directories.add(name);
                } else {
                    files.add(name);
                }
            }

            // Sort directories and files separately
            java.util.Collections.sort(directories);
            java.util.Collections.sort(files);


            // List directories first
            for (String name : directories) {
                String fullPath = name; // For root, path is just the name
                dirContent.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>", fullPath, name));
            }
            // Then list files
            for (String name : files) {
                String fullPath = name; // For root, path is just the name
                if (name.toLowerCase().endsWith(".json")) {
                    // For JSON files, add both download and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirContent.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>", fullPath, name, encodedPath2));
                } else if (name.toLowerCase().endsWith(".lua")) {
                    // For Lua files, add both download and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirContent.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-lua?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>", fullPath, name, encodedPath2, fullPath));
                } else if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")) {
                    // For image files, add download, preview, and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirContent.append(com.watabou.pixeldungeon.utils.Utils.format("<p>🖼️ <a href=\"/fs/%s\">%s</a> (<a href=\"/preview-image?file=%s\">preview</a>) (<a href=\"/edit-png?file=%s\">edit</a>)</p>", fullPath, name, encodedPath2, encodedPath2));
                } else {
                    // For other files, just show download link
                    dirContent.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", fullPath, name));
                }
            }
        }

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("MOD_NAME", ModdingMode.activeMod());

        // For the root directory, use empty string for upload path
        try {
            replacements.put("ENCODED_UPLOAD_PATH", java.net.URLEncoder.encode("", "UTF-8"));
        } catch (Exception e) {
            replacements.put("ENCODED_UPLOAD_PATH", ""); // Fallback if encoding fails
        }
        replacements.put("DIRECTORY_CONTENTS", dirContent.toString());

        return replacePlaceholders(template, replacements);
    }

    @Override
    public String generateDirectoryListing(String directoryPath) {
        String template = loadTemplate("directory_listing_template.html");

        // Generate up one level link
        String upOneLevelLink = "";
        if (!directoryPath.isEmpty()) {
            String upOneLevel = directoryPath.contains("/") ?
                directoryPath.substring(0, directoryPath.lastIndexOf("/")) : "";
            if (upOneLevel.isEmpty()) {
                upOneLevelLink = "<p><a href=\"/list\">..</a></p>";
            } else {
                upOneLevelLink = com.watabou.pixeldungeon.utils.Utils.format("<p><a href=\"/fs/%s/\">..</a></p>", upOneLevel);
            }
        } else {
            upOneLevelLink = "<p><a href=\"/list\">..</a></p>";
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
            java.util.Collections.sort(directories);
            java.util.Collections.sort(files);


            // List directories first
            for (String name : directories) {
                String fullPath = directoryPath.isEmpty() ? name : directoryPath + "/" + name;
                dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>",
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
                    dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2));
                } else if (name.toLowerCase().endsWith(".lua")) {
                    // For Lua files, add both download and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-lua?file=%s\">edit</a>) (<a href=\"/fs/%s?download=1\">download</a>)</p>",
                        fullPath, name, encodedPath2, fullPath));
                } else if (name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg") || name.toLowerCase().endsWith(".jpeg")) {
                    // For image files, add download, preview, and edit links
                    String encodedPath2;
                    try {
                        encodedPath2 = java.net.URLEncoder.encode(fullPath, "UTF-8");
                    } catch (Exception e) {
                        encodedPath2 = fullPath; // Fallback if encoding fails
                    }
                    dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>🖼️ <a href=\"/fs/%s\">%s</a> (<a href=\"/preview-image?file=%s\">preview</a>) (<a href=\"/edit-png?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2, encodedPath2));
                } else {
                    // For other files, just show download link
                    dirListing.append(com.watabou.pixeldungeon.utils.Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>",
                        fullPath, name));
                }
            }
        }

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("DIRECTORY_PATH", directoryPath.isEmpty() ? "/" : directoryPath);

        // For the template's upload link in header, use the directory path with potential slash added
        String templateUploadPath = directoryPath.isEmpty() ? "" : directoryPath;
        if (!templateUploadPath.endsWith("/") && !templateUploadPath.isEmpty()) {
            templateUploadPath += "/";
        }
        try {
            replacements.put("ENCODED_UPLOAD_PATH", java.net.URLEncoder.encode(templateUploadPath, "UTF-8"));
        } catch (Exception e) {
            replacements.put("ENCODED_UPLOAD_PATH", templateUploadPath); // Fallback if encoding fails
        }
        replacements.put("UP_ONE_LEVEL_LINK", upOneLevelLink);
        replacements.put("DIRECTORY_LISTING", dirListing.toString());

        return replacePlaceholders(template, replacements);
    }

    @Override
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

        // Prepare form action and path display
        String formAction = "/upload";
        String pathDisplay = currentPath != null ? currentPath : "";
        replacements.put("FORM_ACTION", formAction);
        replacements.put("CURRENT_PATH", pathDisplay);

        // Prepare upload link
        String uploadLink = currentPath != null && !currentPath.isEmpty()
            ? com.watabou.pixeldungeon.utils.Utils.format("<p><a href=\"/fs/%s/\">Back to directory</a></p>", currentPath)
            : "<p><a href=\"/list\">Back to main directory</a></p>";
        replacements.put("UPLOAD_LINK", uploadLink);

        return replacePlaceholders(template, replacements);
    }

    @Override
    public String serveJsonEditor(String filePath) {
        String template = loadTemplate("json_editor_template.html");

        String uploadPath = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf("/")) : "";

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("UPLOAD_PATH", uploadPath);
        replacements.put("FILE_PATH", filePath);
        replacements.put("ESCAPED_FILE_PATH", javaScriptEscape(filePath));

        return replacePlaceholders(template, replacements);
    }

    @Override
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

    @Override
    protected String[] listDirectoryContents(String path) {
        GLog.debug("Listing contents of directory: '" + path + "'");

        try {
            // Use ModdingMode to get a combined list of both assets and external files
            java.util.List<String> resourceList = ModdingMode.listResources(path, (dir, name) -> {
                // Include all items in the directory
                return true;
            });

            GLog.debug("ModdingMode.listResources returned " + (resourceList != null ? resourceList.size() : 0) + " items for path: '" + path + "'");

            // ModdingMode.listResources returns direct child names, so no path prefix filtering is needed
            // Filter the resources to only include direct children, not nested items
            java.util.List<String> filteredList = new java.util.ArrayList<>();

            for (String resource : resourceList) {
                // Only include direct children, not nested items
                if (!resource.contains("/")) {
                    filteredList.add(resource);
                }
            }

            GLog.debug("After filtering for direct children, found " + filteredList.size() + " items in directory: '" + path + "'");

            return filteredList.toArray(new String[0]);
        } catch (Exception e) {
            GLog.debug("Error listing directory contents: " + e.getMessage());
            e.printStackTrace();
            // Fallback to file system method
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            if (modFile.exists()) {
                String[] result = modFile.list();
                GLog.debug("Found " + (result != null ? result.length : 0) + " items in filesystem directory (fallback)");
                return result;
            }
            GLog.debug("Filesystem directory does not exist: " + modFile.getAbsolutePath());
            return null;
        }
    }

    @Override
    protected boolean isDirectoryItem(String parentPath, String itemName) {
        GLog.debug("Checking if item '" + itemName + "' in directory '" + parentPath + "' is a directory");

        // For Remixed mod, check both assets and external files using ModdingMode
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            // First check if the item exists as a directory in the mod-specific external storage
            String fullPath = ModdingMode.activeMod();
            if (!parentPath.isEmpty()) {
                fullPath += "/" + parentPath;
            }
            fullPath += "/" + itemName;

            File itemFile = FileSystem.getExternalStorageFile(fullPath);
            if (itemFile.exists() && itemFile.isDirectory()) {
                GLog.debug("Item '" + itemName + "' found as directory in external storage");
                return true;
            }

            // Then check using ModdingMode.listResources to see if it's a directory in assets
            // Need to check in the context of the parent path
            String assetPath = (parentPath.isEmpty() ? "" : parentPath + "/") + itemName;
            try {
                // Using listResources to check if it's a directory in assets
                // If this path has any content, it means it's a directory
                String[] assetList = FileSystem.listResources(assetPath);
                if (assetList != null && assetList.length > 0) {
                    GLog.debug("Item '" + itemName + "' found as directory in assets, contains " + assetList.length + " items");
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Item '" + itemName + "' not found as directory in assets: " + e.getMessage());
            }

            GLog.debug("Item '" + itemName + "' is not a directory in assets or external storage");
            return false;
        }
        // For other mods, use ModdingMode to check both external storage and potentially other sources
        else {
            // Check if it exists in external storage
            String fullPath = ModdingMode.activeMod();
            if (!parentPath.isEmpty()) {
                fullPath += "/" + parentPath;
            }
            fullPath += "/" + itemName;

            File itemFile = FileSystem.getExternalStorageFile(fullPath);
            if (itemFile.exists() && itemFile.isDirectory()) {
                GLog.debug("Item '" + itemName + "' (full path: " + fullPath + ") found as directory in filesystem");
                return true;
            }

            // For non-Remixed mods, also check via ModdingMode for consistency
            try {
                String checkPath = (parentPath.isEmpty() ? "" : parentPath + "/") + itemName;
                String[] assetList = FileSystem.listResources(checkPath);
                if (assetList != null && assetList.length > 0) {
                    GLog.debug("Item '" + itemName + "' found as directory in other mod via ModdingMode, contains " + assetList.length + " items");
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Item '" + itemName + "' not found as directory via ModdingMode: " + e.getMessage());
            }

            GLog.debug("Item '" + itemName + "' (full path: " + fullPath + ") is not a directory in filesystem");
            return false;
        }
    }


    @Override
    public Response serve(IHTTPSession session) {
        // Call the parent implementation which includes our new debug endpoints
        return super.serve(session);
    }


    /**
     * Check if a path represents a directory for the current mod
     */
    @Override
    protected boolean isDirectory(String path) {
        GLog.debug("Checking if path is directory: '" + path + "'");

        // Special case: empty path or "/" represents the root directory
        if (path.isEmpty() || path.equals("/")) {
            GLog.debug("Path is root directory, returning true");
            return true;
        }

        // For Remixed mod, check both assets and external files using ModdingMode
        if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
            // First check if path exists as directory in mod-specific external storage
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            if (modFile.exists() && modFile.isDirectory()) {
                GLog.debug("Path '" + path + "' found as directory in external storage");
                return true;
            }

            // Then check if path exists as directory in assets (base game assets)
            // Using listResources to check if it's a directory in assets
            try {
                String[] assetList = FileSystem.listResources(path);
                if (assetList != null && assetList.length > 0) {
                    GLog.debug("Path '" + path + "' found as directory in assets, contains " + assetList.length + " items");
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Path '" + path + "' not found as directory in assets: " + e.getMessage());
            }

            GLog.debug("Path '" + path + "' is not a directory in assets or external storage");
            return false;
        }
        // For other mods, use ModdingMode to check both external storage and potentially other sources
        else {
            // Check if it exists in external storage
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
            if (modFile.exists() && modFile.isDirectory()) {
                GLog.debug("Path '" + path + "' found as directory in filesystem");
                return true;
            }

            // For non-Remixed mods, also check via ModdingMode for consistency
            try {
                String[] assetList = FileSystem.listResources(path);
                if (assetList != null && assetList.length > 0) {
                    GLog.debug("Path '" + path + "' found as directory via ModdingMode, contains " + assetList.length + " items");
                    return true;
                }
            } catch (Exception e) {
                GLog.debug("Path '" + path + "' not found as directory via ModdingMode: " + e.getMessage());
            }

            GLog.debug("Path '" + path + "' is not a directory in filesystem or other sources");
            return false;
        }
    }

    /**
     * Debug endpoint to test ModdingMode.listResources directly
     */
    protected Response serveDebugList(String path) {
        GLog.debug("serveDebugList called with path: '" + path + "'");

        try {
            List<String> resourceList = ModdingMode.listResources(path, (dir, name) -> {
                // Include all items in the directory
                return true;
            });

            StringBuilder response = new StringBuilder();
            response.append("<h1>Debug List Resources for Path: '").append(path).append("'</h1>");
            response.append("<p>Active mod: ").append(ModdingMode.activeMod()).append("</p>");

            response.append("<p>Total resources found: ").append(resourceList.size()).append("</p>");
            response.append("<ul>");
            for (String resource : resourceList) {
                response.append("<li>").append(resource).append("</li>");
            }
            response.append("</ul>");

            // Also show what our filtering logic would return (updated)
            response.append("<h2>Filtering Logic Results (Fixed):</h2>");
            List<String> filteredList = new java.util.ArrayList<>();

            for (String resource : resourceList) {
                // Only include direct children, not nested items
                if (!resource.contains("/")) {
                    filteredList.add(resource);
                }
            }

            response.append("<p>After filtering for direct children: ").append(filteredList.size()).append("</p>");
            response.append("<ul>");
            for (String item : filteredList) {
                response.append("<li>").append(item).append("</li>");
            }
            response.append("</ul>");

            return newFixedLengthResponse(Response.Status.OK, "text/html", response.toString());
        } catch (Exception e) {
            GLog.debug("Error in serveDebugList: " + e.getMessage());
            EventCollector.logException(e);
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                "<h1>Error in serveDebugList</h1><p>" + e.getMessage() + "</p>");
        }
    }

    /**
     * Serve Lua editor page (similar to JSON editor)
     */
    public String serveLuaEditor(String filePath) {
        String template = loadTemplate("lua_editor_template.html");

        String uploadPath = filePath.contains("/") ? filePath.substring(0, filePath.lastIndexOf("/")) : "";

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("UPLOAD_PATH", uploadPath);
        replacements.put("FILE_PATH", filePath);
        replacements.put("ESCAPED_FILE_PATH", javaScriptEscape(filePath));

        return replacePlaceholders(template, replacements);
    }

    /**
     * Serve image preview page
     */
    protected Response serveImagePreview(String filePath) {
        try {
            GLog.debug("Serving image preview for: " + filePath);

            // Verify that the file exists and is an image
            if (!filePath.toLowerCase().endsWith(".png") &&
                !filePath.toLowerCase().endsWith(".jpg") &&
                !filePath.toLowerCase().endsWith(".jpeg")) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html",
                    "<html><body><h1>Invalid image file</h1></body></html>");
            }

            // Create an HTML page to display the image
            String encodedFilePath = java.net.URLEncoder.encode(filePath, "UTF-8");
            String html = String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <title>Image Preview - %s</title>" +
                "    <style>" +
                "        body { font-family: Arial, sans-serif; margin: 20px; background: #f0f0f0; }" +
                "        .container { background: white; padding: 20px; border-radius: 8px; max-width: 800px; margin: 0 auto; }" +
                "        .image-container { text-align: center; margin: 20px 0; }" +
                "        img { max-width: 100%%; height: auto; border: 1px solid #ccc; }" +
                "        .controls { text-align: center; margin: 20px 0; }" +
                "        .controls a { margin: 0 10px; padding: 10px 15px; background: #4CAF50; color: white; text-decoration: none; border-radius: 4px; }" +
                "        .controls a:hover { background: #45a049; }" +
                "    </style>" +
                "</head>" +
                "<body>" +
                "    <div class='container'>" +
                "        <h1>Preview: %s</h1>" +
                "        <div class='image-container'>" +
                "            <img src='/fs/%s' alt='%s'>" +
                "        </div>" +
                "        <div class='controls'>" +
                "            <a href='/edit-png?file=%s'>Edit with PixelCraft</a>" +
                "            <a href='/fs/%s?download=true'>Download</a>" +
                "            <a href='/list'>Back to directory</a>" +
                "        </div>" +
                "    </div>" +
                "</body>" +
                "</html>",
                filePath, filePath, filePath, filePath, encodedFilePath, filePath);

            return newFixedLengthResponse(Response.Status.OK, "text/html", html);

        } catch (Exception e) {
            GLog.debug("Error serving image preview: " + e.getMessage());
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                "<html><body><h1>Error serving image preview</h1></body></html>");
        }
    }

    /**
     * Serve PNG editor page (redirects to PixelCraft with the image loaded)
     */
    protected Response servePngEditor(String filePath) {
        try {
            GLog.debug("Serving PNG editor for: " + filePath);

            // Verify that the file exists and is an image
            if (!filePath.toLowerCase().endsWith(".png") &&
                !filePath.toLowerCase().endsWith(".jpg") &&
                !filePath.toLowerCase().endsWith(".jpeg")) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html",
                    "<html><body><h1>Invalid image file</h1></body></html>");
            }

            // Redirect to PixelCraft with the edit_file parameter
            String encodedFilePath = java.net.URLEncoder.encode(filePath, "UTF-8");
            String pixelCraftUrl = "/web/pixelcraft/?edit_file=" + encodedFilePath;

            // Create a redirect page
            String html = String.format(
                "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "    <meta http-equiv='refresh' content='0; url=%s'>" +
                "    <title>Redirecting to PixelCraft Editor</title>" +
                "</head>" +
                "<body>" +
                "    <p>If you are not redirected to PixelCraft automatically, <a href='%s'>click here</a>.</p>" +
                "</body>" +
                "</html>",
                pixelCraftUrl, pixelCraftUrl);

            return newFixedLengthResponse(Response.Status.OK, "text/html", html);

        } catch (Exception e) {
            GLog.debug("Error serving PNG editor: " + e.getMessage());
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                "<html><body><h1>Error serving PNG editor</h1></body></html>");
        }
    }

    /**
     * Handle saving texture content from PixelCraft editor
     */
    protected Response handleTextureSave(IHTTPSession session) {
        try {
            GLog.debug("Handling texture save request");

            // Use the same approach as JSON save, but for texture data
            Map<String, String> files = new java.util.HashMap<>();

            // This will parse the body and handle both form data and raw data
            session.parseBody(files);

            // Try to get the raw texture data from postData
            String jsonString = files.get("postData");

            // If postData is null, the content may be in the input stream directly
            if (jsonString == null || jsonString.isEmpty()) {
                // Get query parameters in case the data was sent as query parameters
                String body = session.getQueryParameterString();
                if (body != null && !body.isEmpty()) {
                    jsonString = java.net.URLDecoder.decode(body, "UTF-8");
                }
            }

            // If still null, try to read from the input stream directly
            if (jsonString == null || jsonString.isEmpty()) {
                GLog.debug("Reading texture data from input stream");
                java.util.Map<String, java.util.List<String>> parms = session.getParameters();

                // If parameters exist, check if we have JSON in parameters
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
                        // Create a buffer and read with timeout
                        byte[] buffer = new byte[8192]; // Increased buffer size for image data
                        java.io.InputStream inputStream = session.getInputStream();

                        // Mark and reset approach to avoid issues with already-read streams
                        if (inputStream.markSupported()) {
                            inputStream.mark(8192);
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
                GLog.debug("No texture data found in request");
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                    "{\"error\":\"Empty request body\"}");
            }

            GLog.debug("Received texture data (length): " + jsonString.length());

            // Parse the JSON to extract filename and image content
            org.json.JSONObject jsonData = new org.json.JSONObject(jsonString);
            String filename = jsonData.getString("name");
            String base64Content = jsonData.getString("image");

            GLog.debug("Saving texture to: " + filename);

            // Check if we're trying to save to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                GLog.debug("Save blocked - attempt to save to main 'Remixed' mod");
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
                    "{\"error\":\"Save to the main 'Remixed' mod is disabled for security reasons.\"}");
            }

            // Validate that the file path is within the allowed mod directory
            if (filename.contains("../") || filename.startsWith("../")) {
                GLog.debug("Directory traversal attempt detected: " + filename);
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
                    "{\"error\":\"Directory traversal is not allowed.\"}");
            }

            // Create the full path for the file
            String fullPath = ModdingMode.activeMod() + "/" + filename;
            GLog.debug("Full file path: " + fullPath);

            // Decode the base64 content using Java's built-in Base64 decoder
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Content);

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
            GLog.debug("Writing texture content to file");
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile)) {
                fos.write(imageBytes);
            }

            // Since we're on desktop, this is the place to trigger cache refresh if needed
            GLog.debug("=== TEXTURE SAVE COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "application/json",
                "{\"success\":true, \"message\":\"File saved successfully to: " + fullPath + "\"}");

        } catch (org.json.JSONException e) {
            GLog.debug("=== JSON PARSING ERROR ===");
            GLog.debug("JSON parsing error: " + e.getMessage());
            e.printStackTrace();
            return newFixedLengthResponse(Response.Status.BAD_REQUEST, "application/json",
                "{\"error\":\"Invalid JSON format in request: " + e.getMessage() + "\"}");
        } catch (Exception e) {
            GLog.debug("=== TEXTURE SAVE FAILED ===");
            GLog.debug("Texture save error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                "{\"error\":\"Failed to save texture file: " + e.getMessage() + "\"}");
        }
    }

    /**
     * Handle getting texture content for PixelCraft editor
     */
    protected Response handleTextureGet(String filePath) {
        try {
            GLog.debug("Handling texture get request for: " + filePath);

            // Create the full path for the file
            String fullPath = ModdingMode.activeMod() + "/" + filePath;
            GLog.debug("Full file path: " + fullPath);

            // First, check if the file exists in the mod directory
            File textureFile = FileSystem.getExternalStorageFile(fullPath);

            byte[] fileBytes;
            if (textureFile.exists()) {
                // File exists in mod directory, read from there
                fileBytes = new byte[(int) textureFile.length()];
                try (java.io.FileInputStream fis = new java.io.FileInputStream(textureFile)) {
                    fis.read(fileBytes);
                }
            } else {
                // File doesn't exist in mod directory, try to read from assets via ModdingMode
                try (java.io.InputStream fis = ModdingMode.getInputStream(filePath)) {
                    if (fis == null) {
                        GLog.debug("Texture file does not exist in mod directory or assets: " + fullPath);
                        return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json",
                            "{\"error\":\"File not found: " + filePath + "\"}");
                    }

                    // Read all bytes from input stream
                    java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[1024];
                    while ((nRead = fis.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    fileBytes = buffer.toByteArray();
                }
            }

            // Encode to base64 using Java's built-in Base64 encoder
            String base64Content = java.util.Base64.getEncoder().encodeToString(fileBytes);

            // Create a JSON response with the base64 content
            String jsonResponse = "{\"name\":\"" + filePath + "\",\"image\":\"" + base64Content + "\"}";

            GLog.debug("=== TEXTURE GET COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "application/json", jsonResponse);

        } catch (Exception e) {
            GLog.debug("=== TEXTURE GET FAILED ===");
            GLog.debug("Texture get error: " + e.getMessage());
            e.printStackTrace(); // Log the full stack trace for debugging
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "application/json",
                "{\"error\":\"Failed to get texture file: " + e.getMessage() + "\"}");
        }
    }

}