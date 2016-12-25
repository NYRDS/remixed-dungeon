package com.nyrds.pixeldungeon.mobs.icecaves;

import com.nyrds.pixeldungeon.ml.R;
import com.watabou.noosa.Game;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Terror;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Gold;
import com.watabou.pixeldungeon.items.potions.PotionOfFrost;
import com.watabou.pixeldungeon.sprites.BruteSprite;
import com.watabou.pixeldungeon.sprites.CharSprite;
import com.watabou.pixeldungeon.utils.GLog;
import com.watabou.utils.Bundle;
import com.watabou.utils.Random;

public class Kobold extends Mob {

	public Kobold() {
		
		hp(ht(60));
		defenseSkill = 18;
		
		EXP = 10;
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
