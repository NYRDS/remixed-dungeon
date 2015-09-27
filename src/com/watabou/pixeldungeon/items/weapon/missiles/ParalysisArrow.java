
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class ParalysisArrow extends Arrow {

	public static final float DURATION	= 4f;
	
	public ParalysisArrow() {
		this( 1 );
	}
	
	public ParalysisArrow( int number ) {
		super();
		quantity(number);
		
		baseMin = 0;
		baseMax = 4;
		baseDly = 0.75;
		
		image = ItemSpriteSheet.ARROW_PARALYSIS;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity() * 5;
	}

	@Override
	public void proc( Char attacker, Char defender, int damage ) {
		if(activateSpecial(attacker, defender, damage)) {
			Buff.prolong( defender, Paralysis.class, DURATION );
		}
		super.proc( attacker, defender, damage );
	}
}
