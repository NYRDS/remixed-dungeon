package com.nyrds.platform.app;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.platform.game.Game;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import fi.iki.elonen.NanoHTTPD;

public class WebServer extends NanoHTTPD {

    public WebServer(int port) {
        super(port);
    }

    private String defaultHead() {
        return "<head><meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\"></head>";
    }
    private String serveRoot() {
        String msg = "<html><body>";
        msg += defaultHead();
        msg += Utils.format("<br>RemixedDungeon: %s (%d)" ,RemixedDungeon.version, RemixedDungeon.versionCode);
        msg += Utils.format("<br>Mod: %s (%d)", ModdingMode.activeMod(), ModdingMode.activeModVersion());
        if(Dungeon.level != null) {
            msg += Utils.format("<br>Level: %s", Dungeon.level.levelId);
        }
        msg+= "</p></body><html>";
        return msg;
    }

    private String serveList() {
        StringBuilder msg = new StringBuilder("<html><body>");
        msg.append(defaultHead());
        listDir(msg, "");
        msg.append("</p></body><html>");

        return msg.toString();
    }

    private static void listDir(StringBuilder msg, String path) {
        List<String> list = ModdingMode.listResources(path,(dir, name)->true);
        Collections.sort(list);

        for (String name : list) {
            if(path.isEmpty()) {
                msg.append(Utils.format("<br><a href=\"/fs/%s\">%s</a>", name, name));
            } else {
                msg.append(Utils.format("<br><a href=\"/fs/%s%s\">%s%s</a>", path, name, path, name));
            }

        }
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
            String upOneLevel = file.contains("/") ? file.substring(0, file.lastIndexOf("/")) : "";
            msg.append(Utils.format("<br><a href=\"/fs/%s\">%s</a>", upOneLevel , ".."));
            if(!file.isEmpty()) {
                listDir(msg, file + "/");
            } else {
                listDir(msg, "");
            }
            msg.append("</p></body><html>");
            return  newFixedLengthResponse(Response.Status.OK, "text/html",msg.toString());
        }
    }


    @Override
    public Response serve(IHTTPSession session) {
        String uri = session.getUri();
        GLog.debug("WebServer: " + uri);

        if (session.getMethod() == NanoHTTPD.Method.GET) {
            if (uri.equals("/")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveRoot());
            }

            if(uri.equals("/list")) {
                return newFixedLengthResponse(Response.Status.OK, "text/html", serveList());
            }

            if(uri.startsWith("/fs/")) {
                return serveFs(uri.substring(4));
            }
        }

        return newFixedLengthResponse(Response.Status.NOT_FOUND, "text/html", "Not Found");
    }


}