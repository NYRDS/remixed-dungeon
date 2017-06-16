package com.nyrds.android.lua;

/**
 * Created by mike on 16.06.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

import android.support.annotation.Nullable;

import com.nyrds.android.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.LuajavaLib;

import java.io.InputStream;

public class LuaEngine implements ResourceFinder {

	static private LuaEngine engine = new LuaEngine();

	private Globals globals;

	public String call(String method) {
		return globals.get(method).call().tojstring();
	}

	private class log extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			GLog.i("lua: " + x.tojstring());
			return LuaValue.NIL;
		}
	}

	public void reset(@Nullable String scriptFile) {
		globals = new Globals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new LuajavaLib() {
	        @Override
	        protected Class classForName(String name) throws ClassNotFoundException {
		        return Class.forName(name, true, Thread.currentThread().getContextClassLoader());
	        }
        });

		globals.finder = this;
		globals.set("log", new log());

		if(scriptFile==null) {
			return;
		}

		try {
			globals.loadfile(scriptFile).call();
		} catch (LuaError err) {
			reportLuaError(err);
		}
	}

	public static LuaEngine getEngine() {
		return engine;
	}

	private LuaEngine() {
		reset(null);
	}

	private void reportLuaError(LuaError err) {
		GLog.w(err.getMessage());
	}

	synchronized public void runScriptFile(String fileName) {
		reset(fileName);
	}

	synchronized void runScriptText(String script) {
		try {
			globals.load(script, "user chunk").call();
		} catch (LuaError err) {
			reportLuaError(err);
		}
	}

	@Override
	public InputStream findResource(String filename) {
		return ModdingMode.getInputStream(filename);
	}

}
