package com.nyrds.pixeldungeon.mobs.spiders;

import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.SpiderMind;
import com.watabou.pixeldungeon.actors.mobs.SpiderMum;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class SpiderSpawner {
	
	static Object SpiderClasses[] = {SpiderMum.class, SpiderMind.class};
	
	static public void spawnRandomSpider(Level level,int position) {
		try {
			Mob mob = (Mob) ((Class) Random.oneOf(SpiderClasses)).newInstance();
			mob.pos = position;
			mob.state = mob.WANDERING;
			
			level.spawnMob( mob );
			
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
