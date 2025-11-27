package com.nyrds.platform.app;

import com.badlogic.gdx.Gdx;
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

        // We need to generate directory contents dynamically
        StringBuilder dirContent = new StringBuilder();
        listDir(dirContent, "");

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("MOD_NAME", ModdingMode.activeMod());
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

        StringBuilder dirContent = new StringBuilder();
        listDir(dirContent, directoryPath);

        java.util.Map<String, String> replacements = new java.util.HashMap<>();
        replacements.put("DIRECTORY_PATH", directoryPath.isEmpty() ? "/" : directoryPath);
        try {
            replacements.put("ENCODED_UPLOAD_PATH", java.net.URLEncoder.encode(directoryPath, "UTF-8"));
        } catch (Exception e) {
            replacements.put("ENCODED_UPLOAD_PATH", directoryPath); // Fallback if encoding fails
        }
        replacements.put("UP_ONE_LEVEL_LINK", upOneLevelLink);
        replacements.put("DIRECTORY_LISTING", dirContent.toString());

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

            // Filter the resources to only include those that are in the current path (not subdirectories)
            java.util.List<String> filteredList = new java.util.ArrayList<>();
            String currentPath = path.isEmpty() ? "" : path + "/";

            for (String resource : resourceList) {
                if (resource.startsWith(currentPath)) {
                    String relativePath = resource.substring(currentPath.length());
                    // Only include direct children, not nested items
                    if (!relativePath.contains("/")) {
                        filteredList.add(relativePath);
                    }
                }
            }

            GLog.debug("Found " + filteredList.size() + " items in directory: '" + path + "'");

            return filteredList.toArray(new String[0]);
        } catch (Exception e) {
            GLog.debug("Error listing directory contents: " + e.getMessage());
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



}