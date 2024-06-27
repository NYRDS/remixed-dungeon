
package com.watabou.pixeldungeon.sprites;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.Random;

public class SeniorSprite extends MobSprite {
	
	private final Animation kick;
	
	public SeniorSprite() {
		super();
		
		texture( Assets.MONK );
		
		TextureFilm frames = TextureCache.getFilm( texture, 15, 14 );
		
		idle = new Animation( 6, true );
		idle.frames( frames, 18, 17, 18, 19 );
		
		run = new Animation( 15, true );
		run.frames( frames, 28, 29, 30, 31, 32, 33 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 20, 21, 20, 21 );
		
		kick = new Animation( 10, false );
		kick.frames( frames, 22, 23, 22 );
		
		die = new Animation( 15, false );
		die.frames( frames, 18, 24, 25, 25, 26, 27 );
		
		play( idle );
	}
	
	@Override
	public void attack( int cell ) {
		super.attack( cell );
		if (Random.Float() < 0.3f) {
			play( kick );
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		super.onComplete( anim == kick ? attack : anim );
	}
}
