package com.nyrds.android.lua;

/**
 * Created by mike on 16.06.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.nyrds.android.util.ModdingMode;
import com.nyrds.retrodungeon.ml.EventCollector;
import com.watabou.pixeldungeon.utils.GLog;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;

import java.io.InputStream;

public class LuaEngine implements ResourceFinder {

	static private LuaEngine engine = new LuaEngine();

	private Globals globals;

	public LuaValue call(String method) {
		return globals.get(method).call();
	}

	public LuaValue call(String method, Object arg1) {
		try {
			LuaValue methodForData = globals.get(method);
			return methodForData.call(CoerceJavaToLua.coerce(arg1));
		} catch (LuaError err) {
			reportLuaError(err);
		}
		return LuaValue.NIL;
	}

	public LuaValue call(String method, Object arg1, Object arg2) {
		try {
			LuaValue methodForData = globals.get(method);
			methodForData.call(CoerceJavaToLua.coerce(arg1),CoerceJavaToLua.coerce(arg2));
		} catch (LuaError err) {
			reportLuaError(err);
		}
		return LuaValue.NIL;
	}

	@Nullable
	public static LuaTable module(String module, String fallback) {
		LuaValue luaModule = getEngine().call("require", module);

		if(luaModule.istable()) {
			return luaModule.checktable();
		}

		EventCollector.logEvent("LuaError","failed to load lua module:",module);

		luaModule = getEngine().call("require", fallback);

		if(luaModule.istable()) {
			return luaModule.checktable();
		}

		EventCollector.logEvent("LuaError","failed to load fallback lua module:",fallback);
		return null;
	}

	private class resLoader extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(ModdingMode.getResource(x.tojstring()));
		}
	}

	public static LuaEngine getEngine() {
		return engine;
	}

	private LuaEngine() {
		globals = new Globals();
		globals.load(new JseBaseLib());
		globals.load(new PackageLib());
		globals.load(new Bit32Lib());
		globals.load(new TableLib());
		globals.load(new StringLib());
		globals.load(new CoroutineLib());
		globals.load(new JseMathLib());
		globals.load(new JseIoLib());
		globals.load(new JseOsLib());
		globals.load(new MultiDexLuajavaLib());
		LoadState.install(globals);
		LuaC.install(globals);

		globals.finder = this;
		globals.set("loadResource", new resLoader());
	}

	private void reportLuaError(LuaError err) {
		GLog.w(err.getMessage());
	}

	public LuaTable require(String module) {
		return LuaEngine.getEngine().call("require", module).checktable();
	}

	public void runScriptFile(@NonNull String fileName) {
		try {
			globals.loadfile(fileName).call();
		} catch (LuaError err) {
			reportLuaError(err);
		}
	}


	@Override
	public InputStream findResource(String filename) {
		return ModdingMode.getInputStream(filename);
	}

}
