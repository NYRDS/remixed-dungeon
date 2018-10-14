package com.watabou.pixeldungeon.items.weapon.melee;

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
}
