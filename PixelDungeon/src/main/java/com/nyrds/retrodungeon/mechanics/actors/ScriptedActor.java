package com.nyrds.retrodungeon.mechanics.actors;

import com.nyrds.Packable;
import com.nyrds.android.lua.LuaEngine;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.utils.Bundle;

import org.luaj.vm2.LuaTable;

/**
 * Created by mike on 05.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class ScriptedActor extends Actor {
	@Packable
	private String sourceFile;

	public ScriptedActor() {}

	public ScriptedActor(String sSourceFile) {
		sourceFile = sSourceFile;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
	}

	@Override
	protected boolean act() {
		LuaTable actor = LuaEngine.getEngine().call("require",  sourceFile).checktable();

		boolean ret = actor.get("act").call().checkboolean();
		spend((float) actor.get("actionTime").call().checkdouble());

		return ret;
	}

	public void activate() {
		LuaTable actor = LuaEngine.getEngine().call("require",  sourceFile).checktable();
		actor.get("activate").call();
	}
}
