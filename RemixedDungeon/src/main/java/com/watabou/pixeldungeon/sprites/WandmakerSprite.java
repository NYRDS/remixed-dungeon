
package com.watabou.pixeldungeon.sprites;

import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.particles.ElmoParticle;
import com.watabou.pixeldungeon.scenes.GameScene;

public class WandmakerSprite extends MobSprite {
	
	private ManaShield shield;
	
	public WandmakerSprite() {
		super();
		
		texture( Assets.MAKER );
		
		TextureFilm frames = TextureCache.getFilm( texture, 12, 14 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 3, 3, 3, 3, 3, 2, 1 );
		
		run = new Animation( 20, true );
		run.frames( frames, 0 );
		
		die = new Animation( 20, false );
		die.frames( frames, 0 );
		
		play( idle );
	}
	
	@Override
	public void link(Char ch ) {
		super.link( ch );
		
		if (shield == null) {
			GameScene.addToMobLayer( shield = new ManaShield(this) );
		}
	}
	
	@Override
	public void die() {
		super.die();
		
		if (shield != null) {
			shield.putOut();
		}
		emitter().start( ElmoParticle.FACTORY, 0.03f, 60 );
	}

}
