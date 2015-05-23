package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderServantSprite;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

public class SpiderServant extends Mob {

	private int kind = 0;
	
	public SpiderServant() {
		
		spriteClass = SpiderServantSprite.class;
		
		hp(ht(15));
		defenseSkill = 1;
		baseSpeed = 2f;
		
		EXP = 3;
		maxLvl = 9;
		
		kind = Random.IntRange(0, 1);
		
		loot = new MysteryMeat();
		lootChance = 0.167f;
	}
	
	@Override
	public int getKind() {
		return kind;
	}
	
	@Override
	public int attackProc( Char enemy, int damage ) {
		
		switch (getKind()) {
			case 0:
				if (Random.Int( 4 ) == 0) {
					Buff.affect( enemy, Poison.class ).set( Random.Int( 2, 3 ) * Poison.durationFactor( enemy ) );
				}
			break;
			
			case 1:
				if (Random.Int( 10 ) == 0) {
					Buff.prolong( enemy, Paralysis.class, 3);
				}				
				
			break;
		}
		
		return damage;
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
