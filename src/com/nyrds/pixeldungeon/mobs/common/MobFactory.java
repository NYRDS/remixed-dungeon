package com.nyrds.pixeldungeon.mobs.common;

import java.util.HashMap;

import com.watabou.pixeldungeon.actors.mobs.Crab;
import com.watabou.pixeldungeon.actors.mobs.Gnoll;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.actors.mobs.Rat;
import com.watabou.pixeldungeon.actors.mobs.Skeleton;
import com.watabou.pixeldungeon.actors.mobs.Swarm;
import com.watabou.pixeldungeon.actors.mobs.Thief;

public class MobFactory {

	static private HashMap <String, Class<? extends Mob>> mMobsList = new HashMap<>();
	
	{
		mMobsList.put("Rat", Rat.class);
		mMobsList.put("Gnoll", Gnoll.class);
		mMobsList.put("Crab", Crab.class);
		mMobsList.put("Swarm", Swarm.class);
		mMobsList.put("Thief", Thief.class);
		mMobsList.put("Skeleton", Skeleton.class);
	};
	
	public static Class<? extends Mob> mobClassByName(String selectedMobClass) {
		
		Class<? extends Mob> mobClass = mMobsList.get(selectedMobClass);
		if(mobClass != null) {
			return mobClass;
		}
		
		return null;
	}

}
