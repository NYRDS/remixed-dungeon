package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.ai.MobAi;
import com.nyrds.pixeldungeon.ai.Wandering;

public class Shadow extends Mob {
	{
		hp(ht(20));
		baseDefenseSkill = 15;
		baseAttackSkill  = 10;
		dmgMin = 5;
		dmgMax = 10;

		expForKill = 5;
		maxLvl = 10;
		carcassChance = 0;

		walkingType = WalkingType.WALL;

		setState(MobAi.getStateByClass(Wandering.class));
	}

	@Override
	public float speed() {
		return 2;
	}
	
	@Override
	protected float _attackDelay() {
		return 0.5f;
	}
}
