package com.nyrds.pixeldungeon.items.guts.weapon.melee;

import com.watabou.pixeldungeon.items.weapon.melee.Polearm;

public class Halberd extends Polearm {

	public Halberd() {
		super( 6, 1.1f, 1.4f );
		imageFile = "items/polearms.png";
		image = 2;
		animation_class = STAFF_ATTACK;
	}
}
