package com.nyrds.pixeldungeon.items.chaos;

import com.watabou.pixeldungeon.actors.Char;

public interface IChaosItem {
	public void ownerTakesDamage(int damage);
	public void ownerDoesDamage(Char ch,int damage);
	public int getCharge();
}
