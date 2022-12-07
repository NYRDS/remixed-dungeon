package com.nyrds.retrodungeon.items.guts.weapon.melee;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;
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
	public void applySpecial(Hero hero, Char tgt ) {

	}

	@Override
	public Item upgrade() {
		return this;
	}
}
