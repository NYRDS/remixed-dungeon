package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class SpiderSpawner {

	static public void spawnQueen(Level level, int position) {
		Mob mob = new SpiderQueen();
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Wandering.class));
		level.spawnMob(mob);
	}

	static public void spawnEgg(Level level, int position) {
		Mob mob = new SpiderEgg();
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Sleeping.class));
		level.spawnMob(mob);
	}

	static public void spawnNest(Level level, int position) {
		Mob mob = new SpiderNest();
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Sleeping.class));
		level.spawnMob(mob);
	}

}
