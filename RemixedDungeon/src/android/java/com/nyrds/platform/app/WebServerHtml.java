package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.Assets;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * HTML generation utilities for the WebServer
 */
class WebServerHtml {

    /**
     * Load template from assets
     */
    private static String loadTemplate(String templateName) {
        try {
            return Assets.loadAssetAsString("html/" + templateName);
        } catch (Exception e) {
            GLog.error("Failed to load template: " + templateName + ", error: " + e.getMessage());
            return "<html><body><h1>Template Error</h1><p>Failed to load template: " + templateName + "</p></body></html>";
        }
    }

    /**
     * Replace placeholders in template
     */
    private static String replacePlaceholders(String template, java.util.Map<String, String> replacements) {
        String result = template;
        for (java.util.Map.Entry<String, String> entry : replacements.entrySet()) {
            result = result.replace("{{" + entry.getKey() + "}}", entry.getValue());
        }
        return result;
    }

    /**
     * Escape string for use in JavaScript
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
     * Generate the root page HTML using template
     */
    public static String serveRoot() {
        String template = loadTemplate("root_template.html");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("GAME_VERSION", Utils.format("%s (%d)", GameLoop.version, GameLoop.versionCode));
        replacements.put("MOD_INFO", Utils.format("%s (%d)", ModdingMode.activeMod(), ModdingMode.activeModVersion()));

        String levelInfo = "";
        if(Dungeon.level != null) {
            levelInfo = Utils.format("<p>Level: %s</p>", Dungeon.level.levelId);
        }
        replacements.put("LEVEL_INFO", levelInfo);

        return replacePlaceholders(template, replacements);
    }

    /**
     * Generate the file listing page HTML
     */
    public static String serveList() {
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
    public static String generateDirectoryListing(String directoryPath) {
        String template = loadTemplate("directory_listing_template.html");

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
            dirListing.append(Utils.format("<p>📤 <a href=\"/upload?path=%s\">Upload files to this directory</a></p>", encodedUploadPath));

            // List directories first
            for (String name : directories) {
                dirListing.append(Utils.format("<p>📁 <a href=\"/fs/%s%s/\">%s/</a></p>",
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
                    dirListing.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>",
                        fullPath, name, encodedPath2));
                } else {
                    // For non-JSON files, just show download link
                    dirListing.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>",
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
    public static String serveUploadForm(String message, String currentPath) {
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
    public static String serveJsonEditor(String filePath) {
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
    public static String serveNotFound() {
        String template = loadTemplate("not_found_template.html");
        return replacePlaceholders(template, new java.util.HashMap<>());
    }

    /**
     * List contents of a directory for the current mod
     */
    private static String[] listDirectoryContents(String path) {
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
    private static boolean isDirectoryItem(String parentPath, String itemName) {
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
        String encodedUploadPath;
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
                if (name.toLowerCase().endsWith(".json")) {
                    // For JSON files, add both download and edit links
                    String encodedPath1;
                    try {
                        encodedPath1 = java.net.URLEncoder.encode(name, "UTF-8");
                    } catch (Exception e) {
                        encodedPath1 = name; // Fallback if encoding fails
                    }
                    msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>", name, name, encodedPath1));
                } else {
                    // For non-JSON files, just show download link
                    msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", name, name));
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
                    msg.append(Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s%s</a> (<a href=\"/edit-json?file=%s\">edit</a>)</p>",
                        path, name, path, name, encodedPath));
                } else {
                    // For non-JSON files, just show download link
                    msg.append(Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s%s</a></p>", path, name, path, name));
                }
            }
        }
        msg.append("</div>");
    }
}