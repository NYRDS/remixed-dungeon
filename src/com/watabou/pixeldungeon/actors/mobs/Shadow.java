package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.ShadowSprite;
import com.watabou.utils.Random;

public class Shadow extends Mob {
	{
		spriteClass = ShadowSprite.class;
		
		hp(ht(20));
		defenseSkill = 15;
		
		EXP = 5;
		maxLvl = 10;
		
		state = WANDERING;
	}

	@Override
	public float speed() {
		return 2;
	}
	
	@Override
	protected float attackDelay() {
		return 0.5f;
	}
	
	@Override
	public boolean isWallWalker() {
		return true;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 5, 10 );
	}
	
	@Override
	public int attackSkill(Char target) {
		return 10;
	}
}
