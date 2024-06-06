package com.nyrds.platform.lua;

import com.nyrds.platform.app.RemixedDungeonApp;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingMode;

import org.luaj.vm2.lib.jse.LuajavaLib;

import org.apache.commons.collections4.map.HashedMap;
import java.util.Map;

/**
 * Created by mike on 01.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class PlatformLuajavaLib extends LuajavaLib {


	final static Map<String, String> classRemap = new HashedMap<>();

	static {
		classRemap.put("com.watabou.pixeldungeon.RemixedDungeon", "com.nyrds.platform.game.RemixedDungeon");
		classRemap.put("com.watabou.noosa.audio.Sample", "com.nyrds.platform.audio.Sample");
		classRemap.put("com.watabou.noosa.audio.Music", "com.nyrds.platform.audio.Music");
		classRemap.put("com.watabou.noosa.StringsManager", "com.nyrds.platform.util.StringsManager");
		classRemap.put("com.nyrds.platform.Input", "com.nyrds.platform.app.Input");
	}

	public PlatformLuajavaLib() {
		super();
	}

	@Override
	protected Class<?> classForName(String name) {
		ClassLoader classLoader = RemixedDungeonApp.getContext().getClassLoader();

		String actualClassName = name;

		if (classRemap.containsKey(name)) {
			actualClassName = classRemap.get(name);
		}

		try {
			Class<?> clazz = Class.forName(actualClassName, true, classLoader);
			return clazz;
		} catch (ClassNotFoundException e) {
			ModError.doReport("Failed to load class ["+classLoader.toString() + "] in mod "+ ModdingMode.activeMod(), e);
			return Object.class;
		}
	}
}
