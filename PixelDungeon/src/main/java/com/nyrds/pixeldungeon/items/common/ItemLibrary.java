package com.nyrds.pixeldungeon.items.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;

import org.json.JSONException;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 30.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class ItemLibrary {
	private static Map<String, Integer> mKnowledgeLevel = new HashMap<>();

	private static String libraryFile = "library.json";

	private static Gson gson = new Gson();

	static {
		loadLibrary();
	}

	private static void saveLibrary() {
		gson.toJson(mKnowledgeLevel);
		try {
			OutputStream output = new FileOutputStream(FileSystem.getInteralStorageFile(libraryFile));
			output.write(gson.toJson(mKnowledgeLevel).getBytes());
			output.close();
		} catch (IOException e) {
			throw new TrackedRuntimeException(e);
		}

	}

	private static void loadLibrary(){
		try {
			mKnowledgeLevel = gson.fromJson(
					JsonHelper.readJsonFromFile(FileSystem.getInteralStorageFile(libraryFile)).toString(),
					new TypeToken<HashMap<String, Integer>>() {
					}.getType()
			);
		} catch (JSONException e) {
			mKnowledgeLevel = new HashMap<>();
		}
	}

	static public void identify(String clazz) {
		int knowledgeLevel = getKnowledgeLevel(clazz);

		if(knowledgeLevel < 10) {
			mKnowledgeLevel.put(clazz, knowledgeLevel + 1);
			saveLibrary();
		}
	}

	private static int getKnowledgeLevel(String clazz) {
		int knowledgeLevel = 0;
		if (mKnowledgeLevel.containsKey(clazz)) {
			knowledgeLevel = mKnowledgeLevel.get(clazz);
		}
		return knowledgeLevel;
	}

	public static Map<String, Integer> getKnowledgeMap() {
		return Collections.unmodifiableMap(mKnowledgeLevel);
	}
}
