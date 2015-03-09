package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.ShadowSprite;
import com.watabou.utils.Random;

public class Shadow extends Mob {
	private int level;

	{
		spriteClass = ShadowSprite.class;

		HP = HT = 1;
		EXP = 0;
		state = WANDERING;
	}

	@Override
	public boolean isWallWalker() {
		return true;
	}

	@Override
	public int damageRoll() {
		return Random.NormalIntRange(1, 3 + level);
	}

	@Override
	public int attackSkill(Char target) {
		return 10 + level;
	}
}
