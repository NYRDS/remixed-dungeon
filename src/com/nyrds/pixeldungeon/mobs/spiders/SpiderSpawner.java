package com.nyrds.pixeldungeon.mobs.spiders;

import com.watabou.pixeldungeon.actors.Actor;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.levels.Level;
import com.watabou.utils.Random;

public class SpiderSpawner {
	
	static Class<?> SpiderClasses[] = {	SpiderServant.class, 
										SpiderMind.class, 
										SpiderExploding.class};
	
	static public void spawnRandomSpider(Level level,int position) {
		try {
			Mob mob = (Mob) ((Class<?>) Random.oneOf(SpiderClasses)).newInstance();
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
	
	static public void spawnEgg(Level level,int position) {
		
			if(Actor.findChar(position) instanceof SpiderEgg) {
				return;
			}
		
			Mob mob = new SpiderEgg();
			mob.pos = position;
			mob.state = mob.SLEEPEING;
			level.spawnMob( mob );
	}
	
}
