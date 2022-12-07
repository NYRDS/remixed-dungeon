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

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.npc.NecromancerNPC;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.hero.HeroClass;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.npcs.WandMaker;
import com.watabou.pixeldungeon.effects.Halo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.Room.Type;
import com.watabou.pixeldungeon.levels.painters.NecroExitPainter;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class PrisonLevel extends RegularLevel {

	private static final String NECROMACNER_SPAWNED = "necromancerSpawned";
	private boolean necromancerSpawned = false;

	{
		color1 = 0x6a723d;
		color2 = 0x88924c;
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

		if(Dungeon.depth==7 && !necromancerSpawned) {
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
		
		for (int i=0; i < getWidth(); i++) {
			if (map[i] == Terrain.WALL &&  
				(map[i + getWidth()] == Terrain.EMPTY || map[i + getWidth()] == Terrain.EMPTY_SP) &&
				Random.Int( 6 ) == 0) {
				
				map[i] = Terrain.WALL_DECO;
			}
		}
		
		for (int i=getWidth(); i < getLength() - getWidth(); i++) {
			if (map[i] == Terrain.WALL && 
				map[i - getWidth()] == Terrain.WALL && 
				(map[i + getWidth()] == Terrain.EMPTY || map[i + getWidth()] == Terrain.EMPTY_SP) &&
				Random.Int( 3 ) == 0) {
				
				map[i] = Terrain.WALL_DECO;
			}
		}

		placeEntranceSign();

		if(Dungeon.depth == 7) {
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
			return Game.getVar(R.string.Prison_TileWater);
		default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.EMPTY_DECO:
			return Game.getVar(R.string.Prison_TileDescDeco);
		case Terrain.BOOKSHELF:
			return Game.getVar(R.string.Prison_TileDescBookshelf);
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
	
	private static class Torch extends Emitter {
		
		private int pos;
		
		public Torch( int pos ) {
			super();
			
			this.pos = pos;
			
			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 1, p.y + 3, 2, 0 );
			
			pour( FlameParticle.FACTORY, 0.15f );
			
			add( new Halo( 16, 0xFFFFCC, 0.2f ).point( p.x, p.y ) );
		}
		
		@Override
		public void update() {
			if (setVisible(Dungeon.visible[pos])) {
				super.update();
			}
		}
	}
}