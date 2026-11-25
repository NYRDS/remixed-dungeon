package com.watabou.pixeldungeon.actors;

import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.mobs.Mob;

/**
 * Created by mike on 27.05.2017.
 * This file is part of Remixed Pixel Dungeon.
 */

public class DelayedMobSpawner extends Actor {
	private final Mob mob;
	private final int cell;

	public DelayedMobSpawner(Mob mob, int cell){
		this.mob = mob;
		this.cell = cell;
	}

	@Override
	protected void act() {
		if(Actor.findChar(cell) == null) {
			CharUtils.appear(mob, cell);
		}
		remove(this);
	}
}
