package com.nyrds.platform.app;

import android.util.Base64;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.Assets;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.scenes.AboutScene;
import com.watabou.pixeldungeon.utils.GLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }

    @Override
    public boolean isReady() {
        // Check if game loop is initialized (Android doesn't use libGDX)
        return GameLoop.instance() != null;
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



    /**
     * Handle saving texture content from PixelCraft editor
     */
    protected Response handleTextureSave(IHTTPSession session) {
        try {
            GLog.debug("Handling texture save request");

            // Use the same approach as JSON save, but for texture data
            Map<String, String> files = new HashMap<>();

            // This will parse the body and handle both form data and raw data
            session.parseBody(files);

            // Try to get the raw texture data from postData
            String jsonString = files.get("postData");

            // If postData is null, the content may be in the input stream directly
            if (jsonString == null || jsonString.isEmpty()) {
                // Get query parameters in case the data was sent as query parameters
                String body = session.getQueryParameterString();
                if (body != null && !body.isEmpty()) {
                    jsonString = URLDecoder.decode(body, "UTF-8");
                }
            }

            // If still null, try to read from the input stream directly
            if (jsonString == null || jsonString.isEmpty()) {
                GLog.debug("Reading texture data from input stream");
                Map<String, List<String>> parms = session.getParameters();

                // If parameters exist, check if we have JSON in parameters
                if (!parms.isEmpty()) {
                    for (Map.Entry<String, List<String>> entry : parms.entrySet()) {
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
                        InputStream inputStream = session.getInputStream();

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
            JSONObject jsonData = new JSONObject(jsonString);
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
            if (!isSafeResourcePath(filename)) {
                GLog.debug("Directory traversal attempt detected: " + filename);
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
                    "{\"error\":\"Directory traversal is not allowed.\"}");
            }

            // Create the full path for the file
            String fullPath = ModdingMode.activeMod() + "/" + filename;
            GLog.debug("Full file path: " + fullPath);

            // Decode the base64 content
            byte[] imageBytes = Base64.decode(base64Content, Base64.DEFAULT);

            // Create the file
            File destFile = FileSystem.getExternalStorageFile(fullPath);
            GLog.debug("Destination file path: " + destFile.getAbsolutePath());

            if (!isInsideStorageRoot(destFile)) {
                GLog.w("Blocked texture save outside storage root: " + fullPath);
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
                    "{\"error\":\"Destination outside mod storage.\"}");
            }

            // Create directories if needed
            File destDir = destFile.getParentFile();
            GLog.debug("Destination directory: " + (destDir != null ? destDir.getAbsolutePath() : "null"));
            if (destDir != null && !destDir.exists()) {
                GLog.debug("Creating destination directory");
                destDir.mkdirs();
            }

            // Write the content to the file
            GLog.debug("Writing texture content to file");
            try (FileOutputStream fos = new FileOutputStream(destFile)) {
                fos.write(imageBytes);
            }

            // Reinitialize the mod cache to reflect the newly saved file
            GLog.debug("Reinitializing mod cache after texture save");
            // FileSystem.reinitModCache(); // Not available on Android platform

            GLog.debug("=== TEXTURE SAVE COMPLETED SUCCESSFULLY ===");
            return newFixedLengthResponse(Response.Status.OK, "application/json",
                "{\"success\":true, \"message\":\"File saved successfully to: " + fullPath + "\"}");

        } catch (JSONException e) {
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
            try (FileInputStream fis = new FileInputStream(textureFile)) {
                fis.read(fileBytes);
            }

            // Encode to base64
            String base64Content = Base64.encodeToString(fileBytes, Base64.NO_WRAP);

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
            String encodedFilePath = URLEncoder.encode(filePath, "UTF-8");
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
                htmlEscape(filePath), htmlEscape(filePath), getEncodedPath(filePath),
                htmlEscape(filePath), encodedFilePath, getEncodedPath(filePath));

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
            String encodedFilePath = URLEncoder.encode(filePath, "UTF-8");
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