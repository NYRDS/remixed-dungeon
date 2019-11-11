package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.actors.hero.Belongings;

public class Polearm extends SpecialWeapon {

	{
		//imageFile = "items/polearms.png";
		range = 2;
		animation_class = SPEAR_ATTACK;
	}

	public Polearm(int tier, float acu, float dly) {
		super(tier, acu, dly);
	}

	@Override
	public boolean isFliesStraight() {
		return true;
	}

	@Override
	public Belongings.Slot blockSlot() {
		return Belongings.Slot.LEFT_HAND;
	}
}
