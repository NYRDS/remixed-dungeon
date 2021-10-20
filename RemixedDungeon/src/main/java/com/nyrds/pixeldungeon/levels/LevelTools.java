package com.nyrds.pixeldungeon.levels;

import com.nyrds.pixeldungeon.levels.objects.LevelObjectsFactory;
import com.nyrds.pixeldungeon.levels.objects.Trap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.levels.TerrainFlags;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

import lombok.var;

/**
 * Created by mike on 27.02.2016.
 */
public class LevelTools {

	public static void buildShadowLordMaze(Level level, final int roomStep) {
		int w = level.getWidth();
		int h = level.getHeight();

		if(level.cellValid(level.entrance)) {
			level.set(level.entrance, Terrain.EMPTY_DECO);
			level.set(level.getExit(0), Terrain.EMPTY_DECO);
		}
		level.entrance = level.INVALID_CELL;
		level.setExit(level.INVALID_CELL,0);

		int im = (int) (Math.floor((float)(w)/roomStep)*roomStep+2);
		int jm = (int) (Math.floor((float)(h)/roomStep)*roomStep+2);

		for (int i = 0; i <im; i++) {
			for (int j = 0; j <jm; j++) {
				if (i == 0 || j == 0 || i == im-1 || j == jm-1) {
					level.set(i, j, Terrain.WALL_DECO);
					continue;
				}

				if ((i - 1) % roomStep == 0 && (j - 1) % roomStep == 0) {
					level.set(i, j, Terrain.WALL_DECO);
					continue;
				}

				if ((i - 1) % roomStep == roomStep/2 && (j - 1) % roomStep == roomStep/2 ) {

					if(level.get(i, j)==Terrain.EMPTY) {
						level.putLevelObject(LevelObjectsFactory.createCustomObject(level, LevelObjectsFactory.PEDESTAL, level.cell(i,j)));
						//level.set(i, j, Terrain.PEDESTAL);
					}
					continue;
				}

				if ((i - 1) % roomStep == 0) {

					if (TerrainFlags.is(level.get(i - 1, j), TerrainFlags.SOLID) || TerrainFlags.is(level.get(i + 1, j), TerrainFlags.SOLID)) {
						setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
						continue;
					}

					setCellIfEmpty(level, i, j, Terrain.WALL);
					continue;
				}

				if((j-1)%roomStep==0) {
					if (TerrainFlags.is(level.get(i, j-1), TerrainFlags.SOLID) ||
							TerrainFlags.is(level.get(i, j + 1), TerrainFlags.SOLID)) {
						setCellIfEmpty(level, i, j, Terrain.WALL_DECO);
						continue;
					}

					setCellIfEmpty(level, i, j, Terrain.WALL);
					continue;
				}
			}
		}

		int[] doorPositions = {144, 87, 150, 93, 156, 264, 207, 270, 213, 276, 327, 333};
        for (int doorPosition : doorPositions) {
            level.set(doorPosition, Terrain.DOOR);
        }

		GameScene.updateMap();
	}

	private static void setCellIfEmpty(Level level, int x, int y, int terrain) {
		int cell = level.cell(x, y);
		if (Actor.findChar(cell) == null) {
			level.set(cell, terrain);
		}
	}

	public static void makeEmptyLevel(Level level, boolean randomEntrance) {
		int width = level.getWidth();
		int height = level.getHeight();

		for (int i = 1; i < width; i++) {
			for (int j = 1; j < height; j++) {
				level.set(i, j, Terrain.EMPTY);
			}
		}

		for (int i = 1; i < width; i++) {
			level.set(i, 1,        Terrain.WALL);
			level.set(i, height-1, Terrain.WALL);
		}

		for (int j = 1; j < height; j++) {
			level.set(1, j,        Terrain.WALL);
			level.set(width-1, j , Terrain.WALL);
		}

		int entrance = level.cell(width/4,height/4);
		int exit     = level.cell(width-width/4,height-height/4);

		if (randomEntrance) {
			entrance = level.getRandomTerrainCell(Terrain.EMPTY);

			do {
				exit = level.getRandomTerrainCell(Terrain.EMPTY);
			} while (level.distance(entrance,exit) < Math.max(width,height) / 4);

		}

		level.entrance = entrance;
		level.set(level.entrance, Terrain.ENTRANCE);

		level.setExit(exit,0);
		level.set(level.getExit(0), Terrain.EXIT);

		GameScene.updateMap();
	}

	public static void makeShadowLordLevel(Level level) {
		int width = level.getWidth();
		int height = level.getHeight();

		for (int i = 1; i < width; i++) {
			for (int j = 1; j < height; j++) {
				level.set(i, j, Terrain.EMPTY);
			}
		}

		for (int i = 1; i < width; i++) {
			level.set(i, 1,        Terrain.WALL);
			level.set(i, height-1, Terrain.WALL);
		}

		for (int j = 1; j < height; j++) {
			level.set(1, j,        Terrain.WALL);
			level.set(width-1, j , Terrain.WALL);
		}

		for (int i = width/4; i < width/2 + width/4; i++) {
			level.set(i, height/4,        Terrain.WALL);
			level.set(i, height/2 + height/4 - 1, Terrain.WALL);
		}

		for (int j = height/4; j < height/2 + height/4; j++) {
			level.set(width/4, j,        Terrain.WALL);
			level.set(width/2 + width/4 - 1, j , Terrain.WALL);
		}

		level.entrance = level.cell(width/4 + 1,height/4 + 1);
		level.set(level.entrance, Terrain.ENTRANCE);

		level.setExit(level.cell(width-width/4 + 1,height-height/4 + 1),0);
		level.set(level.getExit(0), Terrain.EXIT);

		GameScene.updateMap();
	}

	public static void upgradeMap(@NotNull Level level) {
		for(int i = 0; i< level.map.length; ++i){
			switch (level.map[i]) { // old saves compatibility
				case Terrain.BARRICADE:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(LevelObjectsFactory.createCustomObject(level, LevelObjectsFactory.BARRICADE, i));
				break;

				case Terrain.PEDESTAL:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(LevelObjectsFactory.createCustomObject(level,LevelObjectsFactory.PEDESTAL, i));
				break;

				case Terrain.STATUE:
					level.map[i] = Terrain.EMPTY;
				case Terrain.STATUE_SP:
					level.map[i] = Terrain.EMPTY_SP;
					level.putLevelObject(LevelObjectsFactory.createCustomObject(level,LevelObjectsFactory.STATUE, i));
				break;

				case Terrain.TOXIC_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.TOXIC_TRAP, false));
					break;

				case Terrain.SECRET_TOXIC_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.TOXIC_TRAP, true));
					break;

				case Terrain.FIRE_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.FIRE_TRAP, false));
					break;

				case Terrain.SECRET_FIRE_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.FIRE_TRAP, true));
					break;

				case Terrain.PARALYTIC_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.PARALYTIC_TRAP, false));
					break;

				case Terrain.SECRET_PARALYTIC_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.PARALYTIC_TRAP, true));
					break;

				case Terrain.POISON_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.POISON_TRAP, false));
					break;

				case Terrain.SECRET_POISON_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.POISON_TRAP, true));
					break;

				case Terrain.ALARM_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.ALARM_TRAP, false));
					break;

				case Terrain.SECRET_ALARM_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.ALARM_TRAP, true));
					break;

				case Terrain.LIGHTNING_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.LIGHTNING_TRAP, false));
					break;

				case Terrain.SECRET_LIGHTNING_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.LIGHTNING_TRAP, true));
					break;

				case Terrain.GRIPPING_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.GRIPPING_TRAP, false));
					break;

				case Terrain.SECRET_GRIPPING_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.GRIPPING_TRAP, true));
					break;

				case Terrain.SUMMONING_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.SUMMONING_TRAP, false));
					break;

				case Terrain.SECRET_SUMMONING_TRAP:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(Trap.makeSimpleTrap(i, LevelObjectsFactory.SUMMONING_TRAP, true));
					break;

				case Terrain.INACTIVE_TRAP:
					level.map[i] = Terrain.EMPTY;
					var trap = Trap.makeSimpleTrap(i, LevelObjectsFactory.ALARM_TRAP, false);
					trap.deactivate();
					level.putLevelObject(trap);
					break;
				case Terrain.WELL:
				case Terrain.EMPTY_WELL:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(LevelObjectsFactory.createCustomObject(level, LevelObjectsFactory.WELL, i));
					break;
				case Terrain.ALCHEMY:
					level.map[i] = Terrain.EMPTY;
					level.putLevelObject(LevelObjectsFactory.createCustomObject(level, LevelObjectsFactory.POT, i));
					break;

			}
		}
	}

    public static void northWallDecorate(Level level, int floorDecoRate, int wallDecoRate) {
        for (int i = 0; i < level.getLength()- level.getWidth(); i++) {
            if (level.map[i] == Terrain.EMPTY && Random.Int( floorDecoRate ) == 0) {
                level.map[i] = Terrain.EMPTY_DECO;
            } else if (level.map[i] == Terrain.WALL && TerrainFlags.is(level.map[i+ level.getWidth()],TerrainFlags.PASSABLE) && Random.Int( wallDecoRate ) == 0) {
                level.map[i] = Terrain.WALL_DECO;
            }
        }
    }
}