
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.sprites.ItemSpriteSheet;

public class PoisonArrow extends Arrow {

	public static final float DURATION	= 5f;
	
	public PoisonArrow() {
		this( 1 );
	}
	
	public PoisonArrow( int number ) {
		super();
		quantity = number;
		
		baseMin = 3;
		baseMax = 4;
		baseDly = 0.75;
		
		image = ItemSpriteSheet.ARROW_POISON;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity * 5;
	}

	@Override
	public void proc( Char attacker, Char defender, int damage ) {
		Buff.affect( defender, com.watabou.pixeldungeon.actors.buffs.Poison.class ).
		set( com.watabou.pixeldungeon.actors.buffs.Poison.durationFactor( defender ) * (firedFrom.level + 1) );

		super.proc( attacker, defender, damage );
	}
}
