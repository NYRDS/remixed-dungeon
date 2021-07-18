package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Stun;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class SpiderGuard extends MultiKindMob {

	public SpiderGuard() {
		hp(ht(35));
		baseDefenseSkill = 15;
		baseAttackSkill  = 17;
		baseSpeed = 1.2f;
		dmgMin = 8;
		dmgMax = 14;
		dr = 7;
		
		exp = 4;
		maxLvl = 10;
		
		kind = 1;
		
		loot(new MysteryMeat(), 0.067f);
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 10 ) == 0) {
			Buff.prolong( enemy, Stun.class, 3);
		}
		return damage;
	}
}
