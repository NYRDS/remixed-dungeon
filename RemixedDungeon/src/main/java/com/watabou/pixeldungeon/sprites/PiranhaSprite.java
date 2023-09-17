
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.scenes.GameScene;

public class PiranhaSprite extends MobSprite {
	
	public PiranhaSprite() {
		super();
		
		texture( Assets.PIRANHA );
		
		TextureFilm frames = new TextureFilm( texture, 12, 16 );
		
		idle = new Animation( 8, true );
		idle.frames( frames, 0, 1, 2, 1 );
		
		run = new Animation( 20, true );
		run.frames( frames, 0, 1, 2, 1 );
		
		attack = new Animation( 20, false );
		attack.frames( frames, 3, 4, 5, 6, 7, 8, 9, 10, 11 );
		
		die = new Animation( 4, false );
		die.frames( frames, 12, 13, 14 );
		
		play( idle );
	}
	
	@Override
	public void onComplete( Animation anim ) {

		ch.ifPresent( chr -> {
		super.onComplete( anim );
		
		if (anim == attack) {
			GameScene.ripple( chr.getPos() );
		}});
	}
}
