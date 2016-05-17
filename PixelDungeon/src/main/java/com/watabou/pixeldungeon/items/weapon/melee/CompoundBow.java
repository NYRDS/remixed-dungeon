package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.items.Item;

public class CompoundBow extends Bow {

	public CompoundBow() {
		super( 3, 0.8f, 1.5f );
		imageFile = "items/ranged.png";
		image = 2;
	}
	
	@Override
	public Item burn(int cell) {
		return null;
	}
	
	@Override
	public double acuFactor() {
		return 1 + level() * 0.2;
	}
	
	@Override
	public double dmgFactor() {
		return 1 + level() * 0.5;
	}
	
	public double dlyFactor() {
		return 1.1;
	}
}
