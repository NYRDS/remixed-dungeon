package com.nyrds.pixeldungeon.items.guts.weapon.melee;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.MeleeWeapon;

public class TitanSword extends MeleeWeapon {
	{
		imageFile = "items/swords.png";
		image = 6;
	}

	public TitanSword() {
		super( 20, 1.5f, 4f );
	}

	@Override
	public Item upgrade() {
		return this;
	}
}
