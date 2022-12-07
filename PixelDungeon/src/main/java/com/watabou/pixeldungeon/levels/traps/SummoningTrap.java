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
package com.watabou.pixeldungeon.levels.traps;

import android.support.annotation.Nullable;

import com.nyrds.retrodungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DelayedMobSpawner;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class SummoningTrap implements ITrigger {

	public static void trigger(int pos, @Nullable Char c) {

		if (Dungeon.bossLevel()) {
			return;
		}

		if (c != null) {
			Actor.occupyCell(c);
		}

		int nMobs = 3;
		if (Random.Int(2) == 0) {
			nMobs++;
			if (Random.Int(2) == 0) {
				nMobs++;
			}
		}

		Level level = Dungeon.level;

		for (int i = 0; i < nMobs; ++i) {
			int cell = level.getEmptyCellNextTo(pos);
			if (level.cellValid(cell)) {
				Mob mob = placeMob(level, cell);

				if(mob!= null) {
					mob.setState(mob.WANDERING);
					Actor.addDelayed(new DelayedMobSpawner(mob, cell), 0.1f);
				}
			}
		}
	}

	@Nullable
	private static Mob placeMob(Level level, int cell) {
		Mob mob;
		int nTry = 0;
		do {
			mob = Bestiary.mob(level);
			nTry++;
			if(nTry > 10) {
				return null;
			}
		} while (!mob.canSpawnAt(level, cell));
		return mob;
	}

	@Override
	public void doTrigger(int cell, Char ch) {
		trigger(cell,ch);
	}
}
