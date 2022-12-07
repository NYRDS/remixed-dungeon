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

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.ml.R;
import com.nyrds.retrodungeon.mobs.common.IZapper;
import com.watabou.noosa.Camera;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.ResultDescriptions;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.effects.particles.SparkParticle;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.levels.traps.LightningTrap;
import com.watabou.pixeldungeon.mechanics.Ballistica;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.pixeldungeon.utils.Utils;
import com.watabou.utils.Random;

public class Shaman extends Mob implements IZapper {

	private static final String TXT_LIGHTNING_KILLED = Game.getVar(R.string.Shaman_Killed);

	private int fleeState = 0;

	public Shaman() {
		hp(ht(18));
		defenseSkill = 8;

		exp = 6;
		maxLvl = 14;

		loot = Generator.Category.SCROLL;
		lootChance = 0.33f;

		RESISTANCES.add(LightningTrap.Electricity.class);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(2, 6);
	}

	@Override
	public int attackSkill(Char target) {
		return 11;
	}

	@Override
	public int dr() {
		return 4;
	}

	@Override
	protected boolean canAttack(Char enemy) {
		return Ballistica.cast(getPos(), enemy.getPos(), false, true) == enemy.getPos();
	}

	@Override
	public int defenseProc(Char enemy, int damage) {

		if (hp() > 2 * ht() / 3 && fleeState < 1) {
			setState(FLEEING);
			fleeState++;
			return damage / 2;
		}

		if (hp() > ht() / 3 && fleeState < 2) {
			setState(FLEEING);
			fleeState++;
			return damage / 2;
		}

		return damage;
	}

	@Override
	protected boolean getFurther(int target) {

		if (Dungeon.level.distance(getPos(), target) > 2) {
			setState(HUNTING);
		}

		return super.getFurther(target);
	}

	@Override
	public boolean zap(@NonNull Char enemy) {
		if (zapHit(enemy)) {
			int dmg = damageRoll() * 2;
			if (Dungeon.level.water[enemy.getPos()] && !enemy.isFlying()) {
				dmg *= 1.5f;
			}
			enemy.damage(dmg, LightningTrap.LIGHTNING);

			enemy.getSprite().centerEmitter().burst(SparkParticle.FACTORY, 3);
			enemy.getSprite().flash();

			if (enemy == Dungeon.hero) {
				Camera.main.shake(2, 0.3f);

				if (!enemy.isAlive()) {
					Dungeon.fail(Utils.format(ResultDescriptions.MOB,
							Utils.indefinite(getName()), Dungeon.depth));
					GLog.n(TXT_LIGHTNING_KILLED, getName());
				}
			}
			return true;
		}
		return false;
	}
}
