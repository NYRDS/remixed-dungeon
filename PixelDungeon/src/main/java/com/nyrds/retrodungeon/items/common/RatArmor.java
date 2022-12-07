package com.nyrds.retrodungeon.items.common;

import com.watabou.pixeldungeon.items.armor.Armor;

public class RatArmor extends Armor {

	public RatArmor() {
		super( 2 );
		image = 24;
	}

	@Override
	public int typicalSTR() {
		return super.typicalSTR()-1;
	}

	@Override
	public int typicalDR() {
		return super.typicalDR()+1;
	}
}
