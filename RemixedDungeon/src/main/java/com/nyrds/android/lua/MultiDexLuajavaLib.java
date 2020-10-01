package com.nyrds.android.lua;

import com.nyrds.android.RemixedDungeonApp;
import com.nyrds.android.util.ModError;
import com.nyrds.android.util.ModdingMode;

import org.luaj.vm2.lib.jse.LuajavaLib;

/**
 * Created by mike on 01.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class MultiDexLuajavaLib extends LuajavaLib {

	public MultiDexLuajavaLib() {
		super();
	}

	@Override
	protected Class classForName(String name) {
		ClassLoader classLoader = RemixedDungeonApp.getContext().getClassLoader();

		try {
			Class clazz = Class.forName(name, true, classLoader);
			return clazz;
		} catch (ClassNotFoundException e) {
			ModError.doReport("Failed to load class ["+classLoader.toString() + "] in mod "+ ModdingMode.activeMod(), e);
			return Object.class;
		}
	}
}
