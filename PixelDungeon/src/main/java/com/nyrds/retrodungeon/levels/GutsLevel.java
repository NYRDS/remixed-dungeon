package com.nyrds.retrodungeon.levels;

import com.nyrds.retrodungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.noosa.Scene;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.pixeldungeon.levels.Patch;
import com.watabou.pixeldungeon.levels.RegularLevel;
import com.watabou.pixeldungeon.levels.Terrain;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.ColorMath;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class GutsLevel extends RegularLevel {

	public GutsLevel() {
		color1 = 0x534f3e;
		color2 = 0xb9d661;

		viewDistance = 6;
	}

	@Override
	public String tilesTexEx() {
		return Assets.TILES_GUTS;
	}

	@Override
	public String waterTex() {
		return Assets.WATER_GUTS;
	}

	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.60f : 0.45f, 6 );
	}

	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.55f : 0.35f, 3 );
	}

	@Override
	protected void decorate() {

		for (int i=0; i < getWidth(); i++) {
			if (map[i] == Terrain.WALL &&
					map[i + getWidth()] == Terrain.WATER &&
					Random.Int( 4 ) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		for (int i=getWidth(); i < getLength() - getWidth(); i++) {
			if (map[i] == Terrain.WALL &&
					map[i - getWidth()] == Terrain.WALL &&
					map[i + getWidth()] == Terrain.WATER &&
					Random.Int( 2 ) == 0) {

				map[i] = Terrain.WALL_DECO;
			}
		}

		for (int i=getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY) {

				int count =
						(map[i + 1] == Terrain.WALL ? 1 : 0) +
								(map[i - 1] == Terrain.WALL ? 1 : 0) +
								(map[i + getWidth()] == Terrain.WALL ? 1 : 0) +
								(map[i - getWidth()] == Terrain.WALL ? 1 : 0);

				if (Random.Int( 16 ) < count * count) {
					map[i] = Terrain.EMPTY_DECO;
				}
			}
		}
		placeEntranceSign();
	}

	@Override
	public int nMobs() {
		return 12 + Dungeon.depth % 5 + Random.Int( 4 );
	}


	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.GRASS:
			return Game.getVar(R.string.Guts_TileGrass);
		case Terrain.HIGH_GRASS:
			return Game.getVar(R.string.Guts_TileHighGrass);
		case Terrain.WATER:
			return Game.getVar(R.string.Guts_TileWater);
		default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc( int tile ) {
		switch (tile) {
		case Terrain.ENTRANCE:
			return Game.getVar(R.string.Guts_TileDescEntrance);
		case Terrain.EXIT:
			return Game.getVar(R.string.Guts_TileDescExit);
		case Terrain.HIGH_GRASS:
			return Game.getVar(R.string.Guts_TileHighGrass);
		case Terrain.WALL_DECO:
			return Game.getVar(R.string.Guts_TileDescDeco);
		case Terrain.BOOKSHELF:
			return Game.getVar(R.string.Guts_TileDescBookshelf);
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
				scene.add( new Sink( i ) );
			}
		}
	}

	private static class Sink extends Emitter {


		private int pos;
		private float rippleDelay = 0;

		private static final Emitter.Factory factory = new Factory() {

			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				WaterParticle p = (WaterParticle)emitter.recycle( WaterParticle.class );
				p.reset( x, y );
			}
		};

		public Sink( int pos ) {
			super();

			this.pos = pos;

			PointF p = DungeonTilemap.tileCenterToWorld( pos );
			pos( p.x - 2, p.y + 1, 4, 0 );

			pour( factory, 0.02f );
		}

		@Override
		public void update() {
			if (setVisible(Dungeon.visible[pos])) {

				super.update();

				if ((rippleDelay -= Game.elapsed) <= 0) {
					GameScene.ripple(pos + Dungeon.level.getWidth()).y -= DungeonTilemap.SIZE / 2;
					rippleDelay = Random.Float( 0.2f, 0.3f );
				}
			}
		}
	}

	public static final class WaterParticle extends PixelParticle {

		public WaterParticle() {
			super();

			acc.y = 50;
			am = 0.5f;

			color( ColorMath.random( 0xe6e600, 0x9fe05d ) );
			size( 2 );
		}

		public void reset( float x, float y ) {
			revive();

			this.x = x;
			this.y = y;

			speed.set( Random.Float( -2, +2 ), 0 );

			left = lifespan = 0.5f;
		}
	}
}