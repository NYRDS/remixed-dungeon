package com.watabou.pixeldungeon.actors.mobs;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.sprites.SpiderMumParalysisSprite;
import com.watabou.pixeldungeon.sprites.SpiderMumPoisonSprite;
import com.watabou.pixeldungeon.sprites.SpiderMumSprite;
import com.watabou.utils.Random;

public class SpiderMum extends Mob {

	@SuppressWarnings("unchecked")
	SpiderMum() {
		
		spriteClass = (Class<? extends CharSprite>) Random.oneOf( SpiderMumParalysisSprite.class, 
									SpiderMumPoisonSprite.class);
		
		hp(ht(15));
		defenseSkill = 1;
		baseSpeed = 2f;
		
		EXP = 3;
		maxLvl = 9;
		
		loot = new MysteryMeat();
		lootChance = 0.167f;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 3, 6 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 1;
	}
	
	@Override
	public int dr() {
		return 0;
	}
	
	@Override
	public void die( Object cause ) {
		super.die( cause );
	}

}
