
package com.watabou.pixeldungeon.sprites;

import com.nyrds.pixeldungeon.game.GameLoop;

public class DiscardedItemSprite extends ItemSprite {
	
	public DiscardedItemSprite() {
		
		super();
		
		originToCenter();
		angularSpeed = 720;
	}
	
	@Override
	public void drop() {
		setScale( 1 );
		am = 1;
	}
	
	@Override
	public void update() {
		
		super.update();

		setScale( scale.x * 0.9f );
		if ((am -= GameLoop.elapsed) <= 0) {
			remove();
		}
	}
}
