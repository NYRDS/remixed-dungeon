
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.actors.buffs.Buff;
import com.watabou.pixeldungeon.actors.buffs.Paralysis;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.utils.Random;

public class CurareDart extends Dart {

	public static final float DURATION	= 3f;

	public CurareDart() {
		this( 1 );
	}
	
	public CurareDart( int number ) {
		super();
		
		image = 4;
		
		setSTR(14);
		
		MIN = 1;
		MAX = 3;
		
		quantity(number);
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		Buff.prolong( defender, Paralysis.class, DURATION );
		super.attackProc( attacker, defender, damage );
	}

	@Override
	public Item random() {
		quantity(Random.Int( 2, 5 ));
		return this;
	}
	
	@Override
	public int price() {
		return 12 * quantity();
	}
}
