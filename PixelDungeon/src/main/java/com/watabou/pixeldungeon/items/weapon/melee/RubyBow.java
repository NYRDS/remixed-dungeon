package com.watabou.pixeldungeon.items.weapon.melee;

public class RubyBow extends Bow {

	public RubyBow() {
		super( 5, 0.8f, 1.5f );
		imageFile = "items/ranged.png";
		image = 4;
	}
	
	@Override
	public double acuFactor() {
		return 1;
	}
	
	@Override
	public double dmgFactor() {
		return 1 + level();
	}
	
	public double dlyFactor() {
		return 1 + level() * 0.2;
	}
}
