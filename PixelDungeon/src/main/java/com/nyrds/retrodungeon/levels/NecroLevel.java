package com.nyrds.retrodungeon.levels;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.common.MobSpawner;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.effects.Halo;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Room;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class NecroLevel extends RegularLevel {

	public NecroLevel() {
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 6;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_NECRO;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_NECRO;
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
			if (r.type == Room.Type.TUNNEL) {
				r.type = Room.Type.PASSAGE;
			}
		}
	}

	@Override
	protected void createMobs() {
		int pos = randomRespawnCell();
			while(Actor.findChar(pos) != null) {
				pos = randomRespawnCell();
			}
			MobSpawner.spawnJarOfSouls(this, pos);
		super.createMobs();
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
				return Game.getVar(R.string.Halls_TileDescBookshelf);
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
				scene.add( new Candle( i ) );
			}
		}
	}

	private static class Candle extends Emitter {

		private int pos;

		public Candle( int pos ) {
			super();

			this.pos = pos;

			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 1, p.y - 3, 2, 0 );

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