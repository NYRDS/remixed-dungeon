package com.nyrds.retrodungeon.mobs.common;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.necropolis.JarOfSouls;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class MobSpawner {

	@NonNull
	static public Mob spawnRandomMob(Level level, int position) {
		Mob mob = Bestiary.mob(level);
		mob.setPos(position);
		mob.setState(mob.WANDERING);
		level.spawnMob(mob);
		return mob;
	}

	static public void spawnJarOfSouls(Level level, int position) {
		Mob mob = new JarOfSouls();
		mob.setPos(position);
		mob.setState(mob.WANDERING);
		level.spawnMob(mob);
	}

}
