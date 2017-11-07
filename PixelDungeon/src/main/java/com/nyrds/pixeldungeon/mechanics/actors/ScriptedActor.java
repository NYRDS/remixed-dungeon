package com.nyrds.pixeldungeon.mechanics.actors;

import com.nyrds.Packable;
import com.nyrds.android.lua.LuaEngine;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.utils.Bundle;

/**
 * Created by mike on 05.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class ScriptedActor extends Actor {

	private IScriptedActor scriptedActor;

	@Packable
	private String sourceFile;

	public ScriptedActor(String sSourceFile) {
		sourceFile = sSourceFile;
		scriptedActor = fromSource(sourceFile);
	}

	@Override
	public void restoreFromBundle(Bundle bundle) {
		super.restoreFromBundle(bundle);

		scriptedActor=fromSource(sourceFile);
	}

	private IScriptedActor fromSource(String sourceFile) {
		return (IScriptedActor) LuaEngine.getEngine().call("require", sourceFile).checkuserdata(IScriptedActor.class);
	}

	@Override
	protected boolean act() {
		boolean ret = scriptedActor.act();
		spend(scriptedActor.actionTime());
		return ret;
	}
}
