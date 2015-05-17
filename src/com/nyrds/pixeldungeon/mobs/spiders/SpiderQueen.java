package com.nyrds.pixeldungeon.mobs.spiders;

import com.nyrds.pixeldungeon.mobs.spiders.sprites.SpiderQueenSprite;
import com.watabou.pixeldungeon.Dungeon;
import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.mobs.Mob;
import com.watabou.pixeldungeon.items.Generator;
import com.watabou.utils.Random;

public class SpiderQueen extends Mob {
	
	public SpiderQueen() {
		spriteClass = SpiderQueenSprite.class;
		
		hp(ht(1));
		defenseSkill = 18;
		
		EXP = 11;
		maxLvl = 21;
		
		loot = Generator.Category.POTION;
		lootChance = 0.83f;
	}
	
	@Override
	protected boolean act(){
		if(Random.Int(0, 20) == 0 && !SpiderEgg.eggLaid(pos)) {
			SpiderEgg.layEgg(pos);
		}
		
		return super.act();
	}
	
	@Override
	public int damageRoll() {
		return Random.NormalIntRange( 12, 20 );
	}
	
	@Override
	public int attackSkill( Char target ) {
		return 2;
	}
	
	@Override
	public int dr() {
		return 0;
	}
}
