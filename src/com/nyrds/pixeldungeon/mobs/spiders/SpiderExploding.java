package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderExplodingSprite;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.pixeldungeon.plants.Dreamweed;
import com.watabou.pixeldungeon.plants.Earthroot;
import com.watabou.pixeldungeon.plants.Fadeleaf;
import com.watabou.pixeldungeon.plants.Firebloom;
import com.watabou.pixeldungeon.plants.Icecap;
import com.watabou.pixeldungeon.plants.Plant;
import com.watabou.pixeldungeon.plants.Sorrowmoss;
import com.watabou.pixeldungeon.plants.Sungrass;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Random;

public class SpiderExploding extends Mob {

	private int kind = 0;
	
	static Class<?> PLantClasses[] = {
		Firebloom.class, 
		Icecap.class, 
		Sorrowmoss.class,
		Earthroot.class,
		Sungrass.class,
		Dreamweed.class,
		Fadeleaf.class
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
		lootChance = 0.167f;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		
		try {
			Plant plant  = (Plant) ((Class<?>) PLantClasses[getKind()]).newInstance();
			plant.pos = enemy.pos;
			
			plant.effect(enemy.pos,enemy);
			
			die(this);
			
		} catch (InstantiationException e) {
			GLog.w(e.getMessage());
		} catch (IllegalAccessException e) {
			GLog.w(e.getMessage());
		}
		
		return damage;
	}
	
	@Override
	public int getKind() {
		return kind;
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
