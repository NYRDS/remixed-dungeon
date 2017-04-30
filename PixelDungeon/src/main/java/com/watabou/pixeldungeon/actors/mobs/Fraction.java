package com.watabou.pixeldungeon.actors.mobs;

public enum Fraction {
	DUNGEON,
	NEUTRAL,
	HEROES,
	ANY;

	public boolean belongsTo(Fraction fr) {
		return this.equals(ANY) || fr.equals(ANY) || this.equals(fr);

	}
}
