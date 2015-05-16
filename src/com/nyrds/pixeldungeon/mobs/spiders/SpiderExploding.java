package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderExplodingSprite;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

public class SpiderExploding extends Mob {

	private int kind = 0;
	
	public SpiderExploding() {
		
		spriteClass = SpiderExplodingSprite.class;
		
		hp(ht(15));
		defenseSkill = 1;
		baseSpeed = 2f;
		
		EXP = 3;
		maxLvl = 9;
		
		kind = Random.Int(0, 5);
		
		loot = new MysteryMeat();
		lootChance = 0.167f;
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
