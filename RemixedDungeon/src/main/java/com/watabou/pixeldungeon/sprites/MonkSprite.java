
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.Random;

public class MonkSprite extends MobSprite {
	
	private final Animation kick;
	
	public MonkSprite() {
		super();
		
		texture( Assets.MONK );
		
		TextureFilm frames = new TextureFilm( texture, 15, 14 );
		
		idle = new Animation( 6, true );
		idle.frames( frames, 1, 0, 1, 2 );
		
		run = new Animation( 15, true );
		run.frames( frames, 11, 12, 13, 14, 15, 16 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 3, 4, 3, 4 );
		
		kick = new Animation( 10, false );
		kick.frames( frames, 5, 6, 5 );
		
		die = new Animation( 15, false );
		die.frames( frames, 1, 7, 8, 8, 9, 10 );
		
		play( idle );
	}
	
	@Override
	public void attack( int cell ) {
		super.attack( cell );
		if (Random.Float() < 0.5f) {
			play( kick );
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		super.onComplete( anim == kick ? attack : anim );
	}
}
