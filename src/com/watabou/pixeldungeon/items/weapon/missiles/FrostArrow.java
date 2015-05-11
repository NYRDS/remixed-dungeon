
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.actors.buffs.Slow;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class FrostArrow extends Arrow {

	public static final float DURATION	= 5f;
	
	public FrostArrow() {
		this( 1 );
	}
	
	public FrostArrow( int number ) {
		super();
		quantity = number;
		
		baseMin = 0;
		baseMax = 6;
		baseDly = 0.75;
		
		image = ItemSpriteSheet.ARROW_FROST;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity * 5;
	}

	@Override
	public void proc( Char attacker, Char defender, int damage ) {
		Buff.prolong( defender, Slow.class, DURATION );
		super.proc( attacker, defender, damage );
	}
}
