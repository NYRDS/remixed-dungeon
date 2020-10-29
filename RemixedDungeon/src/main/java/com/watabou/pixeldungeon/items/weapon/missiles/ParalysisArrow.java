
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Stun;

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
		
		image = PARALYSIS_ARROW_IMAGE;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity() * 5;
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		if(activateSpecial(attacker, defender, damage)) {
			Buff.prolong( defender, Stun.class, DURATION );
		}
		super.attackProc( attacker, defender, damage );
	}
}
