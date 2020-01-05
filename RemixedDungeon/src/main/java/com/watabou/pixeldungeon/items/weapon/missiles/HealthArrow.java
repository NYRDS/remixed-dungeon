
package com.watabou.pixeldungeon.items.weapon.missiles;

import com.watabou.pixeldungeon.actors.Char;
import com.watabou.pixeldungeon.items.potions.PotionOfHealing;

public class HealthArrow extends Arrow {

	public HealthArrow() {
		this( 1 );
	}

	public HealthArrow(int number ) {
		super();
		quantity(number);

		image = HEALTH_ARROW_IMAGE;
		
		updateStatsForInfo();
	}
	
	@Override
	public int price() {
		return quantity() * 10;
	}

	@Override
	public void attackProc(Char attacker, Char defender, int damage ) {
		if(activateSpecial(attacker, defender, damage)) {
			PotionOfHealing.heal(defender, 0.15f);
		}

		super.attackProc( attacker, defender, damage );
	}
}
