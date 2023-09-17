
package com.watabou.pixeldungeon.actors.buffs;

public class Sleep extends FlavourBuff {
	
	public static final float SWS	= 1.5f;

	@Override
	public void attachVisual() {
		target.getSprite().idle();
	}
}
