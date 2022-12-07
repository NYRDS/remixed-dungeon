package com.nyrds.retrodungeon.items.common;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.Bow;

public class ElvenBow extends Bow {

	public ElvenBow() {
		super( 1, 1.2f, 1.3f );
		imageFile = "items/ranged.png";
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
}
