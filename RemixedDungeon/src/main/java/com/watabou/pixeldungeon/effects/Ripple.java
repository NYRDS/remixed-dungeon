
package com.watabou.pixeldungeon.effects;

import com.nyrds.pixeldungeon.game.GameLoop;
import com.watabou.noosa.Image;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.DungeonTilemap;

public class Ripple extends Image {

	private static final float TIME_TO_FADE = 0.5f;
	
	private float time;
	
	public Ripple() {
		super( Effects.get( Effects.Type.RIPPLE ) );
	}
	
	public void reset( int p ) {
		revive();
		
		setX((p % Dungeon.level.getWidth()) * DungeonTilemap.SIZE);
		setY((p / Dungeon.level.getWidth()) * DungeonTilemap.SIZE);

		setOrigin( width / 2, height / 2 );
		setScale( 0 );
		
		time = TIME_TO_FADE;
	}
	
	@Override
	public void update() {
		super.update();
		
		if ((time -= GameLoop.elapsed) <= 0) {
			kill();
		} else {
			float p = time / TIME_TO_FADE;
			setScale( 1 - p );
			alpha( p );
		}
	}
}
