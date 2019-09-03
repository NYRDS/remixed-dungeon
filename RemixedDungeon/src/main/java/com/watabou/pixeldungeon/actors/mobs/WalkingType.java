package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.TerrainFlags;

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
		switch (this) {
			case NORMAL:
			case ABSOLUTE:
				return passableCells(level)[cell] || TerrainFlags.is(level.getTileType(cell),TerrainFlags.TRAP);
			default:
				return passableCells(level)[cell];
		}
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
