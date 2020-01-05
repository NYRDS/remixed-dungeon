
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Slow;

public class FrostArrow extends Arrow {

	public static final float DURATION	= 5f;
	
	public FrostArrow() {
		this( 1 );
	}
	
	public FrostArrow( int number ) {
		super();
		quantity(number);
		
		baseMin = 0;
		baseMax = 6;
		baseDly = 0.75;
		
		image = FROST_ARROW_IMAGE;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity() * 5;
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		if(activateSpecial(attacker, defender, damage)) {
			Buff.prolong( defender, Slow.class, DURATION );
		}
		super.attackProc( attacker, defender, damage );
	}
}
