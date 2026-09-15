package com.watabou.pixeldungeon;

import com.nyrds.lua.LuaEngine;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Java→lua bridge for quest logic living in scripts/lib/quest.lua.
 * Java keeps only mechanical trigger points (level decorate shims, level
 * layout gates); all quest decisions and state are lua-side and persist
 * via the scripts storage bundle node.
 */
public class QuestBridge {

	/**
	 * Once-per-run quest spawn gate + roll. Records spawned/alternative in
	 * lua game storage; callers place the mob themselves via MobFactory.
	 */
	public static boolean trySpawn(String quest, int rollBase) {
		LuaValue trySpawn = LuaEngine.require(LuaEngine.SCRIPTS_LIB_QUEST).get("trySpawn");
		return trySpawn.call(CoerceJavaToLua.coerce(quest), CoerceJavaToLua.coerce(rollBase)).optboolean(false);
	}

	public static boolean isCompleted(String quest) {
		return LuaEngine.require(LuaEngine.SCRIPTS_LIB_QUEST).get("isCompleted").call(quest).optboolean(false);
	}
}
