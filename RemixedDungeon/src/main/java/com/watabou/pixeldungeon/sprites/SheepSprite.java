
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.Animation;
import com.watabou.noosa.TextureFilm;
import com.watabou.pixeldungeon.Assets;
import com.watabou.utils.Random;

public class SheepSprite extends MobSprite {
	
	public SheepSprite() {
		super();
		
		texture( Assets.SHEEP );
		
		TextureFilm frames = new TextureFilm( texture, 16, 15 );
		
		idle = new Animation( 8, true );
		idle.frames( frames, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 2, 3, 0 );
		
		run = idle.clone();	
		attack = idle.clone();
		
		die = new Animation( 20, false );
		die.frames( frames, 0 );
		
		play( idle );
		curFrame = Random.Int( curAnim.frames.length );
	}
}
