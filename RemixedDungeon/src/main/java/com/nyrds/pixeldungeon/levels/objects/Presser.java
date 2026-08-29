package com.nyrds.pixeldungeon.levels.objects;

/**
 * Created by mike on 30.10.2016.
 */
public interface Presser {
	boolean affectLevelObjects();

	// caveman: only pressers that explicitly say so discharge traps
	// (Char/Item implement Presser - a bare instanceof check would arm
	// every mob and item)
	default boolean dischargesTraps() {
		return false;
	}
}
