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

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.ThiefFleeing;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.CharUtils;
import com.watabou.pixeldungeon.actors.buffs.Blindness;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;


public class Bandit extends Thief {

	@Override
	public int attackProc(@NotNull Char enemy, int damage) {
		if (CharUtils.steal(this, enemy)) {
			setState(MobAi.getStateByClass(ThiefFleeing.class));
			Buff.prolong(getEnemy(), Blindness.class, Random.Int(5, 12));

			if(enemy==Dungeon.hero) {
				Dungeon.observe();
			}
		}
		return damage;
	}
}
