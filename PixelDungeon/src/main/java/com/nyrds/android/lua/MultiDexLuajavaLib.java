package com.nyrds.android.lua;

import android.util.Log;

import com.nyrds.android.util.TrackedRuntimeException;

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
	protected Class classForName(String name) throws ClassNotFoundException {
		try {
			Log.i("MultiDexLuajavaLib",name);
			Class clazz = Class.forName(name, true, Thread.currentThread().getContextClassLoader());
			return clazz;
		} catch (ClassNotFoundException e) {
			throw new TrackedRuntimeException(e);
		}
	}
}
