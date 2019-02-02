package com.nyrds.pixeldungeon.items.guts.weapon.melee;

import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.weapon.melee.SpecialWeapon;

public class TitanSword extends SpecialWeapon {
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
