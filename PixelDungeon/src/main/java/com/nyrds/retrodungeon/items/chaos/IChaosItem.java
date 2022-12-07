package com.nyrds.retrodungeon.items.chaos;

import com.watabou.pixeldungeon.actors.Char;

public interface IChaosItem {
	void ownerTakesDamage(int damage);
	void ownerDoesDamage(Char ch, int damage);
	int getCharge();
}
