package com.watabou.pixeldungeon.actors.mobs;

public enum Fraction {
	DUNGEON,
	NEUTRAL,
	HEROES,
	ANY;

	public boolean belongsTo(Fraction fr) {
		if(this.equals(ANY) || fr.equals(ANY)) {
			return true;
		}

		return this.equals(fr);
	}
}
