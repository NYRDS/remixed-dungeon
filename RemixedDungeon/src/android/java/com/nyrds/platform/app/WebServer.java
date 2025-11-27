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

public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }
    
    @Override
    protected void onServerStarted() {
        // Notify AboutScene to refresh the WebServer link
        GameLoop.pushUiTask(AboutScene::refreshWebServerLink);
    }

    @Override
    protected boolean isDirectory(String path) {
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

    @Override
    protected String[] listDirectoryContents(String path) {
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

    @Override
    protected boolean isDirectoryItem(String parentPath, String itemName) {
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
            // For Android, check file system
            // Construct the full path properly: ModName/parentPath/itemName
            String fullPath = ModdingMode.activeMod();
            if (!parentPath.isEmpty()) {
                fullPath += "/" + parentPath;
            }
            fullPath += "/" + itemName;

            File itemFile = FileSystem.getExternalStorageFile(fullPath);
            boolean result = itemFile.isDirectory();
            GLog.debug("Item '" + itemName + "' (full path: " + fullPath + ") is " + (result ? "" : "not ") + "a directory in filesystem");
            return result;
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
                    return newFixedLengthResponse(Response.Status.OK, "text/html", serveJsonEditor(filePath));
                } else {
                    return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", serveNotFound());
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
        } else if (session.getMethod() == Method.POST) {
            if(uri.startsWith("/upload")) {
                return handleFileUpload(session);
            }

            if(uri.startsWith("/api/save-json")) {
                return handleJsonSave(session);
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

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", serveNotFound());
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

            // Decode the base64 content
            byte[] imageBytes = android.util.Base64.decode(base64Content, android.util.Base64.DEFAULT);

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

            // Reinitialize the mod cache to reflect the newly saved file
            GLog.debug("Reinitializing mod cache after texture save");
            // FileSystem.reinitModCache(); // Not available on Android platform

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

            // Check if the file exists
            File textureFile = FileSystem.getExternalStorageFile(fullPath);

            if (!textureFile.exists()) {
                GLog.debug("Texture file does not exist: " + fullPath);
                return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json",
                    "{\"error\":\"File not found: " + filePath + "\"}");
            }

            // Read the file content
            byte[] fileBytes = new byte[(int) textureFile.length()];
            try (java.io.FileInputStream fis = new java.io.FileInputStream(textureFile)) {
                fis.read(fileBytes);
            }

            // Encode to base64
            String base64Content = android.util.Base64.encodeToString(fileBytes, android.util.Base64.NO_WRAP);

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
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html",
                "<html><body><h1>Error serving PNG editor</h1></body></html>");
        }
    }

}