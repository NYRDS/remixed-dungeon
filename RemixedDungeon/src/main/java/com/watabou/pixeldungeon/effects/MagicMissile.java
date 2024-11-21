
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Group;
import com.watabou.noosa.Image;
import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.Dungeon;
import com.nyrds.pixeldungeon.DungeonOptions;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.effects.particles.FlameParticle;
import com.watabou.pixeldungeon.effects.particles.LeafParticle;
import com.watabou.pixeldungeon.effects.particles.PoisonParticle;
import com.watabou.pixeldungeon.effects.particles.PurpleParticle;
import com.watabou.pixeldungeon.effects.particles.ShadowParticle;
import com.watabou.pixeldungeon.effects.particles.WoolParticle;
import com.nyrds.util.Callback;
import com.nyrds.util.ColorMath;
import com.nyrds.util.PointF;
import com.nyrds.util.Random;

import org.jetbrains.annotations.Nullable;

public class MagicMissile extends Emitter {

	private static final float SPEED	= 200f;

	@Nullable
	private Callback callback;
	
	private float sx;
	private float sy;
	private float time;
	
	public void reset( int from, int to, @Nullable Callback callback ) {
		if(Dungeon.isPathVisible(from,to)) {
			this.callback = callback;
			revive();

			PointF pf = DungeonTilemap.tileCenterToWorld(from);
			PointF pt = DungeonTilemap.tileCenterToWorld(to);

			if(DungeonOptions.isIsometricMode()) {
				pf.offset(0, Image.isometricModeShift);
				pt.offset(0, Image.isometricModeShift);
			}

			x = pf.x;
			y = pf.y;
			width = 0;
			height = 0;

			PointF d = PointF.diff(pt, pf);
			PointF speed = new PointF(d).normalize().scale(SPEED);
			sx = speed.x;
			sy = speed.y;
			time = d.length() / SPEED;
		} else {
			if (callback != null) {
				callback.call();
			}
		}
	}
	
	public void size( float size ) {
		x -= size / 2;
		y -= size / 2;
		width = height = size;
	}
	
	public static void blueLight( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.pour( MagicParticle.FACTORY, 0.01f );
	}
	
	public static void fire( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 4 );
		missile.pour( FlameParticle.FACTORY, 0.01f );
	}

	public static void ice( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 2 );
		missile.pour( ColdParticle.FACTORY, 0.01f );
	}
	
	public static void earth( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 2 );
		missile.pour( EarthParticle.FACTORY, 0.01f );
	}
	
	public static void purpleLight( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 2 );
		missile.pour( PurpleParticle.MISSILE, 0.01f );
	}
	
	public static void whiteLight( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 4 );
		missile.pour( WhiteParticle.FACTORY, 0.01f );
	}
	
	public static void wool( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 3 );
		missile.pour( WoolParticle.FACTORY, 0.01f );
	}
	
	public static void poison( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 3 );
		missile.pour( PoisonParticle.MISSILE, 0.01f );
	}
	
	public static void foliage( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 4 );
		missile.pour( LeafParticle.GENERAL, 0.01f );
	}
	
	public static void slowness( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.pour( SlowParticle.FACTORY, 0.01f );
	}
	
	public static void force( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 4 );
		missile.pour( ForceParticle.FACTORY, 0.01f );
	}
	
	public static void coldLight( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 4 );
		missile.pour( ColdParticle.FACTORY, 0.01f );
	}
	
	public static void shadow( Group group, int from, int to, Callback callback ) {
		MagicMissile missile = ((MagicMissile)group.recycle( MagicMissile.class ));
		missile.reset( from, to, callback );
		missile.size( 4 );
		missile.pour( ShadowParticle.MISSILE, 0.01f );
	}
	
	@Override
	public void update() {
		super.update();
		if (on) {
			float d = GameLoop.elapsed;
			x += sx * d;
			y += sy * d;
			if ((time -= d) <= 0) {
				on = false;
				if(callback!= null) {
					callback.call();
				}
			}
		}
	}
	
	public static class MagicParticle extends PixelParticle {
		
		public static final Emitter.Factory FACTORY = new Factory() {	
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((MagicParticle)emitter.recycle( MagicParticle.class )).reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};
		
		public MagicParticle() {
			color( 0x88CCFF );
			lifespan = 0.5f;
			
			speed.set( Random.Float( -10, +10 ), Random.Float( -10, +10 ) );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			
			left = lifespan;
		}
		
		@Override
		public void update() {
			super.update();
			// alpha: 1 -> 0; size: 1 -> 4
			size( 4 - (am = left / lifespan) * 3 );
		}
	}
	
	public static class EarthParticle extends PixelParticle.Shrinking {
		
		public static final Emitter.Factory FACTORY = new Factory() {	
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((EarthParticle)emitter.recycle( EarthParticle.class )).reset( x, y );
			}
		};
		
		public EarthParticle() {
			lifespan = 0.5f;
			
			color( ColorMath.random( 0x555555, 0x777766 ) );
			
			acc.set( 0, +40 );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			
			left = lifespan;
			size = 4;
			
			speed.set( Random.Float( -10, +10 ), Random.Float( -10, +10 ) );
		}
	}
	
	public static class WhiteParticle extends PixelParticle {
		
		public static final Emitter.Factory FACTORY = new Factory() {	
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((WhiteParticle)emitter.recycle( WhiteParticle.class )).reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};
		
		public WhiteParticle() {
			lifespan = 0.4f;
			
			am = 0.5f;
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			
			left = lifespan;
		}
		
		@Override
		public void update() {
			super.update();
			// size: 3 -> 0
			size( (left / lifespan) * 3 );
		}
	}

	public static class SlowParticle extends PixelParticle {
		
		private Emitter emitter;
		
		public static final Emitter.Factory FACTORY = new Factory() {	
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((SlowParticle)emitter.recycle( SlowParticle.class )).reset( x, y, emitter );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};
		
		public SlowParticle() {
			lifespan = 0.6f;
			
			color( 0x664422 );
			size( 2 );
		}
		
		public void reset( float x, float y, Emitter emitter ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			this.emitter = emitter;
			
			left = lifespan;
			
			acc.set( 0 );
			speed.set( Random.Float( -20, +20 ), Random.Float( -20, +20 ) );
		}
		
		@Override
		public void update() {
			super.update();
			
			am = left / lifespan;
			acc.set( (emitter.x - getX()) * 10, (emitter.y - getY()) * 10 );
		}
	}
	
	public static class ForceParticle extends PixelParticle {
		
		public static final Emitter.Factory FACTORY = new Factory() {	
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((ForceParticle)emitter.recycle( ForceParticle.class )).reset( x, y );
			}
		};
		
		public ForceParticle() {
			lifespan = 0.6f;

			size( 4 );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			
			left = lifespan;
			
			acc.set( 0 );
			speed.set( Random.Float( -40, +40 ), Random.Float( -40, +40 ) );
		}
		
		@Override
		public void update() {
			super.update();
			
			am = (left / lifespan) / 2;
			acc.set( -speed.x * 10, -speed.y * 10 );
		}
	}
	
	public static class ColdParticle extends PixelParticle.Shrinking {
		
		public static final Emitter.Factory FACTORY = new Factory() {	
			@Override
			public void emit( Emitter emitter, int index, float x, float y ) {
				((ColdParticle)emitter.recycle( ColdParticle.class )).reset( x, y );
			}
			@Override
			public boolean lightMode() {
				return true;
			}
		};
		
		public ColdParticle() {
			lifespan = 0.6f;
			
			color( 0x2244FF );
		}
		
		public void reset( float x, float y ) {
			revive();
			
			this.setX(x);
			this.setY(y);
			
			left = lifespan;
			size = 8;
		}
		
		@Override
		public void update() {
			super.update();
			
			am = 1 - left / lifespan;
		}
	}
}
