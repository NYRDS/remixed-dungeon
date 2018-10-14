package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;

public class ElvenDagger extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 8;
		animation_class = SWORD_ATTACK;
	}

	public ElvenDagger() {
		super( 1, 1.5f, 0.7f );
	}

	@Override
	public int typicalSTR() {
		return 9;
	}

	@Override
	public String getVisualName() {
		return "Dagger";
	}

}
