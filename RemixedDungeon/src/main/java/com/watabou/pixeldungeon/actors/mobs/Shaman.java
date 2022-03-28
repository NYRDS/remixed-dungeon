/*
 * Pixel Dungeon
 * Copyright (C) 2012-2014  Oleg Dolya
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
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

		exp = 6;
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
