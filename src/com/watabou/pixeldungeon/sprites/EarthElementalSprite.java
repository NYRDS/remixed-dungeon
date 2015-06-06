package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;

public class EarthElementalSprite extends MobSprite {
	
	public EarthElementalSprite() {
		super();
		
		texture( Assets.EARTH_ELEMENTAL );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 4, true );
		idle.frames( frames, 0, 1);
		
		run = new Animation( 8, true );
		run.frames( frames, 2, 3, 4, 5 );
		
		attack = new Animation( 12, false );
		attack.frames( frames, 2, 6, 7, 8 );
		
		die = new Animation( 11, false );
		die.frames( frames, 2, 9, 10, 11, 12, 13 );
		
		play( idle );
	}
	
	@Override
	public int blood() {
		return 0xFF137DFF;
	}
}
