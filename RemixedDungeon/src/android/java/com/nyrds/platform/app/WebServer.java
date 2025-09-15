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
        msg += "<p><a href=\"/upload\">📤 Upload Files</a></p>";
        msg += "</body></html>";
        return msg;
    }

    private String serveList() {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        msg.append("<h1>File Browser</h1>");
        msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/upload\">📤 Upload Files</a></p>");
        msg.append("<h2>Files in Active Mod: ").append(ModdingMode.activeMod()).append("</h2>");
        listDir(msg, "");
        msg.append("</body></html>");

        return msg.toString();
    }

    private static void listDir(StringBuilder msg, String path) {
        List<String> list = ModdingMode.listResources(path,(dir, name)->true);
        Collections.sort(list);

        if (!path.isEmpty()) {
            String upOneLevel = path.contains("/") ? path.substring(0, path.lastIndexOf("/")) : "";
            msg.append(Utils.format("<p><a href=\"/fs/%s\">..</a></p>", upOneLevel));
        }

        msg.append("<div class=\"file-list\">");
        for (String name : list) {
            if(path.isEmpty()) {
                msg.append(Utils.format("<p><a href=\"/fs/%s\">%s</a></p>", name, name));
            } else {
                msg.append(Utils.format("<p><a href=\"/fs/%s%s\">%s%s</a></p>", path, name, path, name));
            }
        }
        msg.append("</div>");
    }

    private Response serveFs(String file) {
        if(ModdingMode.isResourceExist(file)) {
            InputStream fis = ModdingMode.getInputStream(file);
            Response response = newChunkedResponse(Response.Status.OK, "application/octet-stream", fis);
            response.addHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
            return response;
        } else {
            StringBuilder msg = new StringBuilder("<html><body>");
            msg.append(defaultHead());
            msg.append("<h1>Directory: ").append(file.isEmpty() ? "/" : file).append("</h1>");
            msg.append("<p><a href=\"/\">🏠 Home</a> | <a href=\"/list\">📁 File Browser</a> | <a href=\"/upload\">📤 Upload Files</a></p>");
            String upOneLevel = file.contains("/") ? file.substring(0, file.lastIndexOf("/")) : "";
            if(!file.isEmpty()) {
                msg.append(Utils.format("<p><a href=\"/fs/%s\">..</a></p>", upOneLevel));
            }
            if(!file.isEmpty()) {
                listDir(msg, file + "/");
            } else {
                listDir(msg, "");
            }
            msg.append("</body></html>");
            return  newFixedLengthResponse(Response.Status.OK, "text/html",msg.toString());
        }
    }
    
    private String serveUploadForm(String message) {
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
            msg.append("<form method=\"post\" action=\"/upload\" enctype=\"multipart/form-data\">");
            msg.append("<label for=\"file\">Select file to upload:</label><br>");
            msg.append("<input type=\"file\" name=\"file\" id=\"file\" required><br>");
            msg.append("<label for=\"path\">Path in mod (optional):</label><br>");
            msg.append("<input type=\"text\" name=\"path\" id=\"path\" placeholder=\"subfolder/\" value=\"\"><br>");
            msg.append("<button type=\"submit\">Upload File</button>");
            msg.append("</form>");
            msg.append("</div>");
        }
        
        msg.append("</body></html>");
        return msg.toString();
    }
    
    private Response handleFileUpload(IHTTPSession session) {
        try {
            // Check if we're trying to upload to the main Remixed mod
            if (ModdingMode.activeMod().equals(ModdingMode.REMIXED)) {
                return newFixedLengthResponse(Response.Status.FORBIDDEN, "text/html", 
                    serveUploadForm("ERROR: Upload to the main 'Remixed' mod is disabled for security reasons."));
            }
            
            Map<String, String> files = new java.util.HashMap<>();
            session.parseBody(files);
            
            // Get the uploaded file
            String filename = session.getParameters().get("file").get(0);
            String path = session.getParameters().get("path").get(0);
            
            // Sanitize the path
            if (path == null) path = "";
            path = path.replace("..", ""); // Prevent directory traversal
            path = path.replace("//", "/"); // Normalize path separators
            
            // Create the full path for the file
            String fullPath = ModdingMode.activeMod() + "/" + path + filename;
            
            // Get the temporary uploaded file
            String tempFilePath = files.get("file");
            if (tempFilePath == null) {
                return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html", 
                    serveUploadForm("ERROR: No file uploaded."));
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
                serveUploadForm("File uploaded successfully to: " + fullPath));
                
        } catch (Exception e) {
            GLog.debug("Upload error: " + e.getMessage());
            return newFixedLengthResponse(Response.Status.INTERNAL_ERROR, "text/html", 
                serveUploadForm("ERROR: Failed to upload file - " + e.getMessage()));
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

            if(uri.equals("/list")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveList());
            }
            
            if(uri.equals("/upload")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveUploadForm(""));
            }

            if(uri.startsWith("/fs/")) {
                return serveFs(uri.substring(4));
            }
        } else if (session.getMethod() == Method.POST) {
            if(uri.equals("/upload")) {
                return handleFileUpload(session);
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "Not Found");
    }
}


}