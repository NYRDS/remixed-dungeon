package com.nyrds.pixeldungeon.mobs.common;

import android.support.annotation.NonNull;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mobs.necropolis.JarOfSouls;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.effects.Pushing;
import com.watabou.pixeldungeon.levels.Level;

public class MobSpawner {

	static public void spawnRandomMob(Level level, Mob parent) {
		int position = parent.getPos();

		int mobPos = Dungeon.level.getEmptyCellNextTo(position);

		if (Dungeon.level.cellValid(mobPos)) {

			Mob mob = Bestiary.mob(level);
			mob.setPos(position);
			mob.setState(MobAi.getStateByClass(Wandering.class));
			level.spawnMob(mob);


			if(parent.isPet()) {
				Mob.makePet(mob, Dungeon.hero);
			}

			mob.setPos(mobPos);
			Actor.addDelayed(new Pushing(mob, position, mob.getPos()), -1);
			Dungeon.level.press(mobPos, mob);
		}
	}

	static public void spawnJarOfSouls(Level level, int position) {
		Mob mob = new JarOfSouls();
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Wandering.class));
		level.spawnMob(mob);
	}

}
