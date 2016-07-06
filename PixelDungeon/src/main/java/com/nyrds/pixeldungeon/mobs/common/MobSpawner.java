package com.nyrds.pixeldungeon.mobs.common;

import com.nyrds.pixeldungeon.mobs.spiders.SpiderEgg;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderNest;
import com.nyrds.pixeldungeon.mobs.spiders.SpiderQueen;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class MobSpawner {

	static public Mob spawnRandomMob(Level level, int position) {
		Mob mob = Bestiary.mob(Dungeon.depth, level.levelKind());
		mob.setPos(position);
		mob.state = mob.WANDERING;
		level.spawnMob(mob);
		return mob;
	}

}
