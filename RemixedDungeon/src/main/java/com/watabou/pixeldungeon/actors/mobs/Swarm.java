
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.Packable;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.hero.Belongings;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;

public class Swarm extends Mob {

	private static final float BASIC_LOOT_CHANCE = 0.2f;

	{
		hp(ht(80));
		baseDefenseSkill = 5;
		baseAttackSkill  = 12;

		dmgMin = 1;
		dmgMax = 4;

		maxLvl = 10;
		
		flying = true;

		loot(new PotionOfHealing(), BASIC_LOOT_CHANCE);
	}

	@Packable
	private int generation = 0;

	@Override
	public int defenseProc( Char enemy, int damage ) {

		if (hp() >= damage + 2) {
			int cell = level().getEmptyCellNextTo(getPos());

			if (level().cellValid(cell)) {
				int cloneHp = split(cell, damage).hp();

				hp(hp() - cloneHp);
			}
		}
		
		return damage;
	}

	@Override
	public Mob split(int cell, int damage) {
		Swarm clone = (Swarm) super.split(cell, damage);
		clone.generation = generation + 1;
		clone.resetBelongings(new Belongings(clone));
		clone.loot(new PotionOfHealing(), BASIC_LOOT_CHANCE / (clone.generation + 1f));
		return clone;
	}
}
