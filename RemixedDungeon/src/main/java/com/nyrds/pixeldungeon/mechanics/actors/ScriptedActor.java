package com.nyrds.pixeldungeon.mechanics.actors;

import androidx.annotation.Keep;

import com.nyrds.Packable;
import com.nyrds.lua.LuaEngine;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.utils.Bundle;

import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

/**
 * Created by mike on 05.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class ScriptedActor extends Actor {
	@Packable
	public String sourceFile;

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
	protected void act() {
		LuaTable actor = LuaEngine.require(sourceFile);

		boolean ret = actor.get("act").call().checkboolean();
		spend((float) actor.get("actionTime").call().checkdouble());
	}

	public boolean cellClicked(int cell) {
		LuaTable actor = LuaEngine.require(sourceFile);

		var func = actor.get("cellClicked");
		if (func.isfunction()) {
			return func.call(CoerceJavaToLua.coerce(cell)).checkboolean();
		}
		return false;
	}

	// caveman: per-frame tick (real-time). called every frame from Level.onStep, unlike act()
	// (which is turn-based and stalls when the hero is idle). Level scripts opt in by defining onStep.
	public void onStep() {
		LuaTable actor = LuaEngine.require(sourceFile);
		var func = actor.get("onStep");
		if (func.isfunction()) {
			func.call();
		}
	}

	public void activate() {
		LuaTable actor = LuaEngine.require(sourceFile);
		actor.get("activate").call();
	}
}
