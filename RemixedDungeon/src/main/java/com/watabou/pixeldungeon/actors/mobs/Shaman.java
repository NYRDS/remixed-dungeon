
package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.Fleeing;
import com.nyrds.pixeldungeon.ai.Hunting;
import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.items.Treasury;
import com.nyrds.pixeldungeon.ml.R;
import com.nyrds.pixeldungeon.mobs.common.IZapper;
import com.nyrds.platform.util.StringsManager;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.mechanics.Ballistica;

import org.jetbrains.annotations.NotNull;

public class Shaman extends Mob implements IZapper {

	private int fleeState = 0;

	public Shaman() {
		hp(ht(18));
		baseDefenseSkill = 8;
		baseAttackSkill  = 11;
		dmgMin = 2;
		dmgMax = 6;
		dr = 4;

		expForKill = 6;
		maxLvl = 14;

		loot(Treasury.Category.SCROLL, 0.33f);

		addResistance(LightningTrap.Electricity.class);
	}

	@Override
	public boolean canAttack(@NotNull Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public int defenseProc(Char enemy, int damage) {

		if (hp() > 2 * ht() / 3 && fleeState < 1) {
			setState(MobAi.getStateByClass(Fleeing.class));
			fleeState++;
			return damage / 2;
		}

		if (hp() > ht() / 3 && fleeState < 2) {
			setState(MobAi.getStateByClass(Fleeing.class));
			fleeState++;
			return damage / 2;
		}

		return damage;
	}

	@Override
    public boolean getFurther(int target) {

		if (level().distance(getPos(), target) > 2) {
			setState(MobAi.getStateByClass(Hunting.class));
		}

		return super.getFurther(target);
	}

	@Override
	protected int zapProc(@NotNull Char enemy, int damage) {
		int dmg = damageRoll() * 2;

		CharUtils.lightningProc(this ,enemy.getPos(), dmg);

        CharUtils.checkDeathReport(this, enemy, StringsManager.getVar(R.string.Shaman_Killed));
		return 0;
	}

	@Override
	protected void zapMiss(@NotNull Char enemy) {
		if (Math.random() < 0.1) {
			yell(StringsManager.getVar(R.string.Shaman_ZapMiss));
		}
	}
}
