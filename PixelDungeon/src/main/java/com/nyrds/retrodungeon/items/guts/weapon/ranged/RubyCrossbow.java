package com.nyrds.retrodungeon.items.guts.weapon.ranged;

import com.watabou.pixeldungeon.items.weapon.melee.Bow;

public class RubyCrossbow extends Bow {

	public RubyCrossbow() {
		super( 6, 1.1f, 1.6f );
		imageFile = "items/ranged.png";
		image = 5;
	}

	@Override
	public double acuFactor() {
		return 1;
	}

	@Override
	public double dmgFactor() {
		return 1 + level() * 1.1;
	}

	public double dlyFactor() {
		return 1 + level() * 0.2;
	}
}
