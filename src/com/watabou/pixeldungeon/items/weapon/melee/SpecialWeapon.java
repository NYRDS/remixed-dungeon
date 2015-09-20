package com.watabou.pixeldungeon.items.weapon.melee;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Hero;

public class SpecialWeapon extends MeleeWeapon{

	protected int range;
	
	public SpecialWeapon(int tier, float acu, float dly) {
		super(tier, acu, dly);
		range = 1;
	}

	public int getRange() {
		return range;
	}
	
	public void applySpecial(Hero hero, Char tgt ) {
	}

}
