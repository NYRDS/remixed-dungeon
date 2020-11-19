package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.actors.hero.Belongings;

public class Polearm extends MeleeWeapon {

	{
		//imageFile = "items/polearms.png";
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
	public Belongings.Slot slot(Belongings belongings) {
		return Belongings.Slot.WEAPON;
	}

	@Override
	public Belongings.Slot blockSlot() {
		return Belongings.Slot.LEFT_HAND;
	}

	@Override
	public int range() {
		return 2;
	}
}
