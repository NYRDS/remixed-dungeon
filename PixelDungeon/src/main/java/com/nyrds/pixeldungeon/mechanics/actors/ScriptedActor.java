package com.nyrds.pixeldungeon.mechanics.actors;

import com.watabou.pixeldungeon.actors.Actor;

/**
 * Created by mike on 05.11.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class ScriptedActor extends Actor {

	IScriptedActor scriptedActor;

	public ScriptedActor(IScriptedActor sActor) {
		scriptedActor = sActor;
	}

	@Override
	protected boolean act() {
		boolean ret = scriptedActor.act();
		spend(scriptedActor.actionTime());
		return ret;
	}
}
