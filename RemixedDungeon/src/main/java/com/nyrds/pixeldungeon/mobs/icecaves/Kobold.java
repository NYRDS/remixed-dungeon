package com.nyrds.pixeldungeon.mobs.icecaves;

import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.utils.Random;

public class Kobold extends Mob {

	public Kobold() {
		
		hp(ht(60));
		baseDefenseSkill = 18;
		baseAttackSkill  = 21;
		
		exp = 10;
		maxLvl = 20;
		
		loot(new PotionOfFrost(), 0.1f);
		
		addImmunity( Terror.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 17 );
	}

	@Override
	public int dr() {
		return 9;
	}

}
