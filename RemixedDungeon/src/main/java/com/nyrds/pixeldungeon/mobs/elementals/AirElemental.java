package com.nyrds.pixeldungeon.mobs.elementals;


import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.mechanics.spells.WindGust;
import com.nyrds.pixeldungeon.mobs.common.IDepthAdjustable;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Bleeding;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfLevitation;
import com.watabou.pixeldungeon.mechanics.Ballistica;

import org.jetbrains.annotations.NotNull;

public class AirElemental extends Mob implements IDepthAdjustable {


	private static final WindGust windGust = new WindGust();

	public AirElemental() {

		adjustStats(Dungeon.depth);

		flying = true;
		
		loot(new PotionOfLevitation(),0.1f);

		addImmunity(Bleeding.class);
		setSkillLevel(3 + lvl() / 10);
	}

	public void adjustStats(int depth) {
		hp(ht(depth * 3 + 1));
		baseDefenseSkill = depth * 2 + 1;
		baseAttackSkill = baseDefenseSkill * 2 + 1;
		expForKill = depth + 1;
		maxLvl = depth + 2;
		dr = expForKill / 5;
		dmgMin = 0;
		dmgMax = ht() / 4;
	}

	@Override
	public boolean getCloser(int target) {
		if (getState() instanceof Hunting
				&& level().distance(getPos(), target) < skillLevel() - 1) {
			return getFurther(target);
		}

		return super.getCloser(target);
	}

	@Override
    public boolean canAttack(@NotNull Char enemy) {

		if (adjacent(enemy)) {
			return false;
		}

		if(level().distance(getPos(), enemy.getPos()) >= skillLevel()) {
			return false;
		}

		Ballistica.cast(getPos(), enemy.getPos(), true, false);

		for (int i = 1; i < skillLevel(); i++) {
			if (Ballistica.trace[i] == enemy.getPos()) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected int zapProc(@NotNull Char enemy, int damage) {
		windGust.cast(this, enemy.getPos());
		return damage;
	}
}
