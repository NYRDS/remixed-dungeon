package com.nyrds.retrodungeon.mobs.icecaves;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.utils.Random;

public class Kobold extends Mob {

	public Kobold() {
		
		hp(ht(60));
		defenseSkill = 18;
		
		exp = 10;
		maxLvl = 20;
		
		loot = new PotionOfFrost();
		lootChance = 0.1f;
		
		IMMUNITIES.add( Terror.class );
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 10, 17 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 21;
	}
	
	@Override
	public int dr() {
		return 9;
	}

}
