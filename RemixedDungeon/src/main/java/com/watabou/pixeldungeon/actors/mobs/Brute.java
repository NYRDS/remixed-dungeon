
package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.hero.HeroSubClass;
import com.watabou.pixeldungeon.items.Gold;

public class Brute extends Mob {

	public Brute() {

		hp(ht(40));
		baseAttackSkill = 15;
		baseDefenseSkill = 20;
		dmgMin = 8;
		dmgMax = 18;

		expForKill = 8;
		maxLvl = 15;

		dr = 8;

		loot(Gold.class, 0.5f);
		
		addImmunity( Terror.class );
	}

	@Override
	public HeroSubClass getSubClass() {
		return HeroSubClass.BERSERKER;
	}
}
