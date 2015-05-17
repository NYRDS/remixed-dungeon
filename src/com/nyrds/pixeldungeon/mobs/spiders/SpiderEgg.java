package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderEggSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;

public class SpiderEgg extends Mob {

	public SpiderEgg() {
		
		spriteClass = SpiderEggSprite.class;
		
		hp(ht(2));
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
		
		remove();
		
		return true;
	}
	
	@Override
	public int dr() {
		return 0;
	}
}
