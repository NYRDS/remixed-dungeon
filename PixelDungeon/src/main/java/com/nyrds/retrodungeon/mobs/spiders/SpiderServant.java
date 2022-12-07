package com.nyrds.retrodungeon.mobs.spiders;

import android.support.annotation.NonNull;

import com.nyrds.retrodungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

public class SpiderServant extends MultiKindMob {
	
	public SpiderServant() {
		hp(ht(25));
		defenseSkill = 5;
		baseSpeed = 1.1f;
		
		exp = 2;
		maxLvl = 9;

		loot = new MysteryMeat();
		lootChance = 0.067f;
	}
	
	@Override
	public int attackProc(@NonNull Char enemy, int damage ) {
		if (Random.Int( 4 ) == 0) {
			Buff.affect(enemy, Poison.class).set(Random.Int(2, 3) * Poison.durationFactor(enemy));
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
