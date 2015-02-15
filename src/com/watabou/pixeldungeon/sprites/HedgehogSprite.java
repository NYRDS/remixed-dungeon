package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;


public class HedgehogSprite extends MobSprite {
	public HedgehogSprite() {
		super();
		
		texture( Assets.HEDGEHOG );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 5, true );
		idle.frames( frames, 0, 0 );
		
		run = new Animation( 5, true );
		run.frames( frames, 0, 1, 2, 3 );
		
		die = new Animation( 20, false );
		die.frames( frames, 0 );
		
		play( idle );
	}
	
	private Emitter cloud;
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		
		if (cloud == null) {
			cloud = emitter();
			//cloud.pour( Speck.factory( Speck.PARALYSIS ), 0.7f );
			cloud.pour( Speck.factory( Speck.MIST ), 0.1f );
		}
	}
	
	@Override
	public void update() {
		
		super.update();
		
		if (cloud != null) {
			cloud.visible = visible;
		}
	}
	
	@Override
	public void die() {
		super.die();
		
		if (cloud != null) {
			cloud.on = false;
		}
	}
	
}
