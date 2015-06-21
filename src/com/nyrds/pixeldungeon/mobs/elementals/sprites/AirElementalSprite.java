package com.nyrds.pixeldungeon.mobs.elementals.sprites;

import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.MobSprite;

public class AirElementalSprite extends MobSprite {
	
	public AirElementalSprite() {
		texture( Assets.AIR_ELEMENTAL );
		
		TextureFilm frames = new TextureFilm( texture, 12, 14 );
		
		idle = new Animation( 10, true );
		idle.frames( frames, 0, 1, 2 );
		
		run = new Animation( 12, true );
		run.frames( frames, 0, 1, 3 );
		
		attack = new Animation( 15, false );
		attack.frames( frames, 4, 5, 6 );
		
		die = new Animation( 15, false );
		die.frames( frames, 7, 8, 9, 10, 11, 12, 13, 12 );
		
		play( idle );
	}
	
	@Override
	public void link( Char ch ) {
		super.link( ch );
		add( State.LEVITATING );
	}
	
	@Override
	public void die() {
		super.die();
		remove( State.LEVITATING );
	}
	
	@Override
	public int blood() {
		return 0xFF439DFF;
	}
}
