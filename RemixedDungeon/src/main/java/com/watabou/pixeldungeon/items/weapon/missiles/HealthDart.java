package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.Item;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;
import com.watabou.utils.Random;

public class HealthDart extends Dart {

	public HealthDart() {
		this( 1 );
	}

	public HealthDart(int number ) {
		super();

		image = 14;
		
		setSTR(8);
		
		MIN = 1;
		MAX = 3;
		
		quantity(number);
	}
	
	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		PotionOfHealing.heal(defender,0.1f);
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
