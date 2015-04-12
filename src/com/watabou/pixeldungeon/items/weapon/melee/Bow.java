package com.watabou.pixeldungeon.items.weapon.melee;

public abstract class Bow extends MeleeWeapon {
	
	public Bow( int tier, float acu, float dly ) {
		super(tier, acu, dly);
	}
	
	@Override
	protected int max() {
		return min();
	}
	
	public double acuFactor() {
		return 1;
	}

	public double dlyFactor() {
		return 1;
	}
	
	public double dmgFactor() {
		return 1;
	}
}
