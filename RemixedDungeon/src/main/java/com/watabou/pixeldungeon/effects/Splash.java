
package com.watabou.pixeldungeon.effects;

import com.watabou.noosa.particles.Emitter;
import com.watabou.noosa.particles.PixelParticle;
import com.watabou.pixeldungeon.DungeonTilemap;
import com.watabou.pixeldungeon.scenes.GameScene;
import com.watabou.utils.PointF;
import com.watabou.utils.Random;

public class Splash {
	
	public static void at( int cell, final int color, int n ) {
		at( DungeonTilemap.tileCenterToWorld( cell ), color, n );
	}
	
	public static void at( PointF p, final int color, int n ) {
		
		if (n <= 0) {
			return;
		}
		
		Emitter emitter = GameScene.emitter();
		emitter.pos( p );
		
		FACTORY.color = color;
		FACTORY.dir = -3.1415926f / 2;
		FACTORY.cone = 3.1415926f;
		emitter.burst( FACTORY, n );
	}
	
	public static void at( PointF p, final float dir, final float cone, final int color, int n ) {
		
		if (n <= 0) {
			return;
		}
		
		Emitter emitter = GameScene.emitter();
		emitter.pos( p );
		
		FACTORY.color = color;
		FACTORY.dir = dir;
		FACTORY.cone = cone;
		emitter.burst( FACTORY, n );
	}
	
	private static final SplashFactory FACTORY = new SplashFactory(); 
			
	private static class SplashFactory extends Emitter.Factory {

		public int color;
		public float dir;
		public float cone;
		
		@Override
		public void emit( Emitter emitter, int index, float x, float y ) {
			PixelParticle p = (PixelParticle)emitter.recycle( PixelParticle.Shrinking.class );
			
			p.reset( x, y, color, 4, Random.Float( 0.5f, 1.0f ) );
			p.speed.polar( Random.Float( dir - cone / 2, dir + cone / 2 ), Random.Float( 40, 80 ) );
			p.acc.set( 0, +100 );
		}
	}
}
