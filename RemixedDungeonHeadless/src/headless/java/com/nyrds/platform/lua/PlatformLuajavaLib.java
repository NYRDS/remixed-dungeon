package com.nyrds.platform.lua;

import com.nyrds.lua.LuaSandbox;
import com.nyrds.util.ModError;
import com.nyrds.util.ModdingBase;

import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;
import org.luaj.vm2.lib.jse.LuajavaLib;

import java.lang.reflect.Array;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by mike on 01.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public class PlatformLuajavaLib extends LuajavaLib {


	final static Map<String, String> classRemap = new HashMap<>();

	static {
		classRemap.put("com.watabou.pixeldungeon.RemixedDungeon", "com.nyrds.platform.game.RemixedDungeon");
		classRemap.put("com.watabou.noosa.audio.Sample", "com.nyrds.platform.audio.Sample");
		classRemap.put("com.watabou.noosa.audio.Music", "com.nyrds.platform.audio.MusicManager");
		classRemap.put("com.watabou.noosa.StringsManager", "com.nyrds.platform.util.StringsManager");
		classRemap.put("com.nyrds.platform.Input", "com.nyrds.platform.app.Input");
		classRemap.put("com.nyrds.platform.audio.Music","com.nyrds.platform.audio.MusicManager");
	}

	public PlatformLuajavaLib() {
		super();
	}

	@Override
	public Varargs invoke(Varargs args) {
		Varargs result = super.invoke(args);
		if (opcode == 0) { // INIT call returns the installed luajava table
			LuaValue luajava = result.arg1();
			if (luajava.istable() && luajava.get("createProxy").isnil()) {
				luajava.set("createProxy", new CreateProxy());
			}
		}
		return result;
	}

	// luajava.createProxy, dropped from the luaj fork (TeaVM cannot implement
	// java.lang.reflect.Proxy), restored here for JVM builds only - published
	// mods still call it. Remove once mods migrate to LuaWndBagListener.
	private final class CreateProxy extends VarArgFunction {
		@Override
		public Varargs invoke(Varargs args) {
			try {
				final int niface = args.narg() - 1;
				if (niface <= 0)
					throw new LuaError("no interfaces");
				final LuaValue lobj = args.checktable(niface + 1);

				final Class[] ifaces = new Class[niface];
				for (int i = 0; i < niface; i++)
					ifaces[i] = classForName(args.checkjstring(i + 1));

				Object proxy = Proxy.newProxyInstance(getClass().getClassLoader(), ifaces, new ProxyHandler(lobj));

				return LuaValue.userdataOf(proxy);
			} catch (LuaError e) {
				throw e;
			} catch (Exception e) {
				throw new LuaError(e);
			}
		}
	}

	private static final class ProxyHandler implements InvocationHandler {
		private static final int METHOD_MODIFIERS_VARARGS = 0x80;

		private final LuaValue lobj;

		private ProxyHandler(LuaValue lobj) {
			this.lobj = lobj;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] margs) throws Throwable {
			String name = method.getName();
			LuaValue func = lobj.get(name);
			if (func.isnil())
				return null;
			boolean isvarargs = ((method.getModifiers() & METHOD_MODIFIERS_VARARGS) != 0);
			int n = margs != null ? margs.length : 0;
			LuaValue[] v;
			if (isvarargs) {
				Object o = margs[--n];
				int m = Array.getLength(o);
				v = new LuaValue[n + m];
				for (int i = 0; i < n; i++)
					v[i] = CoerceJavaToLua.coerce(margs[i]);
				for (int i = 0; i < m; i++)
					v[i + n] = CoerceJavaToLua.coerce(Array.get(o, i));
			} else {
				v = new LuaValue[n];
				for (int i = 0; i < n; i++)
					v[i] = CoerceJavaToLua.coerce(margs[i]);
			}
			// caveman: mod callback bugs (RemixedRPG v28 selectAmmo passes a string where
			// WndBag.Mode belongs) must not kill the frame thread - Crashlytics 723df8c1.
			// Report as mod error and treat as missing callback; void methods tolerate null.
			try {
				LuaValue result = func.invoke(v).arg1();
				return CoerceLuaToJava.coerce(result, method.getReturnType());
			} catch (LuaError e) {
				ModError.doReport("proxy callback " + name, e);
				return null;
			}
		}
	}

	@Override
	protected Class classForName(String name) {
		ClassLoader classLoader = this.getClass().getClassLoader();

		String actualClassName = name;

		if (classRemap.containsKey(name)) {
			actualClassName = classRemap.get(name);
		}

		LuaSandbox.warnIfNotAllowed(actualClassName, "<class>", "class");

		try {
			Class clazz = Class.forName(actualClassName, true, classLoader);
			return clazz;
		} catch (ClassNotFoundException e) {
			ModError.doReport("Failed to load class ["+classLoader.toString() + "] in mod "+ ModdingBase.activeMod(), e);
			return Object.class;
		}
	}
}
