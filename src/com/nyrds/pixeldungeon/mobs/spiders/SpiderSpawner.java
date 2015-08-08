package com.nyrds.pixeldungeon.mobs.spiders;

import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.mobs.Bestiary;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;

public class SpiderSpawner {

	static public void spawnRandomSpider(Level level,int position) {
			Mob mob = Bestiary.mob(Dungeon.depth, level.levelKind());
			mob.pos = position;
			mob.state = mob.WANDERING;
			
			level.spawnMob( mob );			
	}
	
	static public void spawnEgg(Level level,int position) {

			Mob mob = new SpiderEgg();
			mob.pos = position;
			mob.state = mob.SLEEPEING;
			level.spawnMob( mob );
	}
	
	static public void spawnNest(Level level,int position) {

		Mob mob = new SpiderNest();
		mob.pos = position;
		mob.state = mob.SLEEPEING;
		level.spawnMob( mob );
}
	
}
