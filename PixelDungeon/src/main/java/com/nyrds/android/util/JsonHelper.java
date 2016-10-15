package com.nyrds.android.util;

import android.support.annotation.NonNull;

import com.watabou.noosa.Game;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class JsonHelper {

	@NonNull
	static public JSONObject tryReadFile(String fileName) {
		if(ModdingMode.isResourceExist(fileName)) {
			return readFile(fileName);
		}
		return new JSONObject();
	}

	@NonNull
	static public JSONObject readFile(String fileName) {
		try {
			InputStream stream = ModdingMode.getInputStream(fileName);
			StringBuilder jsonDef = new StringBuilder();

			BufferedReader reader = new BufferedReader(new InputStreamReader(stream));

			String line = reader.readLine();

			while (line != null) {
				jsonDef.append(line);
				line = reader.readLine();
			}
			reader.close();
			return (JSONObject) new JSONTokener(jsonDef.toString()).nextValue();
		} catch (JSONException e) {
			Game.toast(e.getLocalizedMessage());
			throw new TrackedRuntimeException(e);
		} catch (IOException e) {
			Game.toast(e.getLocalizedMessage());
			throw new TrackedRuntimeException(e);
		}
	}
}
