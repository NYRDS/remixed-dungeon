package com.nyrds.android.lua;

/**
 * Created by mike on 16.06.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

import android.support.annotation.Nullable;

import com.nyrds.android.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;

import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseOsLib;

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

	private class resLoader extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(ModdingMode.getResource(x.tojstring()));
		}
	}

	;

	public void reset(@Nullable String scriptFile) {
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
		globals.set("log", new log());
		globals.set("loadResource", new resLoader());

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

	@Override
	public InputStream findResource(String filename) {
		return ModdingMode.getInputStream(filename);
	}

}
