
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
		carcassChance = 0.2f;

		loot(new PotionOfHealing(), BASIC_LOOT_CHANCE);
	}

	@Packable
	public int generation = 0;

	@Override
	public int defenseProc( Char enemy, int damage ) {

		// caveman: split must not kill the swarm - bare hp() write past the death
		// guard + damage() early-return left a dead swarm in the actor list ->
		// silent turn-loop freeze
		if (damage > 0 && hp() >= damage + 2) {
			int cell = level().getEmptyCellNextTo(getPos());

			if (level().cellValid(cell)) {
				int cloneHp = Math.min(split(cell, damage).hp(), hp() - 1);
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
		clone.carcassChance = 0.2f / (clone.generation + 1f);
		return clone;
	}
}
