package com.nyrds.retrodungeon.mobs.spiders;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.Badges;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

public class SpiderGuard extends MultiKindMob {

	public SpiderGuard() {
		hp(ht(35));
		defenseSkill = 15;
		baseSpeed = 1.2f;
		
		exp = 4;
		maxLvl = 10;
		
		kind = 1;
		
		loot = new MysteryMeat();
		lootChance = 0.067f;
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		if (Random.Int( 10 ) == 0) {
			Buff.prolong( enemy, Paralysis.class, 3);
		}
		return damage;
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 8, 14 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 17;
	}
	
	@Override
	public int dr() {
		return 7;
	}

	@Override
	public void die( Object cause ) {
		super.die( cause );
		Badges.validateRare( this );
	}

}
