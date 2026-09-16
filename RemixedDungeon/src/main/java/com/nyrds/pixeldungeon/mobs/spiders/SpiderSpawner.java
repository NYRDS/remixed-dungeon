package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Sleeping;
import com.nyrds.pixeldungeon.ai.Wandering;
import com.nyrds.pixeldungeon.mobs.common.MobFactory;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class SpiderSpawner {

	static public void spawnQueen(Level level, int position) {
		Mob mob = MobFactory.mobByName(MobFactory.SPIDER_QUEEN);
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Wandering.class));
		level.spawnMob(mob);
	}

	static public void spawnEgg(Level level, int position) {
		Mob mob = MobFactory.mobByName(MobFactory.SPIDER_EGG);
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Sleeping.class));
		level.spawnMob(mob);
	}

	static public void spawnNest(Level level, int position) {
		Mob mob = MobFactory.mobByName(MobFactory.SPIDER_NEST);
		mob.setPos(position);
		mob.setState(MobAi.getStateByClass(Sleeping.class));
		level.spawnMob(mob);
	}

}
