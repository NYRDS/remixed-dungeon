package com.nyrds.pixeldungeon.mobs.icecaves;

import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;

public class Kobold extends Mob {

	public Kobold() {
		
		hp(ht(60));
		baseDefenseSkill = 18;
		baseAttackSkill  = 21;
		dmgMin = 10;
		dmgMax = 17;
		dr = 9;
		
		expForKill = 10;
		maxLvl = 20;
		
		loot(new PotionOfFrost(), 0.1f);
		
		addImmunity( Terror.class );
	}
}
