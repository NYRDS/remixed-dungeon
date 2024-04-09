
package com.watabou.pixeldungeon.levels;

import android.opengl.GLES20;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.platform.util.StringsManager;
import com.watabou.noosa.Group;
import com.watabou.noosa.Scene;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.actors.mobs.npcs.Hedgehog;
import com.watabou.pixeldungeon.items.Torch;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

import javax.microedition.khronos.opengles.GL10;

public class HallsLevel extends RegularLevel {

	{
		minRoomSize = 6;
		
		viewDistance = Math.max( 25 - Dungeon.depth, 1 );
		_objectsKind = 4;
		
		color1 = 0x801500;
		color2 = 0xa68521;
	}
	
	@Override
	public void create() {
		addItemToSpawn( new Torch() );
		super.create();
	}

	@Override
	protected String tilesTexXyz() {
		return Assets.TILES_HALLS_XYZ;
	}

	@Override
	public String tilesTex() {
		return Assets.TILES_HALLS;
	}
	
	@Override
	public String waterTex() {
		return Assets.WATER_HALLS;
	}
	
	protected boolean[] water() {
		return Patch.generate(this, getFeeling() == Feeling.WATER ? 0.55f : 0.40f, 6 );
	}
	
	protected boolean[] grass() {
		return Patch.generate(this, getFeeling() == Feeling.GRASS ? 0.55f : 0.30f, 3 );
	}
	
	@Override
	protected void decorate() {
		
		for (int i=getWidth() + 1; i < getLength() - getWidth() - 1; i++) {
			if (map[i] == Terrain.EMPTY) { 
				
				int count = 0;
				for (int value : NEIGHBOURS8) {
					if ((TerrainFlags.flags[map[i + value]] & TerrainFlags.PASSABLE) > 0) {
						count++;
					}
				}
				
				if (Random.Int( 80 ) < count) {
					map[i] = Terrain.EMPTY_DECO;
				}
				
			} else
			if (map[i] == Terrain.WALL) {
				
				int count = 0;
				for (int value : NEIGHBOURS4) {
					if (map[i + value] == Terrain.WATER) {
						count++;
					}
				}
				
				if (Random.Int( 4 ) < count) {
					map[i] = Terrain.WALL_DECO;
				}
				
			}
		}

		placeEntranceSign();
	}
	
	@Override
	public String tileName( int tile ) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.Halls_TileWater);
            case Terrain.GRASS:
                return StringsManager.getVar(R.string.Halls_TileGrass);
            case Terrain.HIGH_GRASS:
                return StringsManager.getVar(R.string.Halls_TileHighGrass);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.Halls_TileStatue);
            default:
			return super.tileName( tile );
		}
	}
	
	@Override
	public String tileDesc(int tile) {
		switch (tile) {
		case Terrain.WATER:
            return StringsManager.getVar(R.string.Halls_TileDescWater);
            case Terrain.STATUE:
		case Terrain.STATUE_SP:
            return StringsManager.getVar(R.string.Halls_TileDescStatue);
            case Terrain.BOOKSHELF:
                return StringsManager.getVar(R.string.Halls_TileDescBookshelf);
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
			if (level.map[i] == 63) {
				scene.add( new Stream( i ) );
			}
		}
	}
	
	@Override
	protected void createMobs() {
		super.createMobs();
		
		Hedgehog.spawn(this);
	}
	
	private static class Stream extends Group {
		
		private final int pos;
		
		private float delay;
		
		public Stream( int pos ) {
			super();
			
			this.pos = pos;
			
			delay = Random.Float( 2 );
		}
		
		@Override
		public void update() {
			
			if (setVisible(Dungeon.isCellVisible(pos))) {
				
				super.update();
				
				if ((delay -= GameLoop.elapsed) <= 0) {
					
					delay = Random.Float( 2 );
					
					PointF p = DungeonTilemap.tileToWorld( pos );
					((FireParticle)recycle( FireParticle.class )).reset( 
						p.x + Random.Float( DungeonTilemap.SIZE ), 
						p.y + Random.Float( DungeonTilemap.SIZE ) );
				}
			}
		}
		
		@Override
		public void draw() {
			GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE );
			super.draw();
			GLES20.glBlendFunc( GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA );
		}
	}
	
	public static class FireParticle extends PixelParticle.Shrinking {
		
		public FireParticle() {
			super();
			
			color( 0xEE7722 );
			lifespan = 1f;
			
			acc.set( 0, +80 );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			
			left = lifespan;
			
			speed.set( 0, -40 );
			size = 4;
		}
		
		@Override
		public void update() {
			super.update();
			float p = left / lifespan;
			am = p > 0.8f ? (1 - p) * 5 : 1;
		}
	}
}
