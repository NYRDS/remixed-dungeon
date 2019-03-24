package com.nyrds.android.util;

import com.nyrds.pixeldungeon.ml.EventCollector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

import androidx.annotation.NonNull;

public class JsonHelper {

	@NonNull
	static public JSONObject tryReadJsonFromAssets(String fileName) {
		if (ModdingMode.isResourceExist(fileName)) {
			return readJsonFromAsset(fileName);
		}
		return new JSONObject();
	}

	@NonNull
	static public JSONObject readJsonFromAsset(String fileName) {
		try {
			return readJsonFromStream(ModdingMode.getInputStream(fileName));
		} catch (JSONException e) {
			throw ModdingMode.modException(e);
		}
	}

	@NonNull
	static public JSONObject readJsonFromFile(File file) throws JSONException {
		try {
			return readJsonFromStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			return new JSONObject();
		}
	}

	@NonNull
	public static JSONObject readJsonFromStream(InputStream stream) throws JSONException {
		try {
			StringBuilder jsonDef = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			String line = reader.readLine();

			while (line != null) {
				jsonDef.append(line);
				line = reader.readLine();
			}
			reader.close();

			Object value = new JSONTokener(jsonDef.toString()).nextValue();

			try {
				return (JSONObject) (value);
			} catch (ClassCastException e) {
				EventCollector.logException(e, value.toString());
				return new JSONObject();
			}
		} catch (IOException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	public static void readStringSet(JSONObject desc, String field, Set<String> placeTo) throws JSONException {
		if (desc.has(field)) {
			JSONArray array = desc.getJSONArray(field);
			for (int i = 0; i < array.length(); ++i) {
				placeTo.add(array.getString(i));
			}
		}
	}

}
