package com.nyrds.pixeldungeon.items.weapons;

import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;

public class Claymore extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 6;
	}

	public Claymore() {
		super( 6, 1f, 1f );
	}

}
