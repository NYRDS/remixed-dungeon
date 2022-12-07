package com.nyrds.retrodungeon.items.common;

import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;

public class ElvenDagger extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 8;
	}

	public ElvenDagger() {
		super( 1, 1.5f, 0.7f );
	}

	@Override
	public int typicalSTR() {
		return 9;
	}

}
