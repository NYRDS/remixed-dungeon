
package com.watabou.pixeldungeon.sprites;

import com.nyrds.platform.audio.Sample;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;

public class BlacksmithSprite extends MobSprite {
	
	private Emitter emitter;
	
	public BlacksmithSprite() {
		super();
		
		texture( Assets.TROLL );
		
		TextureFilm frames = new TextureFilm( texture, 13, 16 );
		
		idle = new Animation( 15, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 1, 2, 2, 2, 3 );
		
		run = new Animation( 20, true );
		run.frames( frames, 0 );
		
		die = new Animation( 20, false );
		die.frames( frames, 0 );
		
		play( idle );
	}
	
	@Override
	public void link(Char ch ) {
		super.link( ch );
		
		emitter = new Emitter();
		emitter.autoKill = false;
		emitter.pos( getX() + 7, getY() + 12 );
		getParent().add( emitter );
	}
	
	@Override
	public void update() {
		super.update();
		
		if (emitter != null) {
			emitter.setVisible(getVisible());
		}
	}
	
	@Override
	public void onComplete( Animation anim ) {
		ch.ifPresent( chr -> {
			super.onComplete(anim);

			if (getVisible() && emitter != null && anim == idle) {
				emitter.burst(Speck.factory(Speck.FORGE), 3);
				float volume = 0.2f / (chr.distance(Dungeon.hero));
				Sample.INSTANCE.play(Assets.SND_EVOKE, volume, volume, 0.8f);
			}
		});
	}

}
