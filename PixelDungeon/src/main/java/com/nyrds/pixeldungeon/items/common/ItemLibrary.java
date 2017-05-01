package com.nyrds.pixeldungeon.items.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.windows.WndInfoItem;
import com.watabou.pixeldungeon.windows.WndInfoMob;

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
	public static final String ITEM = "item";
	public static final String MOB = "mob";

	private static Map<String, Map<String, Integer>> mKnowledgeLevel;

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

	private static void loadLibrary() {
		try {
			mKnowledgeLevel = gson.fromJson(
					JsonHelper.readJsonFromFile(FileSystem.getInteralStorageFile(libraryFile)).toString(),
					new TypeToken<Map<String, Map<String, Integer>>>() {
					}.getType()
			);
		} catch (Exception e) {
			mKnowledgeLevel = new HashMap<>();
			mKnowledgeLevel.put(ITEM, new HashMap<String, Integer>());
			mKnowledgeLevel.put(MOB, new HashMap<String, Integer>());
		}
	}

	static public void identify(String category, String clazz) {
		int knowledgeLevel = getKnowledgeLevel(category, clazz);

		if (knowledgeLevel < 10) {
			mKnowledgeLevel.get(category).put(clazz, knowledgeLevel + 1);
			saveLibrary();
		}
	}

	private static int getKnowledgeLevel(String category, String clazz) {
		int knowledgeLevel = 0;
		if (mKnowledgeLevel.get(category).containsKey(clazz)) {
			knowledgeLevel = mKnowledgeLevel.get(category).get(clazz);
		}
		return knowledgeLevel;
	}

	public static Map<String, Integer> getKnowledgeMap(String category) {
		return Collections.unmodifiableMap(mKnowledgeLevel.get(category));
	}

	public static Window infoWnd(String category, String clazz) {
		if(category.equals(ITEM)) {
			return new WndInfoItem(ItemFactory.itemByName(clazz));
		}

		if(category.equals(MOB)) {
			return new WndInfoMob(MobFactory.mobByName(clazz));
		}
		throw new TrackedRuntimeException("unknown category: "+category);
	}
}
