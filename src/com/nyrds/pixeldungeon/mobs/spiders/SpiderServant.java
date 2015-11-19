package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderServantSprite;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

public class SpiderServant extends MultiKindMob {
	
	public SpiderServant() {
		
		spriteClass = SpiderServantSprite.class;
		
		hp(ht(25));
		defenseSkill = 5;
		baseSpeed = 1.1f;
		
		EXP = 2;
		maxLvl = 9;
		
		kind = Random.IntRange(0, 1);
		
		loot = new MysteryMeat();
		lootChance = 0.067f;
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
		return Random.NormalIntRange( 4, 6 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 11;
	}
	
	@Override
	public int dr() {
		return 5;
	}

}
