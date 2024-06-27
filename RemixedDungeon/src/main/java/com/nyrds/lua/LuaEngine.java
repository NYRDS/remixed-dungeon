package com.nyrds.lua;

/**
 * Created by mike on 16.06.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

import com.nyrds.pixeldungeon.mechanics.LuaScript;
import com.nyrds.platform.lua.PlatformLuajavaLib;
import com.nyrds.util.ModdingMode;
import com.watabou.pixeldungeon.utils.GLog;

import org.apache.commons.io.input.BOMInputStream;
import org.jetbrains.annotations.NotNull;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaString;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.CoroutineLib;
import org.luaj.vm2.lib.DebugLib;
import org.luaj.vm2.lib.OneArgFunction;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.ResourceFinder;
import org.luaj.vm2.lib.StringLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseIoLib;
import org.luaj.vm2.lib.jse.JseOsLib;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import lombok.Synchronized;


public class LuaEngine implements ResourceFinder {

	public class traceback extends VarArgFunction {

		public static final String DetailsSeparator = "\n!!!!!!!!\n";

		public Varargs invoke(Varargs args) {

			String errMsg = args.arg1() + "\n" + globals.debuglib.traceback(1);
			if(stp!=null) {
				errMsg  += DetailsSeparator + stp.get("stacktrace").call();
			}
			GLog.toFile("\n%s\n",errMsg);

			return LuaString.valueOf(errMsg);
		}
	}

	public static final String    SCRIPTS_LIB_STORAGE = "scripts/lib/storage";
    public static final String    LUA_DATA = "luaData";
	public static final LuaTable  emptyTable = new LuaTable();

	static private      LuaEngine engine              = new LuaEngine();

    private final WeakHashMap<String, LuaTable> modules = new WeakHashMap<>();
    private final WeakHashMap<LuaScript, LuaTable> moduleInstance = new WeakHashMap<>();

    private final LuaValue stp;

	private final Globals globals;

	@Synchronized
	public static void reset() {
		engine = new LuaEngine();
	}

	static public LuaValue call(String method) {
		return getEngine().globals.get(method).call();
	}

	private LuaValue call(String method, Object arg1) {
		LuaValue methodForData = globals.get(method);
		return methodForData.call(CoerceJavaToLua.coerce(arg1));
	}

	@Synchronized
	static public LuaTable moduleInstance(LuaScript script, String module) {

		var moduleInstance = getEngine().moduleInstance;
		if(moduleInstance.containsKey(script)) {
			return moduleInstance.get(script);
		}

		LuaValue luaModule = getEngine().call("dofile", module+".lua");

		if(luaModule.istable()) {
			moduleInstance.put(script, luaModule.checktable());
			return moduleInstance.get(script);
		}

		throw new RuntimeException("failed to load instance of lua module: "+module);
	}


	private static class resLoader extends OneArgFunction {
		public LuaValue call(LuaValue x) {
			return LuaValue.valueOf(ModdingMode.getResource(x.tojstring()));
		}
	}

	@Synchronized
	private static LuaEngine getEngine() {
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
		globals.load(new RpdMathLib());
		globals.load(new JseIoLib());
		globals.load(new JseOsLib());
		globals.load(new PlatformLuajavaLib());
		globals.load(new DebugLib());

		LoadState.install(globals);
		LuaC.install(globals);

		globals.running.errorfunc = new traceback();

		globals.finder = this;
		globals.set("loadResource", new resLoader());

		stp = call("require","scripts/lib/StackTracePlus");
		call("require","scripts/startup/quirks");
	}

	@Synchronized
	static public LuaTable require(String module) {
		var modules = getEngine().modules;

		if(modules.containsKey(module)) {
			return modules.get(module);
		}

		LuaValue luaModule = getEngine().call("require", module);

		if(luaModule.istable()) {
			modules.put(module, luaModule.checktable());
			return modules.get(module);
		}

		throw new RuntimeException("failed to load lua module: "+ module);
	}

	static public void runScriptFile(@NotNull String fileName) {
		getEngine().globals.loadfile(fileName).call();
	}

	@Override
	public InputStream findResource(String filename) {
		return new BOMInputStream(ModdingMode.getInputStream(filename));
	}

	static public void forEach(@NotNull LuaValue arg, LuaEntryAction action) {

		LuaTable tbl = arg.opttable(emptyTable);

		var k = LuaValue.NIL;
		while ( true ) {
			var n = tbl.next(k);
			if ( (k = n.arg1()).isnil() )
				break;
			action.apply(k, n.arg(2));
		}
	}
}
