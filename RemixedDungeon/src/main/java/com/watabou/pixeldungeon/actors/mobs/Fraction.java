package com.watabou.pixeldungeon.actors.mobs;

public enum Fraction {
	DUNGEON,
	NEUTRAL,
	HEROES,
	ANY;

	public boolean belongsTo(Fraction fr) {
		return this==ANY || fr == ANY || this==fr;
	}

	public boolean isEnemy(Fraction fr) {
		switch (this) {
			case DUNGEON:
				return fr==HEROES;
			case NEUTRAL:
			case ANY:
				return false;
			case HEROES:
				return fr==DUNGEON;
		}
		return false;
	}
}
