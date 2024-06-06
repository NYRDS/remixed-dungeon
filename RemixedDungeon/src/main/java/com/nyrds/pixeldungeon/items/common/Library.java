package com.nyrds.pixeldungeon.items.common;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.nyrds.platform.EventCollector;
import com.nyrds.platform.storage.FileSystem;
import com.nyrds.platform.util.TrackedRuntimeException;
import com.nyrds.util.JsonHelper;
import com.nyrds.util.ModdingMode;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.sprites.ItemSprite;
import com.watabou.pixeldungeon.ui.Window;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.pixeldungeon.windows.WndChar;
import com.watabou.pixeldungeon.windows.WndInfoItem;

import org.jetbrains.annotations.NotNull;

import java.io.OutputStream;
import java.util.Collections;
import org.apache.commons.collections4.map.HashedMap;
import java.util.Map;

import lombok.SneakyThrows;

/**
 * Created by mike on 30.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class Library {
	public static final String ITEM = "item";
	public static final String MOB = "mob";

	private static Map<String, Map<String, Integer>> mKnowledgeLevel = new HashedMap<>();

	private final static String  LIBRARY_FILE = "library.json";
	private static       boolean saveNeeded   = false;

	private static final Gson gson = new Gson();

	static {
		loadLibrary();
	}

	@SneakyThrows
	public static void saveLibrary() {
		if(!saveNeeded) {
			return;
		}
		saveNeeded = false;
		gson.toJson(mKnowledgeLevel);
		OutputStream output = FileSystem.getOutputStream(getLibraryFile());
		output.write(gson.toJson(mKnowledgeLevel).getBytes());
		output.close();
	}

	@Deprecated
	private static void loadOldLibrary() {
		try {
			if (FileSystem.getInternalStorageFile(LIBRARY_FILE).exists()) {
				mKnowledgeLevel = gson.fromJson(
						JsonHelper.readJsonFromStream(FileSystem.getInputStream(LIBRARY_FILE), LIBRARY_FILE).toString(),
						new TypeToken<Map<String, Map<String, Integer>>>() {}.getType());
			}
		} catch (Exception e) {
			EventCollector.logException(e,"library restore failed");
		}
	}

	private static void loadLibrary() {
		mKnowledgeLevel = new HashedMap<>();
		try {
			String libraryFile = getLibraryFile();
			if (FileSystem.getInternalStorageFile(libraryFile).exists()) {
				mKnowledgeLevel = gson.fromJson(
						JsonHelper.readJsonFromStream(FileSystem.getInputStream(libraryFile), libraryFile).toString(),
						new TypeToken<Map<String, Map<String, Integer>>>() {
						}.getType()
				);
			}
		} catch (Exception e) {
			loadOldLibrary();
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
			mKnowledgeLevel.put(category, new HashedMap<>());
		}
		return mKnowledgeLevel.get(category);
	}

	public static Map<String, Integer> getKnowledgeMap(String category) {
		return Collections.unmodifiableMap(getCategory(category));
	}


	public static boolean isValidCategory(String category) {
		if(category.equals(ITEM)) {
			return true;
		}

		if(category.equals(MOB)) {
			return true;
		}

		return false;
	}

	@NotNull
	public static EntryHeader infoHeader(String category, String clazz) {
		if(category.equals(ITEM)) {
			if(ItemFactory.isValidItemClass(clazz)) {
				Item item = ItemFactory.itemByName(clazz);
				return new EntryHeader(
					Utils.capitalize(item.name()),
					new ItemSprite(item));
			}
		}

		if(category.equals(MOB)) {
			if(MobFactory.hasMob(clazz)) {
				Mob mob = MobFactory.mobByName(clazz);
				return new EntryHeader(
					Utils.capitalize(mob.getName()),
					mob.newSprite().avatar());
			}
		}

		return new EntryHeader(Utils.EMPTY_STRING,null);
	}

	public static Window infoWindow(String category, String clazz) {
		if(category.equals(ITEM)) {
			return new WndInfoItem(ItemFactory.itemByName(clazz));
		}

		if(category.equals(MOB)) {
			var mob = MobFactory.mobByName(clazz);
			return new WndChar(mob, mob);
		}
		throw new TrackedRuntimeException("unknown category: "+category);
	}

	public static String getLibraryFile() {
		return ModdingMode.activeMod() + "_" + LIBRARY_FILE;
	}

	public static class EntryHeader {
		public final String header;
		public final Image icon;

		public EntryHeader(String hdr, Image icn) {
			header = hdr;
			icon = icn;
		}
	}
}
