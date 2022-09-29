package com.nyrds.pixeldungeon.mechanics.actors;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
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

	@Keep
	public ScriptedActor() {
	}

	public ScriptedActor(String sSourceFile) {
		sourceFile = sSourceFile;
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);
	}

	@Override
	protected boolean act() {
		LuaTable actor = LuaEngine.require(sourceFile);

		boolean ret = actor.get("act").call().checkboolean();
		spend((float) actor.get("actionTime").call().checkdouble());

		return ret;
	}

	public void activate() {
		LuaTable actor = LuaEngine.require(sourceFile);
		actor.get("activate").call();
	}
}
