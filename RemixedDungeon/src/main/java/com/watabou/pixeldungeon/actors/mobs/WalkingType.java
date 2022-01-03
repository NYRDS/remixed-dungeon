package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.levels.objects.LevelObject;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;

/**
 * Created by mike on 26.04.2017.
 * This file is part of Remixed Pixel Dungeon.
 */
public enum WalkingType {
	NORMAL, WATER, WALL, GRASS, CHASM, ABSOLUTE;

	public boolean[] passableCells(Level level) {
		switch (this) {
			case NORMAL:
			case GRASS:
				return level.passable;
			case WATER:
				return level.water;
			case WALL:
				return level.solid;
			case CHASM:
				return level.pit;
			case ABSOLUTE:
				return level.allCells;
		}
		return level.passable;
	}

	public boolean canSpawnAt(Level level,int cell) {
		return canWalkOn(level, cell) && Actor.findChar(cell) == null;
	}

	public boolean canWalkOn(Level level,int cell) {
		final LevelObject topLevelObject = level.getTopLevelObject(cell);
		return ((passableCells(level)[cell] || level.avoid[cell]) && !level.pit[cell])
				&& (topLevelObject == null || topLevelObject.getLayer() < 0);
	}

	public int respawnCell(Level level) {
		switch (this) {
			case WATER:
				return level.randomRespawnCell(level.water);
			case WALL:
				return level.randomRespawnCell(level.solid);
			case CHASM:
				return level.randomRespawnCell(level.pit);
			case NORMAL:
			case GRASS:
			case ABSOLUTE:
			default:
				return level.randomRespawnCell();
		}
	}
}
