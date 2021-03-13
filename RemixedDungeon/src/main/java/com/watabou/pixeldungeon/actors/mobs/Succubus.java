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

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Charm;
import com.watabou.pixeldungeon.actors.buffs.Sleep;
import com.watabou.pixeldungeon.items.quest.DriedRose;
import com.watabou.pixeldungeon.items.scrolls.ScrollOfLullaby;
import com.watabou.pixeldungeon.items.weapon.enchantments.Leech;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class Succubus extends Mob {

	private static final int BLINK_DELAY = 5;

	private int delay = 0;

	public Succubus() {

		hp(ht(80));
		baseDefenseSkill = 25;
		baseAttackSkill  = 40;

		exp = 12;
		maxLvl = 25;

		loot(new ScrollOfLullaby(), 0.05f);

		addResistance(Leech.class);
		addImmunity(Sleep.class);
	}

	@Override
	public void onSpawn(Level level) {
		super.onSpawn(level);
		setViewDistance(level.getViewDistance() + 1);
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(15, 25);
	}

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {

		if (Random.Int(3) == 0) {
			Char target = enemy;

			if (enemy.hasBuff(DriedRose.OneWayLoveBuff.class)) {
				target = this;
			}

			float duration = Charm.durationFactor(target) * Random.IntRange(2, 5);

			Buff.affect(target, Charm.class, duration);
		}

		return damage;
	}

	@Override
    public boolean getCloser(int target) {
		if (level().fieldOfView[target] && level().distance(getPos(), target) > 2 && delay <= 0) {
			CharUtils.blinkTo(this, target);
			delay = BLINK_DELAY;
			spend(-1 / speed());
			return true;
		} else {
			delay--;
			return super.getCloser(target);

		}
	}

	@Override
	public int dr() {
		return 10;
	}
}
