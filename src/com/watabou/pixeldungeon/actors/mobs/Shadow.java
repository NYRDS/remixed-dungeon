package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.ShadowSprite;
import com.watabou.utils.Random;

public class Shadow extends Mob {
	{
		spriteClass = ShadowSprite.class;
		
		HP = HT = 1;
		defenseSkill = 15;
		
		EXP = 5;
		maxLvl = 10;
		
		EXP = 5;
		state = WANDERING;
	}

	@Override
	public float speed() {
		return 2;
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
