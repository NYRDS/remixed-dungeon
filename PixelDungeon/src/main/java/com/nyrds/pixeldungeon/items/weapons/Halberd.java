package com.nyrds.pixeldungeon.items.weapons;

import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;

public class Halberd extends MeleeWeapon {
	{
		imageFile = "items/polearms.png";
		image = 2;
	}

	public Halberd() {
		super( 6, 1.1f, 1.2f );
	}
}
