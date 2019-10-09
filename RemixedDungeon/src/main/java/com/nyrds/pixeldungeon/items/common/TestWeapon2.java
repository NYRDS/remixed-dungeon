package com.nyrds.pixeldungeon.items.common;

import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;

public class TestWeapon2 extends SpecialWeapon {
	{
		imageFile = "items/swords.png";
		image = 5;
		enchatable = false;
		animation_class = SWORD_ATTACK;
	}

	public TestWeapon2() {
		super( 3, 1.1f, 0.8f );
	}
}
