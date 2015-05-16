package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.pixeldungeon.sprites.SpiderQueenSprite;
import com.watabou.utils.Random;

public class SpiderQueen extends Mob {
	
	{
		spriteClass = SpiderQueenSprite.class;
		
		hp(ht(1));
		defenseSkill = 18;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = Generator.Category.POTION;
		lootChance = 0.83f;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 12, 20 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 2;
	}
	
	@Override
	public int dr() {
		return 0;
	}
}
