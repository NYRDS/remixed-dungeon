package com.nyrds.retrodungeon.mobs.spiders;

import com.nyrds.retrodungeon.mobs.common.MobSpawner;
import com.nyrds.retrodungeon.mobs.spiders.sprites.SpiderNestSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;

public class SpiderNest extends Mob {

	public SpiderNest() {
		
		spriteClass = SpiderNestSprite.class;
		
		hp(ht(10));
		defenseSkill = 1;
		baseSpeed = 0f;

		exp = 0;
		maxLvl = 9;
		
		postpone(20);
		
		loot = new PotionOfHealing();
		lootChance = 0.2f;
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

		Mob newSpider = MobSpawner.spawnRandomMob(Dungeon.level, getPos());
		
		if(isPet()) {
			Mob.makePet(newSpider, Dungeon.hero);
		}
		setState(SLEEPING);
		
		postpone(20);
		
		return true;
	}
	
	@Override
	public int dr() {
		return 0;
	}

	@Override
	public boolean canBePet() {
		return false;
	}
}
