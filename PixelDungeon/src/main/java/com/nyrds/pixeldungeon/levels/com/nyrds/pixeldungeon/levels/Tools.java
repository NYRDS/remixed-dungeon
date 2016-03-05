package com.nyrds.pixeldungeon.levels.com.nyrds.pixeldungeon.levels;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;

import java.util.Arrays;

/**
 * Created by mike on 27.02.2016.
 */
public class Tools {

	public static void buildSquareMaze(Level level,final int roomStep) {
		int w = level.getWidth();
		int h = level.getHeight();

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {
				if (i == 0 || j == 0 || i == w-1 || j == h-1) {
					setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
					continue;
				}

				if (w-i <  roomStep || h-j < roomStep) {
					setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
					continue;
				}

				if ((i - 1) % roomStep == 0 && (j - 1) % roomStep == 0) {
					setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
					continue;
				}

				if ((i - 1) % roomStep == roomStep/2 && (j - 1) % roomStep == roomStep/2 ) {

					setCellIfEmpty(level, i, j, Terrain.PEDESTAL);
					continue;
				}

				if ((i - 1) % roomStep == 0) {

					if (TerrainFlags.is(level.map[level.cell(i - 1, j)], TerrainFlags.SOLID) ||
							TerrainFlags.is(level.map[level.cell(i + 1, j)], TerrainFlags.SOLID)) {
						setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
						continue;
					}

					setCellIfEmpty(level, i, j, Terrain.WALL);
					continue;
				}

				if((j-1)%roomStep==0) {
					if (TerrainFlags.is(level.map[level.cell(i, j-1)], TerrainFlags.SOLID) ||
							TerrainFlags.is(level.map[level.cell(i, j + 1)], TerrainFlags.SOLID)) {
						setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
						continue;
					}

					setCellIfEmpty(level, i, j, Terrain.WALL);
					continue;
				}

				setCellIfEmpty(level, i, j, Terrain.EMPTY);
			}
		}

		for (int i = 0; i < w; i++) {
			for (int j = 0; j < h; j++) {

				if (w-i <  roomStep || h-j < roomStep) {
					continue;
				}

				if ((i - 1) % roomStep == roomStep/2 && (j - 1) % roomStep == roomStep/2 ) {
					setCellIfEmpty(level, i, j, Terrain.EMBERS);
					if(level.getDistToNearestTerrain(i,j,Terrain.DOOR)!=level.getDistToNearestTerrain(i,j,Terrain.WALL)){
						int doorCell = level.getNearestTerrain(i, j, Terrain.WALL);
						level.set(doorCell,Terrain.DOOR);
						int secondDoorCell = level.getNearestTerrain(i, j, Terrain.WALL);
						if(level.getDistToNearestTerrain(secondDoorCell,Terrain.DOOR)>1) {
							level.set(secondDoorCell, Terrain.DOOR);
						}
						secondDoorCell = level.getNearestTerrain(i, j, Terrain.WALL);
						if(level.getDistToNearestTerrain(secondDoorCell,Terrain.DOOR)>1) {
							level.set(secondDoorCell, Terrain.DOOR);
						}
					}
					continue;
				}

			}
		}

		GameScene.updateMap();
	}

	private static void setCellIfEmpty(Level level, int x, int y, int terrain) {
		int cell = level.cell(x, y);
		if (Actor.findChar(cell) == null) {
			level.set(cell, terrain);
		}
	}

	public static void makeEmptyLevel(Level level) {
		int width = level.getWidth();
		int height = level.getHeight();

		Arrays.fill(level.map, Terrain.EMPTY);

		for (int i = 1; i < level.getWidth(); i++) {
			level.set(i, 1,        Terrain.WALL);
			level.set(i, height-1, Terrain.WALL);
		}

		for (int i = 1; i < height; i++) {
			level.set(1, i,        Terrain.WALL);
			level.set(width-1, i , Terrain.WALL);
		}

		level.entrance = level.cell(width/4,height/4);
		level.set(level.entrance,Terrain.ENTRANCE);

		level.exit = level.cell(width-width/4,height-height/4);
		level.set(level.exit,Terrain.LOCKED_EXIT);
	}
}