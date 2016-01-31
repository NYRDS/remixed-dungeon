package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderExplodingSprite;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.plants.Dreamweed;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.plants.Fadeleaf;
import com.watabou.pixeldungeon.plants.Firebloom;
import com.watabou.pixeldungeon.plants.Icecap;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.plants.Sorrowmoss;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.utils.Random;

public class SpiderExploding extends MultiKindMob {

	static Class<?> PLantClasses[] = {
		Firebloom.class, 
		Icecap.class, 
		Sorrowmoss.class,
		Earthroot.class,
		Sungrass.class,
		Fadeleaf.class,
		Dreamweed.class
	};
	
	public SpiderExploding() {
		
		spriteClass = SpiderExplodingSprite.class;
		
		hp(ht(5));
		defenseSkill = 1;
		baseSpeed = 2f;
		
		EXP = 3;
		maxLvl = 9;
		
		kind = Random.IntRange(0, 6);
		
		loot = new MysteryMeat();
		lootChance = 0.067f;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		
		try {
			Plant plant  = (Plant) PLantClasses[getKind()].newInstance();
			plant.pos = enemy.getPos();
			
			plant.effect(enemy.getPos(),enemy);
			
			die(this);
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} 
		return damage;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 3, 6 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 1000;
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
