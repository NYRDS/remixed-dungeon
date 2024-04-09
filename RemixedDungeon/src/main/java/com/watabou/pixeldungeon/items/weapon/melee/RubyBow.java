package com.watabou.pixeldungeon.items.weapon.melee;

import com.nyrds.pixeldungeon.items.guts.weapon.ranged.Bow;

public class RubyBow extends Bow {

	public RubyBow() {
		super( 5, 0.8f, 1.5f );
		image = 4;
	}
	
	@Override
	public double acuFactor() {
        return super.acuFactor();
    }
	
	@Override
	public double dmgFactor() {
		return 1 + level();
	}
	
	public double dlyFactor() {
		return 1 + level() * 0.2;
	}
}
