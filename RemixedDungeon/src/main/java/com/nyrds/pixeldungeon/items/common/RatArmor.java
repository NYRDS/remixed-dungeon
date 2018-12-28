package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.items.armor.Armor;

public class RatArmor extends Armor {

	public RatArmor() {
		super( 1 );
		image = 24;
	}

	@Override
	public int typicalDR() {
		return super.typicalDR()+2;
	}
}
