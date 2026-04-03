package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

/**
 * WebServer implementation for HTML platform.
 * Provides a no-op implementation to allow compilation.
 */
public class WebServer extends BaseWebServer {
    public WebServer(int port) {
        super(port);
    }

    @Override
    public boolean isReady() {
        try {
            return com.badlogic.gdx.Gdx.files != null && GameLoop.instance() != null;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    protected void onServerStarted() {
    }

    @Override
    protected boolean isDirectory(String path) {
        if (path.isEmpty() || path.equals("/")) {
            return true;
        }
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
        return modFile.exists() && modFile.isDirectory();
    }

    @Override
    protected String[] listDirectoryContents(String path) {
        File modFile = FileSystem.getExternalStorageFile(ModdingMode.activeMod() + "/" + path);
        if (modFile.exists()) {
            return modFile.list();
        }
        return null;
    }

    @Override
    protected boolean isDirectoryItem(String parentPath, String itemName) {
        String fullPath = ModdingMode.activeMod();
        if (!parentPath.isEmpty()) {
            fullPath += "/" + parentPath;
        }
        fullPath += "/" + itemName;
        File itemFile = FileSystem.getExternalStorageFile(fullPath);
        return itemFile.isDirectory();
    }

    @Override
    public Response serve(IHTTPSession session) {
        return super.serve(session);
    }

    @Override
    public String serveRoot() {
        return "<!DOCTYPE html><html><head><title>Remixed Dungeon WebServer</title></head><body>" +
               "<h1>Remixed Dungeon WebServer</h1>" +
               "<p>Version: " + GameLoop.version + " (" + GameLoop.versionCode + ")</p>" +
               "<p>Mod: " + ModdingMode.activeMod() + "</p>" +
               "</body></html>";
    }

    @Override
    public String serveList() {
        String[] contents = listDirectoryContents("");
        StringBuilder sb = new StringBuilder("<html><body><h1>Directory Listing</h1>");
        if (contents != null) {
            for (String name : contents) {
                sb.append("<p><a href=\"/fs/").append(name).append("\">").append(name).append("</a></p>");
            }
        }
        sb.append("</body></html>");
        return sb.toString();
    }

    @Override
    public String generateDirectoryListing(String directoryPath) {
        return "<html><body><h1>Directory: " + directoryPath + "</h1></body></html>";
    }

    @Override
    public String serveUploadForm(String message, String currentPath) {
        return "<html><body><h1>Upload</h1></body></html>";
    }

    @Override
    public String serveJsonEditor(String filePath) {
        return "<html><body><h1>JSON Editor: " + filePath + "</h1></body></html>";
    }

    @Override
    public String serveLuaEditor(String filePath) {
        return "<html><body><h1>Lua Editor: " + filePath + "</h1></body></html>";
    }

    @Override
    public String serveNotFound() {
        return "<html><body><h1>404 Not Found</h1></body></html>";
    }

    @Override
    protected Response serveImagePreview(String filePath) {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html",
            "<html><body><h1>Image preview not supported in HTML</h1></body></html>");
    }

    @Override
    protected Response servePngEditor(String filePath) {
        return newFixedLengthResponse(Response.Status.BAD_REQUEST, "text/html",
            "<html><body><h1>PNG editor not supported in HTML</h1></body></html>");
    }

    @Override
    protected Response handleTextureSave(IHTTPSession session) {
        return newFixedLengthResponse(Response.Status.FORBIDDEN, "application/json",
            "{\"error\":\"Texture save not supported in HTML\"}");
    }

    @Override
    protected Response handleTextureGet(String filePath) {
        return newFixedLengthResponse(Response.Status.NOT_FOUND, "application/json",
            "{\"error\":\"Texture get not supported in HTML\"}");
    }
}
