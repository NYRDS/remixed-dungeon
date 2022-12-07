package com.nyrds.retrodungeon.items.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nyrds.android.util.FileSystem;
import com.nyrds.android.util.JsonHelper;
import com.nyrds.android.util.ModdingMode;
import com.nyrds.android.util.TrackedRuntimeException;
import com.nyrds.retrodungeon.mobs.common.MobFactory;
import com.watabou.noosa.CompositeTextureImage;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndInfoItem;
import com.watabou.pixeldungeon.windows.WndInfoMob;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 30.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Library {
	public static final String ITEM = "item";
	public static final String MOB = "mob";

	private static Map<String, Map<String, Integer>> mKnowledgeLevel;

	private final static String  LIBRARY_FILE = "library.json";
	private static       boolean saveNeeded   = false;

	private static Gson gson = new Gson();

	static {
		loadLibrary();
	}

	public static void saveLibrary() {
		if(!saveNeeded) {
			return;
		}
		saveNeeded = false;
		gson.toJson(mKnowledgeLevel);
		try {
			OutputStream output = FileSystem.getOutputStream(LIBRARY_FILE);
			output.write(gson.toJson(mKnowledgeLevel).getBytes());
			output.close();
		} catch (IOException e) {
			throw new TrackedRuntimeException(e);
		}
	}

	private static void loadLibrary() {
		try {
			mKnowledgeLevel = gson.fromJson(
					JsonHelper.readJsonFromStream(FileSystem.getInputStream(LIBRARY_FILE)).toString(),
					new TypeToken<Map<String, Map<String, Integer>>>() {
					}.getType()
			);
		} catch (Exception e) {
			mKnowledgeLevel = new HashMap<>();
		}
	}

	static public void identify(String category, String clazz) {
		int knowledgeLevel = getKnowledgeLevel(category, clazz);

		if (knowledgeLevel < 10 ) {
			getCategory(category).put(clazz, knowledgeLevel + 1);
			saveNeeded = true;
		}
	}

	private static int getKnowledgeLevel(String category, String clazz) {
		int knowledgeLevel = 0;
		if (getCategory(category).containsKey(clazz)) {
			knowledgeLevel = getCategory(category).get(clazz);
		}
		return knowledgeLevel;
	}

	private static Map<String, Integer> getCategory(String category) {
		if(!mKnowledgeLevel.containsKey(category)) {
			mKnowledgeLevel.put(category, new HashMap<String, Integer>());
		}
		return mKnowledgeLevel.get(category);
	}

	public static Map<String, Integer> getKnowledgeMap(String category) {
		return Collections.unmodifiableMap(getCategory(category));
	}

	public static EntryHeader infoHeader(String category, String clazz) {
		EntryHeader ret = new EntryHeader();
		if(category.equals(ITEM)) {
			Item item = ItemFactory.itemByName(clazz);
			ret.header = Utils.capitalize(item.name());
			ret.icon = new ItemSprite(item);
			return ret;
		}

		if(category.equals(MOB)) {
			Mob mob = MobFactory.mobByName(clazz);
			ret.header = Utils.capitalize(mob.getName());
			ret.icon = new CompositeTextureImage();
			((CompositeTextureImage)ret.icon).copy(mob.sprite().avatar());
			return ret;
		}

		throw new TrackedRuntimeException("unknown category: "+category);
	}

	public static Window infoWindow(String category, String clazz) {
		if(category.equals(ITEM)) {
			return new WndInfoItem(ItemFactory.itemByName(clazz));
		}

		if(category.equals(MOB)) {
			return new WndInfoMob(MobFactory.mobByName(clazz), getKnowledgeLevel(category, clazz));
		}
		throw new TrackedRuntimeException("unknown category: "+category);
	}

	public static String getLibraryFile() {
		if(!ModdingMode.inMod()) {
			return LIBRARY_FILE;
		} else {
			return ModdingMode.activeMod() + "_" + LIBRARY_FILE;
		}
	}

	public static class EntryHeader {
		public String header;
		public Image icon;
	}
}
