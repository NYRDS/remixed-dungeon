package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.common.MultiKindMob;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Poison;
import com.watabou.pixeldungeon.items.food.MysteryMeat;
import com.watabou.utils.Random;

import org.jetbrains.annotations.NotNull;

public class SpiderServant extends MultiKindMob {
	
	public SpiderServant() {
		hp(ht(25));
		baseDefenseSkill = 5;
		baseAttackSkill  = 11;
		baseSpeed = 1.1f;
		dmgMin = 4;
		dmgMax = 6;
		dr = 5;
		
		expForKill = 2;
		maxLvl = 9;

		loot(new MysteryMeat(), 0.03f);
	}
	
	@Override
	public int attackProc(@NotNull Char enemy, int damage ) {
		if (Random.Int( 4 ) == 0) {
			Buff.affect(enemy, Poison.class, Random.Int(2, 3) * Poison.durationFactor(enemy));
		}
		return damage;
	}
}
