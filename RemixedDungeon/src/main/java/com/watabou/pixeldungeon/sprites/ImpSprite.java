
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.npcs.Imp;
import com.watabou.pixeldungeon.effects.Speck;

public class ImpSprite extends MobSprite {
	
	public ImpSprite() {
		super();
		
		texture( Assets.IMP );
		
		TextureFilm frames = new TextureFilm( texture, 12, 14 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 
			0, 1, 2, 3, 0, 1, 2, 3, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 4, 4,
			0, 1, 2, 3, 0, 1, 2, 3, 0, 1, 3, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4, 0, 0, 0, 4, 4, 4, 4, 4, 4, 4, 4 );
		
		run = new Animation( 20, true );
		run.frames( frames, 0 );
		
		die = new Animation( 10, false );
		die.frames( frames, 0, 3, 2, 1, 0, 3, 2, 1, 0 );
		
		play( idle );
	}
	
	@Override
	public void link(Char ch ) {
		super.link( ch );
		
		if (ch instanceof Imp) {
			alpha( 0.4f );
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		if (anim == die) {
			
			emitter().burst( Speck.factory( Speck.WOOL ), 15 );
			killAndErase();
			
		} else {
			super.onComplete( anim );
		}
	}
}
