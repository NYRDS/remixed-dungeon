
package com.watabou.pixeldungeon.actors.buffs;

//Special kind of buff, that doesn't perform any kind actions 
public class FlavourBuff extends Buff {
	
	@Override
	public boolean act() {
		detach();
		return true;
	}
}
