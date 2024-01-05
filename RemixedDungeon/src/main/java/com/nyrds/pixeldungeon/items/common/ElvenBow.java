package com.nyrds.pixeldungeon.items.common;

import com.nyrds.pixeldungeon.items.guts.weapon.ranged.Bow;
import com.watabou.pixeldungeon.items.Item;

public class ElvenBow extends Bow {

	public ElvenBow() {
		super( 1, 1.2f, 1f );
		image = 6;
	}
	
	@Override
	public Item burn(int cell) {
		return null;
	}
	
	@Override
	public double acuFactor() {
		return 1 + level() * 0.15;
	}
	
	@Override
	public double dmgFactor() {
		return 1 + level() * 0.3;
	}

	@Override
	public String getVisualName() {
		return "WoodenBow";
	}
}
