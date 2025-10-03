package com.nyrds.util;

import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.utils.Utils;

import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class JsonHelper {

    @NotNull
    static public JSONObject tryReadJsonFromAssets(String fileName) {
        if (ModdingMode.isResourceExist(fileName)) {
            return readJsonFromAsset(fileName);
        }
        return new JSONObject();
    }

    @NotNull
    static public JSONObject readJsonFromAsset(String fileName) {
        return readJsonFromStream(ModdingMode.getInputStream(fileName), fileName);
    }

    @NotNull
    static public JSONObject readJsonFromFile(File file) {
        try {
            try {
                return readJsonFromStream(new FileInputStream(file), file.getPath());
            } catch (SecurityException e) {
                return new JSONObject();
            }
        } catch (Exception e) {
            ModError.doReport(file.getAbsolutePath(), e);
            return new JSONObject();
        }

    }


    public static JSONObject readJsonFromString(final String in) {
        return readJsonFromStream(new ByteArrayInputStream(in.getBytes()), "String");
    }

    @NotNull
    public static JSONObject readJsonFromStream(InputStream stream, String tag) {
        StringBuilder jsonDef = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new BOMInputStream(stream)))) {

            String line = reader.readLine();

            while (line != null) {
                jsonDef.append(line);
                line = reader.readLine();
            }
            reader.close();

            String sourceString = jsonDef.toString().strip();

            try {
                return Util.sanitizeJson(sourceString);
            } catch (Exception e) {
                EventCollector.logException(e, Utils.format("bad json, hjson sanitization failed in %s", tag));
            }

            try {
                return (JSONObject) new JSONTokener(sourceString).nextValue();
            } catch (Exception e) {
                EventCollector.logException(e, Utils.format("gson failed in %s", tag));
            }
        } catch (Exception e) {
            EventCollector.logException(e, Utils.format("failed to read json in %s", tag));
        }
        return new JSONObject();
    }

    public static void readStringSet(JSONObject desc, String field, Set<String> placeTo) throws JSONException {
        if (desc.has(field)) {
            JSONArray array = desc.getJSONArray(field);
            for (int i = 0; i < array.length(); ++i) {
                placeTo.add(array.getString(i));
            }
        }
    }

    public static void foreach(JSONObject root, fieldCallback cb) throws JSONException {
        Iterator<String> keys = root.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            cb.onField(root, key);
        }
    }

    public static Animation readAnimation(JSONObject root, String animKind, TextureFilm film, int offset) throws JSONException {
        JSONObject jsonAnim = root.getJSONObject(animKind);

        Animation anim = new Animation((float) jsonAnim.getDouble("fps"), jsonAnim.getBoolean("looped"));
        anim.name = animKind;
        JSONArray jsonFrames = jsonAnim.getJSONArray("frames");

        List<Integer> framesSeq = new ArrayList<>(jsonFrames.length());

        int nextFrame;

        for (int i = 0; (nextFrame = jsonFrames.optInt(i, -1)) != -1; ++i) {
            framesSeq.add(nextFrame);
        }

        anim.frames(film, framesSeq, offset);

        return anim;
    }

    public static void writeJson(JSONObject obj, String fileName) {
        try {
            OutputStream outputStream = FileSystem.getOutputStream(fileName + ".json");
            outputStream.write(obj.toString(2).getBytes());
            outputStream.close();
        } catch (IOException | SecurityException | JSONException e) {
            EventCollector.logException(e);
        }

    }

    public interface fieldCallback {
        void onField(JSONObject root, String key) throws JSONException;
    }
}
