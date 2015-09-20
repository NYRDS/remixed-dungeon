package com.watabou.pixeldungeon.items.weapon.melee;

public class SpecialWeapon extends MeleeWeapon{

	protected int range;
	
	public SpecialWeapon(int tier, float acu, float dly) {
		super(tier, acu, dly);
	}

	public int getRange() {
		return range;
	}

}
