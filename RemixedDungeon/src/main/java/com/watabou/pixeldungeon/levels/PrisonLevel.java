/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package com.watabou.pixeldungeon.levels;

import com.nyrds.pixeldungeon.effects.emitters.Torch;
import com.nyrds.pixeldungeon.levels.LevelTools;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.npc.NecromancerNPC;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Scene;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.levels.painters.NecroExitPainter;
import com.watabou.utils.Random;

public class PrisonLevel extends RegularLevel {

	private boolean necromancerSpawned = false;

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
		_objectsKind = 1;
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_PRISON_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_PRISON;
	}

	@Override
	public String tilesTexEx() {
		return Assets.TILES_PRISON_X;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_PRISON;
	}
	
	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.65f : 0.45f, 4 );
	}
	
	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.60f : 0.40f, 3 );
	}

	@Override
	protected void assignRoomType() {
		super.assignRoomType();
		
		for (Room r : rooms) {
			if (r.type == Type.TUNNEL) {
				r.type = Type.PASSAGE;
			}
		}
	}
	
	@Override
	protected void createMobs() {
		super.createMobs();
		
		WandMaker.Quest.spawn( this, roomEntrance );

		if(Dungeon.depth==7 && !necromancerSpawned && hasExit(1)) {
			Room NecroExit = exitRoom(1);
			if(NecroExit!=null && Dungeon.heroClass != HeroClass.NECROMANCER) {
				NecromancerNPC.spawn(this, exitRoom(1));
				necromancerSpawned = true;
			}
		}
	}
	
	@Override
	protected void decorate() {
		
		for (int i=getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY) { 
				
				float c = 0.05f;
				if (map[i + 1] == Terrain.WALL && map[i + getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i - 1] == Terrain.WALL && map[i + getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i + 1] == Terrain.WALL && map[i - getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				if (map[i - 1] == Terrain.WALL && map[i - getWidth()] == Terrain.WALL) {
					c += 0.2f;
				}
				
				if (Random.Float() < c) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}

		LevelTools.northWallDecorate(this, 10, 5);

		placeEntranceSign();

		if(Dungeon.depth == 7 && hasExit(1)) {
			Room NecroExit = exitRoom(1);
			if(NecroExit!=null) {
				NecroExitPainter.paint(this, NecroExit);
			}
		}
		placeBarrels(Random.Int(5));
	}

	@Override
	public void onHeroDescend(int cell) {
		super.onHeroDescend(cell);

		if(isExit(cell)) {
			int index = exitIndex(cell);
			if(index == 1) {
				for(Mob mob:mobs) {
					if(mob instanceof NecromancerNPC) {
						mobs.remove(mob);
						break;
					}
				}
			}
		}
	}

	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.Prison_TileWater);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.EMPTY_DECO:
            return StringsManager.getVar(R.string.Prison_TileDescDeco);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.Prison_TileDescBookshelf);
            default:
			return super.tileDesc( tile );
		}
	}
	
	@Override
	public void addVisuals( Scene scene ) {
		super.addVisuals( scene );
		addVisuals( this, scene );
	}
	
	public static void addVisuals( Level level, Scene scene ) {
		for (int i=0; i < level.getLength(); i++) {
			if (level.map[i] == Terrain.WALL_DECO) {
				scene.add( new Torch( i ) );
			}
		}
	}
}