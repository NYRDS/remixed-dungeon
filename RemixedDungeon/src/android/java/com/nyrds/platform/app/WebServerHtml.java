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
     * Generate the default HTML head with CSS styling
     */
    public static String defaultHead() {
        return "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"><style>body { font-family: Arial, sans-serif; margin: 20px; } input, select, button { margin: 5px; padding: 5px; } .upload-form { background: #f0f0f0; padding: 15px; border-radius: 5px; margin: 10px 0; } .file-list { margin: 10px 0; } .error { color: red; } .success { color: green; } a { text-decoration: none; color: #007bff; } a:hover { text-decoration: underline; }</style></head>";
    }
    
    /**
     * Generate the root page HTML
     */
    public static String serveRoot() {
        String msg = "<html><body>";
        msg += defaultHead();
        msg += "<h1>Remixed Dungeon Web Interface</h1>";
        msg += Utils.format("<p>RemixedDungeon: %s (%d)</p>", GameLoop.version, GameLoop.versionCode);
        msg += Utils.format("<p>Mod: %s (%d)</p>", ModdingMode.activeMod(), ModdingMode.activeModVersion());
        if(Dungeon.level != null) {
            msg += Utils.format("<p>Level: %s</p>", Dungeon.level.levelId);
        }
        msg += "<p><a href=\"/list\">📁 Browse Files</a></p>";
        msg += "<p><a href=\"/upload?path=\">📤 Upload Files</a></p>";
        msg += "<p><a href=\"/log\">📜 Download Game Log</a></p>";
        msg += "</body></html>";
        return msg;
    }

    /**
     * Generate the file listing page HTML
     */
    public static String serveList() {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>File Browser</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/upload?path=\">📤 Upload Files</a> | <a href=\"/log\">📜 Download Game Log</a></p>");
        msg.append("<h2>Files in Active Mod: ").append(ModdingMode.activeMod()).append("</h2>");
        listDir(msg, "");
        msg.append("</body></html>");

        return msg.toString();
    }

    /**
     * Generate HTML for directory listing page
     */
    public static String generateDirectoryListing(String directoryPath) {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>Directory: ").append(directoryPath.isEmpty() ? "/" : directoryPath).append("</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/log\">📜 Download Game Log</a> | <a href=\"/upload?path=");
        String encodedPath;
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
            String encodedUploadPath;
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

    /**
     * Generate the file upload form HTML
     */
    public static String serveUploadForm(String message, String currentPath) {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>File Upload</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/log\">📜 Download Game Log</a></p>");
        
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
    
    /**
     * Generate the "Not Found" error page HTML
     */
    public static String serveNotFound() {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>Not Found</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/log\">📜 Download Game Log</a> | <a href=\"/upload\">📤 Upload Files</a></p>");
        msg.append("<p>The requested file or directory was not found.</p>");
        msg.append("</body></html>");
        return msg.toString();
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
                msg.append(Utils.format("<p>📄 <a href=\"/fs/%s\">%s</a></p>", name, name));
            } else {
                msg.append(Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s%s</a></p>", path, name, path, name));
            }
        }
        msg.append("</div>");
    }
}