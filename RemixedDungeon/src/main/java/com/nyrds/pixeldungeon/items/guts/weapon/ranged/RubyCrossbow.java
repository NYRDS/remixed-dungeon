package com.nyrds.pixeldungeon.items.guts.weapon.ranged;

public class RubyCrossbow extends Crossbow {

	public RubyCrossbow() {
		super( 6, 1.1f, 1.6f );
		image = 5;
	}

	@Override
	public double acuFactor() {
        return super.acuFactor();
    }

	@Override
	public double dmgFactor() {
		return 1 + level() * 1.1;
	}

	public double dlyFactor() {
		return 1 + level() * 0.2;
	}
}
