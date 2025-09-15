package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    public WebServer(int port) {
        super(port);
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

    private static void listDir(StringBuilder msg, String path) {
        GLog.debug("listDir called with path: '" + path + "'");
        List<String> list = ModdingMode.listResources(path,(dir, name)->true);
        
        // Separate directories and files
        List<String> directories = new java.util.ArrayList<>();
        List<String> files = new java.util.ArrayList<>();
        
        for (String name : list) {
            // Check if this is a directory by looking at the filesystem
            File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path + name);
            if (modFile.exists() && modFile.isDirectory()) {
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
                msg.append(Utils.format("<p>📁 <a href=\"/fs/%s/\">%s/</a></p>", name, name));
            } else {
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

    private Response serveFs(String file) {
        // Check if this is a directory
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + file);
        if (modFile.exists() && modFile.isDirectory()) {
            StringBuilder msg = new StringBuilder("<html><body>");
            msg.append(defaultHead());
            msg.append("<h1>Directory: ").append(file.isEmpty() ? "/" : file).append("</h1>");
            msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/upload?path=");
            String encodedPath = "";
            try {
                encodedPath = java.net.URLEncoder.encode(file, "UTF-8");
                GLog.debug("Encoded directory path for upload link: '" + encodedPath + "' from original: '" + file + "'");
            } catch (Exception e) {
                encodedPath = file; // Fallback if encoding fails
                GLog.debug("Directory path encoding failed, using original: '" + file + "'");
            }
            msg.append(encodedPath);
            msg.append("\">📤 Upload Files</a></p>");
            
            // Add "up one level" link if not at root
            if (!file.isEmpty()) {
                String upOneLevel = file.contains("/") ? file.substring(0, file.lastIndexOf("/")) : "";
                msg.append(Utils.format("<p><a href=\"/fs/%s\">..</a></p>", upOneLevel));
            }
            
            // List directory contents with directories first
            String[] contents = modFile.list();
            if (contents != null) {
                // Separate directories and files
                List<String> directories = new java.util.ArrayList<>();
                List<String> files = new java.util.ArrayList<>();
                
                for (String name : contents) {
                    File item = new File(modFile, name);
                    if (item.isDirectory()) {
                        directories.add(name);
                    } else {
                        files.add(name);
                    }
                }
                
                // Sort directories and files separately
                Collections.sort(directories);
                Collections.sort(files);
                
                msg.append("<div class=\"file-list\">");
                // List directories first
                for (String name : directories) {
                    msg.append(Utils.format("<p>📁 <a href=\"/fs/%s%s/\">%s/</a></p>", file, name, name));
                }
                // Then list files
                for (String name : files) {
                    msg.append(Utils.format("<p>📄 <a href=\"/fs/%s%s\">%s</a></p>", file, name, name));
                }
                msg.append("</div>");
            }
            
            msg.append("</body></html>");
            return newFixedLengthResponse(Response.Status.OK, "text/html", msg.toString());
        }
        
        // Handle file download
        if(ModdingMode.isResourceExist(file)) {
            InputStream fis = ModdingMode.getInputStream(file);
            Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
            return response;
        } else {
            // File or directory doesn't exist
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
            GLog.debug("Handling file upload, URI: " + session.getUri());
            
            // Check if we're trying to upload to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", 
                    serveUploadForm("ERROR: Upload to the main 'Remixed' mod is disabled for security reasons.", ""));
            }
            
            Map<String, String> files = new java.util.HashMap<>();
            session.parseBody(files);
            
            // Get the uploaded file
            String filename = session.getParameters().get("file").get(0);
            String path = "";
            
            // Try to get path from parameters
            if (session.getParameters().containsKey("path")) {
                path = session.getParameters().get("path").get(0);
                GLog.debug("Upload path from form parameter: '" + path + "'");
            }
            
            // Handle null paths
            if (path == null) path = "";
            
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
            
            // Get the temporary uploaded file
            String tempFilePath = files.get("file");
            if (tempFilePath == null) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", 
                    serveUploadForm("ERROR: No file uploaded.", path));
            }
            
            // Move the file to the correct location
            File tempFile = new File(tempFilePath);
            File destFile = FileSystem.getExternalStorageFile(fullPath);
            
            // Create directories if needed
            File destDir = destFile.getParentFile();
            if (destDir != null && !destDir.exists()) {
                destDir.mkdirs();
            }
            
            // Copy the file
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
            tempFile.delete();
            
            return newFixedLengthResponse(Response.Status.OK, "text/html", 
                serveUploadForm("File uploaded successfully to: " + fullPath, path));
                
        } catch (Exception e) {
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
                
                // Parse query parameters manually
                if (uri.contains("?")) {
                    String query = uri.substring(uri.indexOf("?") + 1);
                    GLog.debug("Query string: " + query);
                    
                    // Split by & to get parameter pairs
                    String[] params = query.split("&");
                    for (String param : params) {
                        GLog.debug("Processing parameter: " + param);
                        if (param.startsWith("path=")) {
                            path = param.substring(5); // Remove "path=" prefix
                            GLog.debug("Raw path parameter: '" + path + "'");
                            // URL decode the path
                            try {
                                path = java.net.URLDecoder.decode(path, "UTF-8");
                                GLog.debug("Upload path after URL decoding: '" + path + "'");
                            } catch (Exception e) {
                                GLog.debug("Upload path URL decoding failed: " + e.getMessage());
                                // If decoding fails, use the path as is
                            }
                            // Ensure path is not null
                            if (path == null) {
                                path = "";
                                GLog.debug("Path was null, setting to empty string");
                            }
                            break;
                        }
                    }
                } else {
                    GLog.debug("No query parameters found in URI");
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