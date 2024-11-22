package com.watabou.pixeldungeon.sprites;

import com.nyrds.platform.gl.Gl;
import com.watabou.gltextures.TextureCache;
import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.effects.Speck;
import com.watabou.pixeldungeon.effects.particles.ShaftParticle;

public class GhostSprite extends MobSprite {

	public GhostSprite() {
		super();

		texture( Assets.GHOST );

		TextureFilm frames = TextureCache.getFilm( texture, 14, 15 );

		idle = new Animation( 5, true );
		idle.frames( frames, 0, 1 );

		run = new Animation( 10, true );
		run.frames( frames, 0, 1 );

		die = new Animation( 20, false );
		die.frames( frames, 0 );

		play( idle );
	}

	@Override
	public void draw() {
		Gl.blendSrcAlphaOne();
		super.draw();
		Gl.blendSrcAlphaOneMinusAlpha();
	}

	@Override
	public void die() {
		super.die();
		emitter().start( ShaftParticle.FACTORY, 0.3f, 4 );
		emitter().start( Speck.factory( Speck.LIGHT ), 0.2f, 3 );
	}

	@Override
	public int blood() {
		return 0xFFFFFF;
	}
}
