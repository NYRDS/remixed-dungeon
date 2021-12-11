/*
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.watabou.utils;

import com.nyrds.LuaInterface;
import com.nyrds.generated.BundleHelper;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.Util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PushbackInputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import lombok.SneakyThrows;

public class Bundle {

    private static final String CLASS_NAME = "__className";

    private static Map<String, String> aliases = new HashMap<>();

    private JSONObject data;

    public Bundle() {
        this(new JSONObject());
    }

    public Bundle(String data) throws JSONException {
        this.data = new JSONObject(data);
    }

    private Bundle(JSONObject data) {
        this.data = data;
    }

    public String serialize() {
        return data.toString();
    }

    public boolean isNull() {
        return data == null;
    }

    public boolean contains(String key) {
        return !data.isNull(key);
    }

    public boolean getBoolean(String key) {
        return data.optBoolean(key);
    }

    public boolean optBoolean(String key, boolean val) {
        return data.optBoolean(key, val);
    }

    public int getInt(String key) {
        return data.optInt(key);
    }

    public int optInt(String key, int val) {
        return data.optInt(key, val);
    }

    public float optFloat(String key, float val) {
        return (float) data.optDouble(key, val);
    }

    public float getFloat(String key) {
        return (float) data.optDouble(key);
    }

    public String optString(String key, String defVal) {
        return data.optString(key, defVal);
    }

    public String getString(String key) {
        return data.optString(key);
    }

    public Bundle getBundle(String key) {
        return new Bundle(data.optJSONObject(key));
    }

    @Nullable
    private Bundlable get() {
        String clName = "no_class";
        try {
            clName = optString(CLASS_NAME,clName);
            if (aliases.containsKey(clName)) {
                clName = aliases.get(clName);
            }

            Class<?> cl = Class.forName(clName);

            Bundlable object = (Bundlable) cl.newInstance();
            BundleHelper.UnPack(object, this);
            object.restoreFromBundle(this);
            return object;

        } catch (Exception e) {
            if(Util.isDebug()) {
                throw new TrackedRuntimeException(e);
            }
            EventCollector.logException(e, clName);
            return null;
        }
    }

    @NotNull
    public <T extends Bundlable> T opt(String key, T defaultValue) {
        Object ret = get(key);
        if(ret != null) {
            return (T)ret;
        }

        return defaultValue;
    }

    @Nullable
    public Bundlable get(String key) {
        JSONObject obj = data.optJSONObject(key);
        if (obj != null) {
            return new Bundle(obj).get();
        }
        return null;
    }


    public <E extends Enum<E>> E getEnum(String key, Class<E> enumClass, E defaultValue) {
        try {
            return Enum.valueOf(enumClass, data.getString(key));
        } catch (JSONException e) {
            EventCollector.logException(e);
            return defaultValue;
        }
    }

    public <E extends Enum<E>> E getEnum(String key, Class<E> enumClass) {
        return getEnum(key, enumClass, enumClass.getEnumConstants()[0]);
    }

    @NotNull
    public int[] getIntArray(String key) {
        try {
            JSONArray array = data.getJSONArray(key);

            int length = array.length();
            int[] result = new int[length];
            for (int i = 0; i < length; i++) {
                result[i] = array.optInt(i);
            }
            return result;
        } catch (JSONException e) {
            return new int[0];
        }
    }

    @NotNull
    public boolean[] getBooleanArray(String key) {
        try {
            JSONArray array = data.getJSONArray(key);
            int length = array.length();
            boolean[] result = new boolean[length];
            for (int i = 0; i < length; i++) {
                result[i] = array.getBoolean(i);
            }
            return result;
        } catch (JSONException e) {
            return new boolean[0];
        }
    }

    @NotNull
    public String[] getStringArray(String key) {
        try {
            JSONArray array = data.getJSONArray(key);
            int length = array.length();
            String[] result = new String[length];
            for (int i = 0; i < length; i++) {
                result[i] = array.getString(i);
            }
            return result;
        } catch (JSONException e) {
            return new String[0];
        }
    }

    @NotNull
    public <T extends Bundlable> Collection<T> getCollection(String key, Class<T> type) {
        List<T> list = new ArrayList<>();

        try {
            JSONArray array = data.getJSONArray(key);
            for (int i = 0; i < array.length(); i++) {
                Object storedObject = new Bundle(array.getJSONObject(i)).get();
                if (storedObject != null) {
                    list.add(type.cast(storedObject));
                }
            }
        } catch (JSONException e) {
            return new ArrayList<>();
        }

        return list;
    }

    @SneakyThrows
    public void put(String key, boolean value) {
        data.put(key, value);
    }

    @SneakyThrows
    public void put(String key, int value) {
        data.put(key, value);
    }

    @SneakyThrows
    public void put(String key, float value) {
        if(Float.isInfinite(value)) {
            value = Float.MAX_VALUE;
            EventCollector.logException(key+" is infinity");
        }

        if(Float.isNaN(value)) {
            value = Float.MAX_VALUE;
            EventCollector.logException(key+" is NaN");
        }

        data.put(key, value);
    }

    @SneakyThrows
    public void put(String key, String value) {
        data.put(key, value);
    }

    @SneakyThrows
    public void put(String key, Bundle bundle) {
        data.put(key, bundle.data);
    }

    @SneakyThrows
    public void put(String key, Bundlable object) {
        if (object != null && !object.dontPack()) {
            Bundle bundle = new Bundle();
            bundle.put(CLASS_NAME, object.getClass().getName());
            object.storeInBundle(bundle);
            BundleHelper.Pack(object, bundle);
            data.put(key, bundle.data);
        }
    }

    @LuaInterface
    public void putEntity(String key, Bundlable object) {
        put(key,object);
    }

    @SneakyThrows
    public void put(String key, Enum<?> value) {
        if (value != null) {
            data.put(key, value.name());
        }
    }

    @SneakyThrows
    public void put(String key, Integer[] array) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        data.put(key, jsonArray);
    }

    @SneakyThrows
    public void put(String key, int[] array) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        data.put(key, jsonArray);
    }

    @SneakyThrows
    public void put(String key, boolean[] array) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        data.put(key, jsonArray);
    }

    @SneakyThrows
    public void put(String key, String[] array) {
        JSONArray jsonArray = new JSONArray();
        for (int i = 0; i < array.length; i++) {
            jsonArray.put(i, array[i]);
        }
        data.put(key, jsonArray);
    }


    public void put(String key, Collection<? extends Bundlable> collection) {
        try {
            JSONArray array = new JSONArray();
            for (Bundlable object : collection) {
                if (!object.dontPack()) {
                    Bundle bundle = new Bundle();
                    bundle.put(CLASS_NAME, object.getClass().getName());
                    object.storeInBundle(bundle);
                    BundleHelper.Pack(object, bundle);
                    array.put(bundle.data);
                }
            }
            data.put(key, array);
        } catch (Exception e) {
            EventCollector.logException(e);
        }
    }

    @SneakyThrows
    public <T> void put(String key, Map<String, T> map) {
        JSONObject object = new JSONObject();
        for (Map.Entry<String,T> entry: map.entrySet()) {
            object.put(entry.getKey(),entry.getValue());
        }
        data.put(key,object);
    }

    @SneakyThrows
    public <T> Map<String,T> getMap(String key) {
        Map<String,T> ret = new HashMap<>();

        if(data.isNull(key)) {
            return ret;
        }

        JSONObject object = data.getJSONObject(key);

        Iterator<String> oi = object.keys();
        while(oi.hasNext()) {
            String name = oi.next();
            //noinspection unchecked
            ret.put(name, (T)object.get(name));
        }

        return ret;
    }

    private static final int GZIP_BUFFER_SIZE = 4096;

    public static Bundle read(InputStream stream) {

        try {
            BufferedReader reader;

            PushbackInputStream pb = new PushbackInputStream(stream, 2);

            byte[] header = new byte[2];
            pb.unread(header, 0, pb.read(header));

            if (header[0] == (byte) 0x1f && header[1] == (byte) 0x8b) {
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(pb, GZIP_BUFFER_SIZE)));
            } else {
                reader = new BufferedReader(new InputStreamReader(pb));
            }

            StringBuilder jsonDef = new StringBuilder();

            String line = reader.readLine();

            while (line != null) {
                jsonDef.append(line);
                line = reader.readLine();
            }
            reader.close();

            JSONObject json = (JSONObject) new JSONTokener(jsonDef.toString()).nextValue();

            return new Bundle(json);
        } catch (Exception e) {
            if (e instanceof IOException) {
                EventCollector.logEvent("save_io_exception");
            }
            EventCollector.logException(e);
            return new Bundle();
        }
    }

    @SneakyThrows
    public static void write(Bundle bundle, OutputStream stream) {
        if(Util.isDebug()) { //cleartext for debugging
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(stream));
            writer.write(bundle.data.toString(2));
            writer.close();
            return;
        }

        BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(stream, GZIP_BUFFER_SIZE)));
        writer.write(bundle.serialize());
        writer.close();
    }

    public static void addAlias(Class<?> cl, String alias) {
        aliases.put(alias, cl.getName());
    }
}
