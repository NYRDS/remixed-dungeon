package com.nyrds.platform.app;

import com.nyrds.lua.LuaEngine;
import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.game.GamePreferences;
import com.nyrds.pixeldungeon.utils.ModDesc;
import com.nyrds.platform.game.RemixedDungeon;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.storage.SaveUtils;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingBase;
import com.nyrds.util.ModdingMode;
import com.nyrds.util.Mods;
import com.nyrds.util.Util;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.utils.GLog;
import fi.iki.elonen.NanoHTTPD;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Mod-scoped API for the built-in mod editor web app.
 *
 * The legacy save endpoints (/api/save-json and friends) write into the
 * ACTIVE mod only - an editor must target any installed mod, so every
 * handler here is scoped by an explicit mod dir under the storage root.
 * Guards mirror BaseWebServer: safe relative paths, absolute containment,
 * main Remixed mod read-only unless the platform allows dev writes.
 */
public class ModEditorEndpoints {

    private static final String PREFIX = "/api/mods/";

    // files copied verbatim from assets on /api/mods/create; version.json is
    // generated from the create payload instead, so it is not listed here
    private static final String[] TEMPLATE_FILES = {
        "mobsDesc/ExampleMob.json",
        "levelsDesc/Dungeon.json",
        "levelsDesc/Start.json",
        "strings_en.json",
    };

    private static final String TEMPLATE_DIR = "mod_editor_template";

    // caveman: mod dirs become file names and zip entry roots - keep the
    // charset tight, no dots (kills ".."), no separators, no spaces
    private static boolean isValidModName(String name) {
        return name != null && !name.isEmpty() && name.length() <= 64
            && name.matches("[A-Za-z0-9_-]+")
            && !name.equals(ModdingBase.REMIXED)
            && !name.equals("tmp");
    }

    private static NanoHTTPD.Response ok(JSONObject body) {
        return NanoHTTPD.newFixedLengthResponse(NanoHTTPD.Response.Status.OK, "application/json",
            body.toString());
    }

    private static NanoHTTPD.Response err(NanoHTTPD.Response.IStatus status, String message) {
        try {
            JSONObject body = new JSONObject();
            body.put("error", message);
            return NanoHTTPD.newFixedLengthResponse(status, "application/json", body.toString());
        } catch (JSONException e) {
            return NanoHTTPD.newFixedLengthResponse(status, "application/json",
                "{\"error\":\"failed to build error response\"}");
        }
    }

    private static File modDir(String modName) {
        return FileSystem.getExternalStorageFile(modName);
    }

    private static boolean modExists(String modName) {
        File dir = modDir(modName);
        return dir.exists() && dir.isDirectory();
    }

    // the built-in Remixed mod is content, not a user mod - dev platforms
    // may opt in via canWriteMainMod (same gate as sprite saves)
    private static boolean writableMod(String modName) {
        return !modName.equals(ModdingMode.REMIXED) || BaseWebServer.instance.canWriteMainMod();
    }

    // every mod-relative write target passes through here
    private static File modFile(String modName, String path) {
        return FileSystem.getExternalStorageFile(modName + "/" + path);
    }

    private static boolean containedInMod(File file) {
        return BaseWebServer.isInsideStorageRootAbs(file);
    }

    // ------------------------------------------------------------------
    // GET endpoints - registered in the BaseWebServer exact-match map
    // ------------------------------------------------------------------

    public static NanoHTTPD.Response handleModsList(NanoHTTPD.IHTTPSession session) {
        try {
            String active = ModdingMode.activeMod();
            List<JSONObject> mods = new ArrayList<>();

            for (Map.Entry<String, ModDesc> entry : Mods.getInstalledModsList().entrySet()) {
                ModDesc desc = entry.getValue();
                JSONObject m = new JSONObject();
                m.put("installDir", desc.installDir);
                m.put("name", desc.name);
                m.put("author", desc.author);
                m.put("description", desc.description);
                m.put("version", desc.version);
                m.put("hrVersion", desc.hrVersion);
                m.put("rpdVersion", desc.rpdVersion);
                m.put("url", desc.url);
                m.put("active", desc.installDir.equals(active));
                m.put("builtin", desc.installDir.equals(ModdingBase.REMIXED));
                mods.add(m);
            }

            mods.sort((a, b) -> a.optString("installDir").compareTo(b.optString("installDir")));

            JSONObject result = new JSONObject();
            result.put("mods", new JSONArray(mods));
            result.put("activeMod", active);
            return ok(result);
        } catch (Exception e) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    public static NanoHTTPD.Response handleModsFiles(NanoHTTPD.IHTTPSession session) {
        try {
            String modName = BaseWebServer.queryParam(session, "mod");
            String path = BaseWebServer.queryParam(session, "path");
            boolean builtin = "1".equals(BaseWebServer.queryParam(session, "builtin"));

            if (path == null) {
                path = "";
            }
            if (!BaseWebServer.isSafeResourcePath(path)) {
                return BaseWebServer.forbiddenPath();
            }

            JSONObject result = new JSONObject();
            JSONArray entries = new JSONArray();

            if (builtin) {
                // reference listing through the resource overlay (builtin
                // assets + active mod) - for picking templates/tilesets
                List<String> resources = ModdingMode.listResources(path,
                    (dir, name) -> true);
                for (String resource : resources) {
                    JSONObject e = new JSONObject();
                    e.put("name", resource);
                    e.put("dir", resource.contains("/"));
                    entries.put(e);
                }
            } else {
                if (!isValidModName(modName) || !modExists(modName)) {
                    return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
                }
                File dir = modFile(modName, path);
                if (!containedInMod(dir)) {
                    return BaseWebServer.forbiddenPath();
                }
                File[] files = dir.listFiles();
                if (files != null) {
                    Arrays.sort(files, Comparator.comparing(f -> f.getName().toLowerCase()));
                    for (File file : files) {
                        JSONObject e = new JSONObject();
                        e.put("name", file.getName());
                        e.put("dir", file.isDirectory());
                        e.put("size", file.length());
                        entries.put(e);
                    }
                }
            }

            result.put("path", path);
            result.put("entries", entries);
            return ok(result);
        } catch (Exception e) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    // text read: mod file first, builtin asset fallback (template picking).
    // binary reads for <img>/<canvas> go through /api/mods/raw instead.
    public static NanoHTTPD.Response handleModsRead(NanoHTTPD.IHTTPSession session) {
        try {
            String modName = BaseWebServer.queryParam(session, "mod");
            String file = BaseWebServer.queryParam(session, "file");
            boolean builtin = "1".equals(BaseWebServer.queryParam(session, "builtin"));

            if (!BaseWebServer.isSafeResourcePath(file) || file.isEmpty()) {
                return BaseWebServer.forbiddenPath();
            }

            byte[] data = null;
            String source = null;

            if (!builtin && isValidModName(modName) && modExists(modName)) {
                File f = modFile(modName, file);
                if (containedInMod(f) && f.isFile()) {
                    data = readFile(f);
                    source = "mod";
                }
            }

            if (data == null) {
                // getInputStream throws on missing resources, it does not
                // return null - treat any failure as not-found
                try (InputStream in = ModdingMode.getInputStream(file)) {
                    data = readStream(in);
                    source = "builtin";
                } catch (Exception ignored) {
                    data = null;
                }
            }

            if (data == null) {
                return err(NanoHTTPD.Response.Status.NOT_FOUND, "Not found: " + file);
            }
            if (data.length > 2 * 1024 * 1024) {
                return err(NanoHTTPD.Response.Status.BAD_REQUEST, "File too large for text read");
            }

            JSONObject result = new JSONObject();
            result.put("content", new String(data, StandardCharsets.UTF_8));
            result.put("size", data.length);
            result.put("source", source);
            return ok(result);
        } catch (Exception e) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    // raw bytes with a mime type, mod file first then builtin fallback -
    // the sprite/level canvas workloads need this, not base64 in json
    public static NanoHTTPD.Response handleModsRaw(NanoHTTPD.IHTTPSession session) {
        try {
            String modName = BaseWebServer.queryParam(session, "mod");
            String file = BaseWebServer.queryParam(session, "file");
            boolean builtin = "1".equals(BaseWebServer.queryParam(session, "builtin"));

            if (!BaseWebServer.isSafeResourcePath(file) || file.isEmpty()) {
                return BaseWebServer.forbiddenPath();
            }

            if (!builtin && isValidModName(modName) && modExists(modName)) {
                File f = modFile(modName, file);
                if (containedInMod(f) && f.isFile()) {
                    return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                        mimeFor(file), new FileInputStream(f));
                }
            }

            try (InputStream in = ModdingMode.getInputStream(file)) {
                return NanoHTTPD.newChunkedResponse(NanoHTTPD.Response.Status.OK,
                    mimeFor(file), in);
            } catch (Exception e) {
                return err(NanoHTTPD.Response.Status.NOT_FOUND, "Not found: " + file);
            }
        } catch (Exception e) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    private static String mimeFor(String file) {
        String lower = file.toLowerCase();
        if (lower.endsWith(".png")) {
            return "image/png";
        }
        if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) {
            return "image/jpeg";
        }
        if (lower.endsWith(".json")) {
            return "application/json";
        }
        return "application/octet-stream";
    }

    // zip with a top-level <modName>/ dir - exactly what UnzipTask expects,
    // so an exported zip reinstalls through the regular mod install flow
    public static NanoHTTPD.Response handleModsExport(NanoHTTPD.IHTTPSession session) {
        try {
            String modName = BaseWebServer.queryParam(session, "mod");

            if (!isValidModName(modName) || !modExists(modName)) {
                return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
            }

            ByteArrayOutputStream buffer = new ByteArrayOutputStream();
            try (ZipOutputStream zip = new ZipOutputStream(buffer)) {
                zipDir(modDir(modName), modName, zip, new int[]{0});
            }

            byte[] bytes = buffer.toByteArray();
            NanoHTTPD.Response response = NanoHTTPD.newFixedLengthResponse(
                NanoHTTPD.Response.Status.OK, "application/zip",
                new ByteArrayInputStream(bytes), bytes.length);
            response.addHeader("Content-Disposition",
                "attachment; filename=\"" + modName + ".zip\"");
            return response;
        } catch (Exception e) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    private static void zipDir(File dir, String entryPrefix, ZipOutputStream zip, int[] count) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (count[0]++ > 5000) {
                throw new IOException("too many files to zip");
            }
            String entryName = entryPrefix + "/" + file.getName();
            if (file.isDirectory()) {
                zipDir(file, entryName, zip, count);
            } else {
                zip.putNextEntry(new ZipEntry(entryName));
                try (FileInputStream in = new FileInputStream(file)) {
                    byte[] chunk = new byte[8192];
                    int n;
                    while ((n = in.read(chunk)) > 0) {
                        zip.write(chunk, 0, n);
                    }
                }
                zip.closeEntry();
            }
        }
    }

    // terrain table for the level editor - straight from the constants, so
    // the editor never ships a stale copy
    public static NanoHTTPD.Response handleTerrain(NanoHTTPD.IHTTPSession session) {
        try {
            List<Field> fields = new ArrayList<>();
            for (Field field : Terrain.class.getFields()) {
                if (field.getType() == int.class) {
                    fields.add(field);
                }
            }
            fields.sort(Comparator.comparingInt(f -> {
                try {
                    return f.getInt(null);
                } catch (IllegalAccessException e) {
                    return 0;
                }
            }));

            JSONArray terrains = new JSONArray();
            for (Field field : fields) {
                JSONObject t = new JSONObject();
                t.put("name", field.getName());
                t.put("value", field.getInt(null));
                terrains.put(t);
            }

            JSONObject result = new JSONObject();
            result.put("terrain", terrains);
            result.put("tileSize", DungeonTilemap.SIZE);
            return ok(result);
        } catch (Exception e) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    // ------------------------------------------------------------------
    // POST endpoints - dispatched from the BaseWebServer POST chain so
    // every response gets Connection: close (nanohttpd 2.3.1 drain bug)
    // ------------------------------------------------------------------

    // returns null for unknown sub-paths, so the caller falls through to 404
    public static NanoHTTPD.Response handlePost(String uri, NanoHTTPD.IHTTPSession session) {
        String action = uri.substring(PREFIX.length());
        try {
            switch (action) {
                case "create":
                    return handleCreate(session);
                case "duplicate":
                    return handleDuplicate(session);
                case "delete":
                    return handleDelete(session);
                case "save":
                    return handleSave(session);
                case "save_binary":
                    return handleSaveBinary(session);
                case "delete_file":
                    return handleDeleteFile(session);
                case "rename_file":
                    return handleRenameFile(session);
                case "select":
                    return handleSelect(session);
                case "validate":
                    return handleValidate(session);
                default:
                    return null;
            }
        } catch (JSONException e) {
            return err(NanoHTTPD.Response.Status.BAD_REQUEST, "Invalid JSON payload: " + e.getMessage());
        } catch (Exception e) {
            GLog.debug("ModEditor " + action + " failed: " + e.getMessage());
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, e.getMessage());
        }
    }

    private static JSONObject body(NanoHTTPD.IHTTPSession session) throws IOException, JSONException {
        String json = BaseWebServer.instance.readPostBody(session);
        if (json == null || json.isEmpty()) {
            throw new JSONException("empty request body");
        }
        return new JSONObject(json);
    }

    private static NanoHTTPD.Response handleCreate(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String name = payload.optString("name", "");
        String title = payload.optString("title", name);
        String author = payload.optString("author", "Unknown");
        String description = payload.optString("description", "");

        if (!isValidModName(name)) {
            return err(NanoHTTPD.Response.Status.BAD_REQUEST,
                "Bad mod name (use letters, digits, '-' and '_'): " + name);
        }
        if (modExists(name)) {
            return err(NanoHTTPD.Response.Status.CONFLICT, "Mod already exists: " + name);
        }

        File target = modDir(name);
        if (!target.mkdirs() && !target.exists()) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, "Cannot create mod dir");
        }

        List<String> copied = new ArrayList<>();
        for (String templateFile : TEMPLATE_FILES) {
            copyTemplateAsset(templateFile, modFile(name, templateFile));
            copied.add(templateFile);
        }

        // fresh manifest - never copied from the template. written directly,
        // not via JsonHelper.writeJson (that appends .json and swallows errors)
        JSONObject version = new JSONObject();
        version.put("version", 1);
        version.put("name", title);
        version.put("author", author);
        version.put("description", description);
        version.put("url", "");
        version.put("hr_version", "1");
        version.put("rpd_version", GameLoop.versionCode % 2000);
        try (FileOutputStream out = new FileOutputStream(modFile(name, "version.json"))) {
            out.write(version.toString(2).getBytes(StandardCharsets.UTF_8));
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("mod", name);
        result.put("files", new JSONArray(copied));
        return ok(result);
    }

    private static void copyTemplateAsset(String templateFile, File dest) throws IOException {
        try (InputStream in = ModdingMode.getInputStream(TEMPLATE_DIR + "/" + templateFile)) {
            if (in == null) {
                throw new IOException("template asset missing: " + templateFile);
            }
            File parent = dest.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }
            try (FileOutputStream out = new FileOutputStream(dest)) {
                byte[] chunk = new byte[8192];
                int n;
                while ((n = in.read(chunk)) > 0) {
                    out.write(chunk, 0, n);
                }
            }
        }
    }

    private static NanoHTTPD.Response handleDuplicate(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String from = payload.optString("from", "");
        String to = payload.optString("to", "");

        if (!isValidModName(from) || !modExists(from)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + from);
        }
        if (!isValidModName(to)) {
            return err(NanoHTTPD.Response.Status.BAD_REQUEST, "Bad target name: " + to);
        }
        if (modExists(to)) {
            return err(NanoHTTPD.Response.Status.CONFLICT, "Mod already exists: " + to);
        }

        copyTree(modDir(from), modDir(to), new int[]{0});

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("mod", to);
        return ok(result);
    }

    private static void copyTree(File from, File to, int[] count) throws IOException {
        if (count[0]++ > 5000) {
            throw new IOException("too many files to copy");
        }
        if (from.isDirectory()) {
            if (!to.exists() && !to.mkdirs()) {
                throw new IOException("cannot create dir " + to);
            }
            File[] files = from.listFiles();
            if (files != null) {
                for (File file : files) {
                    copyTree(file, new File(to, file.getName()), count);
                }
            }
            return;
        }
        try (FileInputStream in = new FileInputStream(from);
             FileOutputStream out = new FileOutputStream(to)) {
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) > 0) {
                out.write(chunk, 0, n);
            }
        }
    }

    private static NanoHTTPD.Response handleDelete(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String name = payload.optString("name", "");

        if (!isValidModName(name) || !modExists(name)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + name);
        }
        if (name.equals(ModdingBase.REMIXED)) {
            return err(NanoHTTPD.Response.Status.FORBIDDEN, "The built-in mod cannot be deleted");
        }

        // mirrors WndModSelect.onDelete: if the mod is active its saves go
        // with it - stash-restore to Remixed and restart, all on the game
        // thread. Restart happens AFTER the response is written (sleep).
        if (name.equals(GamePreferences.activeMod())) {
            GameLoop.pushUiTask(() -> {
                try {
                    Thread.sleep(250);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                FileSystem.deleteRecursive(modDir(name));
                SaveUtils.deleteGameAllClasses();
                SaveUtils.copyAllClassesFromSlot(ModdingBase.REMIXED);
                GamePreferences.activeMod(ModdingBase.REMIXED);
                RemixedDungeon.instance().doRestart();
            });
        } else {
            GameLoop.pushUiTask(() -> FileSystem.deleteRecursive(modDir(name)));
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        return ok(result);
    }

    private static NanoHTTPD.Response handleSave(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String modName = payload.optString("mod", "");
        String file = payload.getString("file");
        String content = payload.getString("content");

        // main-mod check first: on non-dev builds mods/Remixed does not even
        // exist on disk, and "read-only" beats "unknown" as the error
        if (!isValidModName(modName) || !writableMod(modName)) {
            return err(NanoHTTPD.Response.Status.FORBIDDEN,
                "The built-in mod is read-only on this build");
        }
        if (!modExists(modName)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
        }
        if (!BaseWebServer.isSafeResourcePath(file) || file.isEmpty()) {
            return BaseWebServer.forbiddenPath();
        }

        File dest = modFile(modName, file);
        if (!containedInMod(dest)) {
            return BaseWebServer.forbiddenPath();
        }

        String problem = validateContent(file, content);
        if (problem != null) {
            return err(NanoHTTPD.Response.Status.BAD_REQUEST, problem);
        }

        File parent = dest.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(content.getBytes(StandardCharsets.UTF_8));
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("file", modName + "/" + file);
        return ok(result);
    }

    // per-type validation, matching what the loaders actually accept
    private static String validateContent(String file, String content) {
        String lower = file.toLowerCase();

        if (lower.endsWith(".lua")) {
            return validateLua(file, content);
        }

        if (lower.startsWith("strings_") && lower.endsWith(".json")) {
            return validateStringsFile(file, content);
        }

        if (lower.endsWith(".json")) {
            return validateJsonContent(file, content);
        }

        return null;
    }

    // strict json first, then the hjson-lenient path the loaders use -
    // JsonHelper.readJsonFromStream itself never fails (returns an empty
    // object), so validation needs the throwing parsers directly
    private static String validateJsonContent(String file, String content) {
        try {
            new JSONObject(content);
            return null;
        } catch (JSONException strict) {
            try {
                Util.sanitizeJson(content);
                return null;
            } catch (Exception lenient) {
                return "Invalid JSON in " + file + ": " + strict.getMessage();
            }
        }
    }

    private static String validateLua(String file, String content) {
        final String[] problem = new String[1];
        boolean ran = GameLoop.pushUiTaskAndWait(() -> {
            try {
                LuaEngine.getGlobals().load(
                    new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)),
                    "=validate:" + file, "t", LuaEngine.getGlobals());
            } catch (Exception e) {
                problem[0] = "Lua error in " + file + ": " + e.getMessage();
            }
        });
        if (!ran) {
            return "game thread busy - lua not validated (save refused)";
        }
        return problem[0];
    }

    // strings files are per-line ["key","value"] pairs, NOT one json doc
    private static String validateStringsFile(String file, String content) {
        int lineNo = 0;
        for (String line : content.split("\n", -1)) {
            lineNo++;
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }
            try {
                JSONArray entry = new JSONArray(line);
                if (entry.length() < 2) {
                    return file + ":" + lineNo + " pair needs at least 2 elements";
                }
                entry.getString(0);
                entry.getString(1);
            } catch (JSONException e) {
                return file + ":" + lineNo + " malformed: " + e.getMessage();
            }
        }
        return null;
    }

    private static NanoHTTPD.Response handleSaveBinary(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String modName = payload.optString("mod", "");
        String file = payload.getString("file");
        String base64 = payload.getString("image");

        // main-mod check first: on non-dev builds mods/Remixed does not even
        // exist on disk, and "read-only" beats "unknown" as the error
        if (!isValidModName(modName) || !writableMod(modName)) {
            return err(NanoHTTPD.Response.Status.FORBIDDEN,
                "The built-in mod is read-only on this build");
        }
        if (!modExists(modName)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
        }
        if (!BaseWebServer.isSafeResourcePath(file) || file.isEmpty()) {
            return BaseWebServer.forbiddenPath();
        }
        if (!file.toLowerCase().endsWith(".png")) {
            return err(NanoHTTPD.Response.Status.BAD_REQUEST, "Only .png files are accepted");
        }

        byte[] bytes = BaseWebServer.instance.base64Decode(base64);

        // caveman: real PNG or nothing - same magic check as sprite saves
        if (bytes.length < 8 || (bytes[0] & 0xFF) != 0x89 || bytes[1] != 'P'
            || bytes[2] != 'N' || bytes[3] != 'G') {
            return err(NanoHTTPD.Response.Status.BAD_REQUEST, "Not a PNG payload");
        }

        File dest = modFile(modName, file);
        if (!containedInMod(dest)) {
            return BaseWebServer.forbiddenPath();
        }

        File parent = dest.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        try (FileOutputStream out = new FileOutputStream(dest)) {
            out.write(bytes);
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("file", modName + "/" + file);
        result.put("size", bytes.length);
        return ok(result);
    }

    private static NanoHTTPD.Response handleDeleteFile(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String modName = payload.optString("mod", "");
        String file = payload.getString("file");

        // main-mod check first: on non-dev builds mods/Remixed does not even
        // exist on disk, and "read-only" beats "unknown" as the error
        if (!isValidModName(modName) || !writableMod(modName)) {
            return err(NanoHTTPD.Response.Status.FORBIDDEN,
                "The built-in mod is read-only on this build");
        }
        if (!modExists(modName)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
        }
        if (!BaseWebServer.isSafeResourcePath(file) || file.isEmpty()) {
            return BaseWebServer.forbiddenPath();
        }

        File target = modFile(modName, file);
        if (!containedInMod(target)) {
            return BaseWebServer.forbiddenPath();
        }
        if (!target.exists()) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Not found: " + file);
        }

        FileSystem.deleteRecursive(target);

        JSONObject result = new JSONObject();
        result.put("success", true);
        return ok(result);
    }

    private static NanoHTTPD.Response handleRenameFile(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String modName = payload.optString("mod", "");
        String from = payload.getString("from");
        String to = payload.getString("to");

        // main-mod check first: on non-dev builds mods/Remixed does not even
        // exist on disk, and "read-only" beats "unknown" as the error
        if (!isValidModName(modName) || !writableMod(modName)) {
            return err(NanoHTTPD.Response.Status.FORBIDDEN,
                "The built-in mod is read-only on this build");
        }
        if (!modExists(modName)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
        }
        if (!BaseWebServer.isSafeResourcePath(from) || from.isEmpty()
            || !BaseWebServer.isSafeResourcePath(to) || to.isEmpty()) {
            return BaseWebServer.forbiddenPath();
        }

        File src = modFile(modName, from);
        File dst = modFile(modName, to);
        if (!containedInMod(src) || !containedInMod(dst)) {
            return BaseWebServer.forbiddenPath();
        }
        if (!src.exists()) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Not found: " + from);
        }
        if (dst.exists()) {
            return err(NanoHTTPD.Response.Status.CONFLICT, "Target exists: " + to);
        }

        File parent = dst.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        if (!src.renameTo(dst)) {
            return err(NanoHTTPD.Response.Status.INTERNAL_ERROR, "Rename failed");
        }

        JSONObject result = new JSONObject();
        result.put("success", true);
        return ok(result);
    }

    // mirrors WndModDescription's restart button: stash current saves to
    // their slot, bring the target mod's saves in, switch, restart. The
    // built-in Remixed mod is a valid switch target (like the mods window),
    // even though it has no writable dir.
    private static NanoHTTPD.Response handleSelect(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        final String name = payload.optString("name", "");

        boolean known = name.equals(ModdingBase.REMIXED)
            || (isValidModName(name) && modExists(name));
        if (!known) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + name);
        }

        final String prevMod = GamePreferences.activeMod();
        if (name.equals(prevMod)) {
            JSONObject result = new JSONObject();
            result.put("success", true);
            result.put("alreadyActive", true);
            return ok(result);
        }

        // restart nukes the process scene - run it after the response had a
        // chance to hit the wire (caveman sleep inside the task)
        GameLoop.pushUiTask(() -> {
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            SaveUtils.copyAllClassesToSlot(prevMod);
            SaveUtils.deleteGameAllClasses();
            SaveUtils.copyAllClassesFromSlot(name);
            GamePreferences.activeMod(name);
            RemixedDungeon.instance().doRestart();
        });

        JSONObject result = new JSONObject();
        result.put("success", true);
        result.put("restarting", true);
        return ok(result);
    }

    // ------------------------------------------------------------------
    // validation
    // ------------------------------------------------------------------

    private static NanoHTTPD.Response handleValidate(NanoHTTPD.IHTTPSession session) throws Exception {
        JSONObject payload = body(session);
        String modName = payload.optString("name", "");

        if (!isValidModName(modName) || !modExists(modName)) {
            return err(NanoHTTPD.Response.Status.NOT_FOUND, "Unknown mod: " + modName);
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();
        List<File> luaFiles = new ArrayList<>();
        List<File> jsonFiles = new ArrayList<>();
        collectFiles(modDir(modName), luaFiles, jsonFiles, new int[]{0});

        JSONObject stats = new JSONObject();
        stats.put("luaFiles", luaFiles.size());
        stats.put("jsonFiles", jsonFiles.size());

        // json: parse the way the game parses (lenient), strings separately
        for (File file : jsonFiles) {
            String rel = relativeTo(modDir(modName), file);
            if (rel.toLowerCase().startsWith("strings_")) {
                String problem = validateStringsFile(rel, new String(readFile(file), StandardCharsets.UTF_8));
                if (problem != null) {
                    errors.add(problem);
                }
                continue;
            }
            try {
                String problem = validateJsonContent(rel,
                    new String(readFile(file), StandardCharsets.UTF_8));
                if (problem != null) {
                    errors.add(problem);
                }
            } catch (Exception e) {
                errors.add(rel + ": " + e.getMessage());
            }
        }

        // lua: compile everything in one shot on the game thread
        if (!luaFiles.isEmpty()) {
            final List<String> luaErrors = new ArrayList<>();
            GameLoop.pushUiTaskAndWait(() -> {
                for (File file : luaFiles) {
                    String rel = relativeTo(modDir(modName), file);
                    try {
                        LuaEngine.getGlobals().load(
                            new ByteArrayInputStream(readFile(file)),
                            "=validate:" + rel, "t", LuaEngine.getGlobals());
                    } catch (Exception e) {
                        luaErrors.add(rel + ": " + e.getMessage());
                    }
                }
            });
            errors.addAll(luaErrors);
        }

        validateMobChains(modName, errors);
        validateLevels(modName, errors, warnings);

        JSONObject result = new JSONObject();
        result.put("success", errors.isEmpty());
        result.put("errors", new JSONArray(errors));
        result.put("warnings", new JSONArray(warnings));
        result.put("stats", stats);
        return ok(result);
    }

    private static void collectFiles(File dir, List<File> luaFiles, List<File> jsonFiles, int[] count) {
        File[] files = dir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (count[0]++ > 5000) {
                return;
            }
            if (file.isDirectory()) {
                collectFiles(file, luaFiles, jsonFiles, count);
            } else if (file.getName().toLowerCase().endsWith(".lua")) {
                luaFiles.add(file);
            } else if (file.getName().toLowerCase().endsWith(".json")) {
                jsonFiles.add(file);
            }
        }
    }

    // mobsDesc/<Kind>.json -> spritesDesc/<Kind>.json -> texture png chain
    private static void validateMobChains(String modName, List<String> errors) {
        File mobsDir = modFile(modName, "mobsDesc");
        File[] files = mobsDir.listFiles();
        if (files == null) {
            return;
        }
        for (File file : files) {
            if (!file.getName().toLowerCase().endsWith(".json")) {
                continue;
            }
            String rel = "mobsDesc/" + file.getName();
            try {
                JSONObject mob = JsonHelper.readJsonFromStream(new FileInputStream(file), rel);
                if (mob.length() == 0) {
                    continue;
                }
                String spriteDesc = mob.optString("spriteDesc", "spritesDesc/" + file.getName());
                if (!modResourceExists(modName, spriteDesc)) {
                    errors.add(rel + ": spriteDesc not found: " + spriteDesc);
                    continue;
                }
                JSONObject sprite = JsonHelper.readJsonFromStream(
                    new ByteArrayInputStream(readModResource(modName, spriteDesc)), spriteDesc);
                String texture = sprite.optString("texture", "");
                if (!texture.isEmpty() && !modResourceExists(modName, texture)) {
                    errors.add(spriteDesc + ": texture not found: " + texture);
                }
                String scriptFile = mob.optString("scriptFile", "");
                if (!scriptFile.isEmpty() && !modResourceExists(modName, scriptFile)) {
                    errors.add(rel + ": scriptFile not found: " + scriptFile);
                }
            } catch (Exception e) {
                errors.add(rel + ": " + e.getMessage());
            }
        }
    }

    private static void validateLevels(String modName, List<String> errors, List<String> warnings) {
        File dungeonJson = modFile(modName, "levelsDesc/Dungeon.json");
        if (!dungeonJson.isFile()) {
            return;
        }
        try {
            JSONObject campaign = JsonHelper.readJsonFromStream(
                new FileInputStream(dungeonJson), "levelsDesc/Dungeon.json");
            if (campaign.length() == 0 || !campaign.has("Levels")) {
                warnings.add("levelsDesc/Dungeon.json has no Levels table");
                return;
            }
            JSONObject levels = campaign.getJSONObject("Levels");
            // keys(), not keySet(): android's org.json gains keySet only at
            // API 26, the minSdk is 23
            for (java.util.Iterator<String> it = levels.keys(); it.hasNext(); ) {
                String levelId = it.next();
                JSONObject level = levels.optJSONObject(levelId);
                if (level == null) {
                    continue;
                }
                String file = level.optString("file", "");
                if (file.isEmpty()) {
                    // PredesignedLevel and RandomLevel both construct from a
                    // level file - DungeonGenerator.getString("file") throws
                    // without one
                    String kind = level.optString("kind", "");
                    if ("PredesignedLevel".equals(kind) || "RandomLevel".equals(kind)) {
                        errors.add("level " + levelId + ": kind " + kind + " requires a \"file\"");
                    }
                    continue;
                }
                if (!modResourceExists(modName, file)) {
                    errors.add("level " + levelId + ": file not found: " + file);
                    continue;
                }
                try {
                    JSONObject map = JsonHelper.readJsonFromStream(
                        new ByteArrayInputStream(readModResource(modName, file)), file);
                    if (map.length() == 0 || !map.has("map")) {
                        continue;
                    }
                    int w = map.optInt("width", 0);
                    int h = map.optInt("height", 0);
                    int expected = w * h;
                    int actual = map.getJSONArray("map").length();
                    if (expected != actual) {
                        errors.add(file + ": map length " + actual + " != width*height " + expected);
                    }
                } catch (Exception e) {
                    errors.add("level " + levelId + " (" + file + "): " + e.getMessage());
                }
            }
        } catch (Exception e) {
            errors.add("levelsDesc/Dungeon.json: " + e.getMessage());
        }
    }

    // existence: mod dir first, then the builtin/active-mod resource
    // overlay. lua script refs resolve with an implicit ".lua" suffix
    // ("scripts/mobs/X" -> "scripts/mobs/X.lua"), like the script loader
    private static boolean modResourceExists(String modName, String path) {
        if (!BaseWebServer.isSafeResourcePath(path) || path.isEmpty()) {
            return false;
        }
        if (FileSystem.getExternalStorageFile(modName + "/" + path).isFile()) {
            return true;
        }
        if (!path.toLowerCase().endsWith(".lua")
            && FileSystem.getExternalStorageFile(modName + "/" + path + ".lua").isFile()) {
            return true;
        }
        try {
            return ModdingMode.isResourceExists(path);
        } catch (Exception e) {
            return false;
        }
    }

    // content: mod dir first, then the resource overlay stream
    private static byte[] readModResource(String modName, String path) throws IOException {
        File f = FileSystem.getExternalStorageFile(modName + "/" + path);
        if (f.isFile() && BaseWebServer.isInsideStorageRootAbs(f)) {
            return readFile(f);
        }
        try (InputStream in = ModdingMode.getInputStream(path)) {
            return readStream(in);
        } catch (Exception e) {
            throw new IOException("not found: " + path);
        }
    }

    // ------------------------------------------------------------------
    // io helpers
    // ------------------------------------------------------------------

    private static byte[] readFile(File file) throws IOException {
        return readStream(new FileInputStream(file));
    }

    private static byte[] readStream(InputStream in) throws IOException {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream()) {
            byte[] chunk = new byte[8192];
            int n;
            while ((n = in.read(chunk)) > 0) {
                buffer.write(chunk, 0, n);
            }
            return buffer.toByteArray();
        } finally {
            in.close();
        }
    }

    private static String relativeTo(File root, File file) {
        String rootPath = root.getAbsolutePath() + File.separator;
        String filePath = file.getAbsolutePath();
        if (filePath.startsWith(rootPath)) {
            return filePath.substring(rootPath.length());
        }
        return file.getName();
    }
}
