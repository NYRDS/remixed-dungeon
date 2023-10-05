
package com.watabou.pixeldungeon.sprites;

import com.watabou.noosa.particles.Emitter;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.Speck;

public class FetidRatSprite extends RatSprite {
	
	private Emitter cloud;
	
	@Override
	public void link(Char ch ) {
		super.link( ch );
		
		if (cloud == null) {
			cloud = emitter();
			cloud.pour( Speck.factory( Speck.PARALYSIS ), 0.7f );
		}
	}
	
	@Override
	public void update() {
		
		super.update();
		
		if (cloud != null) {
			cloud.setVisible(getVisible());
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
