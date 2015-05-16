package com.watabou.pixeldungeon.actors.mobs;

import com.nyrds.pixeldungeon.mobs.spiders.SpiderSpawner;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.sprites.SpiderNestSprite;

public class SpiderNest extends Mob {

	public SpiderNest() {
		
		spriteClass = SpiderNestSprite.class;
		
		hp(ht(10));
		defenseSkill = 1;
		baseSpeed = 0f;
		
		EXP    = 0;
		maxLvl = 9;
		
		postpone(20);
	}
	
	@Override
	public int damageRoll() {
		return 0;
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 0;
	}
	
	@Override
	protected boolean act(){
		super.act();
		
		SpiderSpawner.spawnRandomSpider(Dungeon.level, pos);
		
		state = SLEEPEING;
		
		postpone(20);
		
		return true;
	}
	
	@Override
	public int dr() {
		return 0;
	}
}
