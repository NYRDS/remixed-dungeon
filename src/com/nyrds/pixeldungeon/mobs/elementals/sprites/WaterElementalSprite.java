package com.nyrds.pixeldungeon.mobs.elementals.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.sprites.MobSprite;

public class WaterElementalSprite extends MobSprite {
	
	public WaterElementalSprite() {
		texture( Assets.WATER_ELEMENTAL );
		
		TextureFilm frames = new TextureFilm( texture, 16, 16 );
		
		idle = new Animation( 5, true );
		idle.frames( frames, 0, 1);
		
		run = new Animation( 10, true );
		run.frames( frames, 0, 1 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 2, 3, 4 );
		
		die = new Animation( 15, false );
		die.frames( frames, 5, 6, 7, 8 );
		
		play( idle );
	}
	
	@Override
	public int blood() {
		return 0xFF137DFF;
	}
}
