package com.nyrds.pixeldungeon.items.guts.weapon.ranged;

public class TacticalCrossbow extends Crossbow {

	public TacticalCrossbow() {
		super(3, 1f, 1.f);
		image = 7;
	}


	@Override
	public double acuFactor() {
		return 1 + level() * 0.25;
	}

	@Override
	public double dmgFactor() {
		return 1 + level() * 0.5;
	}

	public double dlyFactor() {
		return 1.1;
	}

	@Override
	public boolean goodForMelee() {
		return true;
	}
}