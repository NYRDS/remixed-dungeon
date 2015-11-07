package com.nyrds.pixeldungeon.items.chaos;

public interface IChaosItem {
	public void ownerTakesDamage(int damage);
	public void ownerDoesDamage(int damage);
	public int getCharge();
}
