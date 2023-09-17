
package com.watabou.pixeldungeon.levels.traps;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.levels.objects.ITrigger;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.DelayedMobSpawner;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

import org.jetbrains.annotations.Nullable;

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
					mob.setState(MobAi.getStateByClass(Wandering.class));
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
